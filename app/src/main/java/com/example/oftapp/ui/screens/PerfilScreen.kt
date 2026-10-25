package com.example.oftapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oftapp.data.repository.OftAppRepository
import com.example.oftapp.ui.theme.AzulClinico
import com.example.oftapp.ui.theme.BlancoClinico
import com.example.oftapp.ui.theme.GrisAzulado
import com.example.oftapp.ui.theme.RojoAlerta
import com.example.oftapp.ui.theme.TextoOscuro
import com.example.oftapp.ui.theme.VerdeSalud
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    userRole: String = "Tecnólogo Médico",
    repository: OftAppRepository? = null,
    onLogout: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val isPatient = userRole.equals("Paciente", ignoreCase = true)

    // Datos personales diferenciados
    val nombreUsuario = when {
        isPatient -> "Juan Pérez"
        userRole.contains("Médico", ignoreCase = true) -> "Dra. Carla Ruiz"
        userRole.contains("Tecnólogo", ignoreCase = true) -> "Lic. Andrés Gómez"
        userRole.contains("Admin", ignoreCase = true) -> "Carlos Vega (Administrador)"
        else -> "Usuario OftApp"
    }

    val emailUsuario = when {
        isPatient -> "juan.perez@email.com"
        userRole.contains("Médico", ignoreCase = true) -> "carla.ruiz@oftapp.cl"
        userRole.contains("Tecnólogo", ignoreCase = true) -> "andres.gomez@oftapp.cl"
        userRole.contains("Admin", ignoreCase = true) -> "admin@oftapp.cl"
        else -> "usuario@oftapp.cl"
    }

    // Observar exámenes para conteo en tiempo real
    val allExams by (repository?.getAllExams() ?: flowOf(emptyList())).collectAsState(initial = emptyList())

    val personalExams = remember(allExams, isPatient) {
        if (isPatient) {
            allExams.filter {
                it.patientName.equals("Juan Pérez", ignoreCase = true) ||
                        it.patientName.contains("Juan", ignoreCase = true)
            }
        } else {
            allExams
        }
    }

    val personalCount = personalExams.size
    val personalValidatedCount = personalExams.count {
        it.status.equals("Validado", ignoreCase = true) ||
                it.status.equals("Completado", ignoreCase = true)
    }
    val personalPendingCount = personalExams.count {
        it.status.equals("Pendiente", ignoreCase = true) ||
                it.status.equals("En Proceso", ignoreCase = true)
    }

    Scaffold(
        containerColor = BlancoClinico,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        color = BlancoClinico,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulClinico)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = BlancoClinico, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Outlined.Search, contentDescription = "Búsqueda") },
                    label = { Text("Búsqueda") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Outlined.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulClinico,
                        selectedTextColor = AzulClinico,
                        indicatorColor = AzulClinico.copy(alpha = 0.15f),
                        unselectedIconColor = GrisAzulado,
                        unselectedTextColor = GrisAzulado
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Avatar circular de usuario
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(AzulClinico),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombreUsuario.take(1),
                    color = BlancoClinico,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Datos principales
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = nombreUsuario,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AzulClinico.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = userRole,
                        color = AzulClinico,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // TARJETA DE CONTEO DE EXÁMENES (Diferenciada según Rol)
            if (isPatient) {
                // Conteo exclusivo de sus exámenes personales para el paciente
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AzulClinico.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AzulClinico.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Mis Exámenes Clínicos Personales",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulClinico
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PerfilMetricaItem(
                                valor = "$personalCount",
                                label = "Total Exámenes",
                                color = AzulClinico
                            )
                            PerfilMetricaItem(
                                valor = "$personalValidatedCount",
                                label = "Validados",
                                color = VerdeSalud
                            )
                            PerfilMetricaItem(
                                valor = "$personalPendingCount",
                                label = "Pendientes",
                                color = GrisAzulado
                            )
                        }
                    }
                }
            } else {
                // Indicador de acceso global para roles facultativos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AzulClinico.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AzulClinico.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Acceso Clínico Facultativo Global",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulClinico
                            )
                            Text(
                                text = "Acceso a todos los pacientes y fichas",
                                fontSize = 12.sp,
                                color = GrisAzulado
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AzulClinico
                        ) {
                            Text(
                                text = "${allExams.size} exámenes",
                                color = BlancoClinico,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Tarjeta de Información Detallada del Usuario / Paciente
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlancoClinico),
                border = BorderStroke(1.dp, GrisAzulado.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isPatient) "Datos Personales del Paciente" else "Información de la Cuenta Profesional",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AzulClinico
                    )

                    HorizontalDivider(color = GrisAzulado.copy(alpha = 0.15f))

                    PerfilDatoItem(
                        icon = Icons.Default.Person,
                        label = "Nombre Completo",
                        valor = nombreUsuario
                    )

                    if (isPatient) {
                        PerfilDatoItem(
                            icon = Icons.Default.Badge,
                            label = "RUT",
                            valor = "12.345.678-9"
                        )
                        PerfilDatoItem(
                            icon = Icons.Default.Call,
                            label = "Teléfono de Contacto",
                            valor = "+56 9 1234 5678"
                        )
                    }

                    PerfilDatoItem(
                        icon = Icons.Default.Email,
                        label = "Correo Electrónico",
                        valor = emailUsuario
                    )

                    PerfilDatoItem(
                        icon = Icons.Default.LocalHospital,
                        label = "Centro Clínico",
                        valor = "Clínica Oftalmológica OftAPP - Sede Central"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón destacado de Cerrar Sesión
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RojoAlerta,
                    contentColor = BlancoClinico
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar Sesión",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PerfilMetricaItem(
    valor: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = GrisAzulado
        )
    }
}

@Composable
private fun PerfilDatoItem(
    icon: ImageVector,
    label: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AzulClinico.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AzulClinico,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = GrisAzulado
            )
            Text(
                text = valor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextoOscuro
            )
        }
    }
}
