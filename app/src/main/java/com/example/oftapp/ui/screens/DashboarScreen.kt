package com.example.oftapp.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val PrimaryBlue = Color(0xFF1A73E8)
private val BgLight = Color(0xFFF6F8FA)
private val RedLight = Color(0xFFFDE8E8)
private val RedText = Color(0xFFE53935)
private val GreenLight = Color(0xFFAAF1AF)
private val GreenText = Color(0xFF2E7D32)

data class ExamItem(
    val eyeTag: String,
    val title: String,
    val patientName: String,
    val dateAndRut: String,
    val isValidated: Boolean
)

@OptIn
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