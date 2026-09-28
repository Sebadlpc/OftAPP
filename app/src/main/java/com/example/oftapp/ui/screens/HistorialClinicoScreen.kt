package com.example.oftapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HistorialClinicoScreen(onSelectHistoricExam: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pantalla de Historial Clínico")
            Button(onClick = { onSelectHistoricExam("HIST-999") }) {
                Text("Simular toque en Examen Histórico")
            }
        }
    }
}