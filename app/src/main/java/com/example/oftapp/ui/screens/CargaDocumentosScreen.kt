package com.example.oftapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CargaDocumentosScreen(onFinishUpload: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pantalla de Carga de Documentos")
            Button(onClick = onFinishUpload) {
                Text("Terminar y volver al Dashboard")
            }
        }
    }
}