package com.example.oftapp.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DashboardScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onNavigateToRegister) { Text("Ir a Registro") }
            Button(onClick = onNavigateToList) { Text("Ir a Listado") }
            Button(onClick = onNavigateToHistory) { Text("Ir a Historial") }
        }
    }
}