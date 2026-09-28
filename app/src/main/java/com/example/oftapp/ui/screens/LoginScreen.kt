package com.example.oftapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.oftapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Tecnólogo Médico") }
    val roles = listOf("Tecnólogo Médico", "Médico Oftalmólogo", "Personal Administrativo", "Paciente")

    Surface(modifier = Modifier.fillMaxSize(), color = BlancoClinico) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null, tint = AzulClinico, modifier = Modifier.size(72.dp))
            Text("OftApp", style = MaterialTheme.typography.headlineLarge, color = TextoOscuro, fontWeight = FontWeight.Bold)
            Text("Trazabilidad de Exámenes", style = MaterialTheme.typography.bodyMedium, color = GrisAzulado)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Usuario / Correo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onLoginSuccess(selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = AzulClinico),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Iniciar Sesión")
            }
        }
    }
}
