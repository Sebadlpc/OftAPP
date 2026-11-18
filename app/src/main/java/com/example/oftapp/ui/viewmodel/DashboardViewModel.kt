package com.example.oftapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oftapp.data.remote.NetworkResult
import com.example.oftapp.data.repository.OftAppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del Dashboard principal.
 *
 * Expone de manera reactiva las métricas (totales, pendientes y validados),
 * el estado de conexión de red (Offline/Online), el conteo de exámenes pendientes
 * por sincronizar, la auto-sincronización con Spring Boot y la consulta interoperable HL7 HAPI FHIR.
 */
class DashboardViewModel(
    private val repository: OftAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentUserRole: String = "Tecnólogo Médico"
    private var currentPatientName: String = "Juan Pérez"
    private var dataJob: Job? = null

    init {
        observeConnectivityAndSync()
        loadDashboardData()
        loadFhirObservations()
    }

    private fun observeConnectivityAndSync() {
        // Monitorea conectividad reactiva
        viewModelScope.launch {
            repository.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }

        // Monitorea exámenes pendientes de sincronizar
        viewModelScope.launch {
            repository.getPendingExamsCount().collect { count ->
                _uiState.update { it.copy(pendingSyncCount = count) }
            }
        }
    }

    /**
     * Establece el rol y la identidad del usuario para aplicar las restricciones de privacidad.
     */
    fun setUserRole(role: String, patientName: String = "Juan Pérez") {
        this.currentUserRole = role
        this.currentPatientName = patientName
        loadDashboardData()
    }

    /**
     * Sincroniza manualmente los exámenes pendientes hacia el microservicio Spring Boot.
     */
    fun syncPendingExams() {
        if (_uiState.value.isSyncing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
            val result = repository.synchronizePendingExams()
            val message = when (result) {
                is NetworkResult.Success -> {
                    val count = result.data
                    if (count > 0) {
                        "$count exámenes sincronizados correctamente"
                    } else {
                        "0 exámenes sincronizados correctamente (al día)"
                    }
                }
                is NetworkResult.Error -> {
                    result.message
                }
                is NetworkResult.Loading -> null
            }
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncMessage = message
                )
            }
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    /**
     * Carga observaciones clínicas externas desde el servidor interoperable HL7 HAPI FHIR.
     */
    fun loadFhirObservations() {
        viewModelScope.launch {
            _uiState.update { it.copy(fhirLoading = true, fhirError = null) }
            when (val result = repository.fetchFhirObservations()) {
                is NetworkResult.Success -> {
                    val entries = result.data.entry.orEmpty()
                    val items = entries.mapNotNull { entry ->
                        val res = entry.resource ?: return@mapNotNull null
                        val codeText = res.code?.text
                            ?: res.code?.coding?.firstOrNull()?.display
                            ?: "Observación Médica General"
                        val valText = when {
                            res.valueQuantity != null -> "${res.valueQuantity.value ?: ""} ${res.valueQuantity.unit ?: ""}".trim()
                            !res.valueString.isNullOrBlank() -> res.valueString
                            else -> "Parámetro registrado"
                        }
                        val dateText = res.effectiveDateTime?.take(10) ?: "Reciente"
                        FhirObservationDisplayItem(
                            id = res.id ?: "FHIR-${System.currentTimeMillis()}",
                            title = codeText,
                            value = valText,
                            date = dateText,
                            status = res.status?.uppercase() ?: "FINAL"
                        )
                    }
                    _uiState.update {
                        it.copy(
                            fhirObservations = items.take(4),
                            fhirLoading = false,
                            fhirError = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            fhirLoading = false,
                            fhirError = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    _uiState.update { it.copy(fhirLoading = true) }
                }
            }
        }
    }

    /**
     * Carga y se suscribe de forma reactiva al flujo de exámenes del repositorio.
     */
    fun loadDashboardData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedDatabaseIfNeeded()
            repository.getAllExams().collect { allExams ->
                val exams = if (currentUserRole.equals("Paciente", ignoreCase = true)) {
                    allExams.filter {
                        it.patientName.equals(currentPatientName, ignoreCase = true) ||
                                it.patientName.contains("Juan", ignoreCase = true)
                    }
                } else {
                    allExams
                }

                val totalCount = exams.size
                val pendingCount = exams.count {
                    it.status.equals("Pendiente", ignoreCase = true) ||
                            it.status.equals("En Proceso", ignoreCase = true)
                }
                val validatedCount = exams.count {
                    it.status.equals("Validado", ignoreCase = true) ||
                            it.status.equals("Completado", ignoreCase = true)
                }
                val recentExams = exams.take(5)

                _uiState.update {
                    it.copy(
                        totalCount = totalCount,
                        pendingCount = pendingCount,
                        validatedCount = validatedCount,
                        recentExams = recentExams,
                        isLoading = false
                    )
                }
            }
        }
    }
}
