package com.example.oftapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oftapp.data.local.entity.ExamEntity
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.TextoOscuro
import com.example.oftapp.ui.theme.VerdeSalud
import com.example.oftapp.ui.viewmodel.ListadoBusquedaViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoBusquedaScreen(
    viewModel: ListadoBusquedaViewModel = viewModel(),
    onExamSelected: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterOptions = listOf("Todos", "Pendientes", "Validados")

    // Estado local síncrono para eliminar el bug de duplicación de letras y salto de cursor
    var localQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = BlancoClinico,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Listado de exámenes",
                        color = BlancoClinico,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = BlancoClinico
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Opciones adicionales */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = BlancoClinico
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulClinico
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Campo de búsqueda con estado local síncrono que delega a onSearchQueryChanged
            OutlinedTextField(
                value = localQuery,
                onValueChange = { newQuery ->
                    localQuery = newQuery
                    viewModel.onSearchQueryChanged(newQuery)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Buscar por paciente, examen, sede o ID...",
                        color = GrisAzulado
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = AzulClinico
                    )
                },
                trailingIcon = {
                    if (localQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            localQuery = ""
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = GrisAzulado
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoOscuro,
                    unfocusedTextColor = TextoOscuro,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AzulClinico,
                    unfocusedBorderColor = GrisAzulado.copy(alpha = 0.4f),
                    cursorColor = AzulClinico
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fila de FilterChip ("Todos", "Pendientes", "Validados")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filterName ->
                    val isSelected = uiState.selectedFilter.equals(filterName, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterSelect(filterName) },
                        label = {
                            Text(
                                text = filterName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = TextoOscuro,
                            selectedContainerColor = AzulClinico,
                            selectedLabelColor = BlancoClinico
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) AzulClinico else GrisAzulado.copy(alpha = 0.4f),
                            selectedBorderColor = AzulClinico,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visualización de la lista de exámenes
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AzulClinico)
                }
            } else if (uiState.exams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron exámenes para el criterio seleccionado",
                        color = GrisAzulado,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.exams, key = { it.id }) { exam ->
                        ExamenCard(
                            exam = exam,
                            onClick = { onExamSelected(exam.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExamenCard(
    exam: ExamEntity,
    onClick: () -> Unit
) {
    val isValidated = exam.status.equals("Validado", ignoreCase = true) ||
            exam.status.equals("Completado", ignoreCase = true)
    val statusColor = if (isValidated) VerdeSalud else GrisAzulado
    val statusBg = if (isValidated) VerdeSalud.copy(alpha = 0.12f) else GrisAzulado.copy(alpha = 0.12f)

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL"))
    val fechaTexto = try {
        dateFormat.format(exam.date)
    } catch (_: Exception) {
        "N/D"
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.patientName,
                    color = TextoOscuro,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = fechaTexto,
                    color = GrisAzulado,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${exam.examType} (${exam.eye})",
                    color = GrisAzulado,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = exam.id,
                    style = MaterialTheme.typography.labelMedium,
                    color = AzulClinico,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.branch,
                    color = GrisAzulado.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = exam.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
