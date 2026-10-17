package com.example.oftapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Modelo de datos de ejemplo
data class ExamenItem(
    val id: String,
    val paciente: String,
    val tipoExamen: String,
    val fecha: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoBusquedaScreen(
    onExamSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Lista de prueba (reemplazar posteriormente con datos del ViewModel / Base de datos)
    val examenes = remember {
        listOf(
            ExamenItem("ID-1234", "Juan Pérez", "Tonometría", "12/10/2026"),
            ExamenItem("ID-5678", "María López", "Campo Visual", "11/10/2026"),
            ExamenItem("ID-9012", "Carlos Gómez", "OCT Retinal", "10/10/2026"),
            ExamenItem("ID-3456", "Ana Martínez", "Fundoscopía", "09/10/2026")
        )
    }

    // Filtrado automático según coincidencia con paciente, tipo de examen o ID
    val examenesFiltrados = remember(searchQuery, examenes) {
        if (searchQuery.isBlank()) {
            examenes
        } else {
            examenes.filter {
                it.paciente.contains(searchQuery, ignoreCase = true) ||
                        it.tipoExamen.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campo de entrada de texto para la búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por paciente, examen o ID...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido según los resultados
        if (examenesFiltrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron exámenes",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(examenesFiltrados, key = { it.id }) { examen ->
                    ExamenCard(
                        examen = examen,
                        onClick = { onExamSelected(examen.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamenCard(
    examen: ExamenItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = examen.paciente,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = examen.fecha,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = examen.tipoExamen,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = examen.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}