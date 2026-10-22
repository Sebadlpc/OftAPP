package com.example.oftapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oftapp.ui.theme.*
import com.example.oftapp.ui.viewmodel.LoginUiState
import com.example.oftapp.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var password by remember { mutableStateOf("") }
    var expandedRoles by remember { mutableStateOf(false) }

    val roles = listOf(
        "Tecnólogo Médico",
        "Médico Oftalmólogo",
        "Personal Administrativo",
        "Paciente"
    )

    // Al autenticarse correctamente, navegar hacia la ruta Dashboard
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess(uiState.selectedRole)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BlancoClinico
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Logo OftApp",
                tint = AzulClinico,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "OftApp",
                style = MaterialTheme.typography.headlineLarge,
                color = TextoOscuro,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Trazabilidad de Exámenes Oftalmológicos",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisAzulado
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Selector de Rol Ficticio (ExposedDropdownMenuBox)
            ExposedDropdownMenuBox(
                expanded = expandedRoles,
                onExpandedChange = { expandedRoles = !expandedRoles },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol de Usuario") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoles)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulClinico,
                        unfocusedBorderColor = GrisAzulado.copy(alpha = 0.5f),
                        focusedLabelColor = AzulClinico,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedTextColor = TextoOscuro,
                        unfocusedTextColor = TextoOscuro
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedRoles,
                    onDismissRequest = { expandedRoles = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    roles.forEach { roleOption ->
                        DropdownMenuItem(
                            text = { Text(roleOption, color = TextoOscuro) },
                            onClick = {
                                viewModel.onRoleSelect(roleOption)
                                expandedRoles = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo de Usuario / Correo
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Usuario / Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulClinico,
                    unfocusedBorderColor = GrisAzulado.copy(alpha = 0.5f),
                    focusedLabelColor = AzulClinico,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedTextColor = TextoOscuro,
                    unfocusedTextColor = TextoOscuro
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulClinico,
                    unfocusedBorderColor = GrisAzulado.copy(alpha = 0.5f),
                    focusedLabelColor = AzulClinico,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedTextColor = TextoOscuro,
                    unfocusedTextColor = TextoOscuro
                )
            )

            // Mensaje de Error
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = RojoAlerta,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Inicio de Sesión
            Button(
                onClick = { viewModel.login(password) },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulClinico,
                    contentColor = BlancoClinico
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BlancoClinico,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
