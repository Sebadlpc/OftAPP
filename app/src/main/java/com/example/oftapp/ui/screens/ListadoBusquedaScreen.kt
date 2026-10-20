package com.example.oftapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.TextoOscuro

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

    val examenes = remember {
        listOf(
            ExamenItem("ID-1234", "Juan Pérez", "Tonometría", "12/10/2026"),
            ExamenItem("ID-5678", "María López", "Campo Visual", "11/10/2026"),
            ExamenItem("ID-9012", "Carlos Gómez", "OCT Retinal", "10/10/2026"),
            ExamenItem("ID-3456", "Ana Martínez", "Fundoscopía", "09/10/2026")
        )
    }

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
            // Campo de búsqueda con colores de texto explícitos
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Buscar por paciente, examen o ID...",
                        color = GrisAzulado
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = AzulClinico)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar", tint = GrisAzulado)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoOscuro,
                    unfocusedTextColor = TextoOscuro,
                    focusedContainerColor = BlancoClinico,
                    unfocusedContainerColor = BlancoClinico,
                    focusedBorderColor = AzulClinico,
                    unfocusedBorderColor = GrisAzulado.copy(alpha = 0.3f),
                    cursorColor = AzulClinico
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (examenesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron exámenes",
                        color = GrisAzulado,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
}

@Composable
fun ExamenCard(
    examen: ExamenItem,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(containerColor = BlancoClinico),
        border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
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
                    color = TextoOscuro,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = examen.fecha,
                    color = GrisAzulado,
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
                    color = GrisAzulado,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = examen.id,
                    style = MaterialTheme.typography.labelMedium,
                    color = AzulClinico,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}