package com.example.oftapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.oftapp.data.local.AppDatabase
import com.example.oftapp.data.repository.OftAppRepositoryImpl
import com.example.oftapp.ui.screens.CargaDocumentosScreen
import com.example.oftapp.ui.screens.DashboardScreen
import com.example.oftapp.ui.screens.DetalleExamenScreen
import com.example.oftapp.ui.screens.HistorialClinicoScreen
import com.example.oftapp.ui.screens.ListadoBusquedaScreen
import com.example.oftapp.ui.screens.LoginScreen
import com.example.oftapp.ui.screens.RegistroAtencionScreen
import com.example.oftapp.ui.viewmodel.AppViewModelFactory
import com.example.oftapp.ui.viewmodel.DashboardViewModel
import com.example.oftapp.ui.viewmodel.DetalleExamenViewModel
import com.example.oftapp.ui.viewmodel.HistorialClinicoViewModel
import com.example.oftapp.ui.viewmodel.ListadoBusquedaViewModel
import com.example.oftapp.ui.viewmodel.LoginViewModel
import com.example.oftapp.ui.viewmodel.RegistroAtencionViewModel

/**
 * Grafo central de navegación de OftAPP.
 *
 * Configura las rutas del sistema, la instanciación de dependencias mediante [AppViewModelFactory],
 * la inyección de ViewModels reactivos con Room ([AppDatabase] y [OftAppRepositoryImpl])
 * y la transferencia segura de argumentos de navegación entre pantallas.
 */
@Composable
fun OftAppNavigation() {
    val navController = rememberNavController()
    var currentRole by remember { mutableStateOf("Tecnólogo Médico") }

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { OftAppRepositoryImpl.fromDatabase(database) }
    val factory = remember { AppViewModelFactory(repository) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 1. Pantalla de Autenticación
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel(factory = factory)
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { role ->
                    currentRole = role
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla del Dashboard Principal
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                userRole = currentRole,
                onNavigateToRegister = { navController.navigate("registro") },
                onNavigateToUpload = { navController.navigate("carga_documentos") },
                onNavigateToSearch = { navController.navigate("listado") },
                onNavigateToHistory = { navController.navigate("historial") },
                onNavigateToDetail = { examId -> navController.navigate("detalle/$examId") }
            )
        }

        // 3. Formulario de Registro de Atención Médica
        composable("registro") {
            val registroViewModel: RegistroAtencionViewModel = viewModel(factory = factory)
            RegistroAtencionScreen(
                viewModel = registroViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNextToDocumentUpload = { navController.navigate("carga_documentos") }
            )
        }

        // 4. Pantalla de Carga de Documentos Adjuntos
        composable("carga_documentos") {
            CargaDocumentosScreen(
                onFinishUpload = {
                    navController.popBackStack("dashboard", inclusive = false)
                }
            )
        }

        // 5. Pantalla de Listado y Búsqueda de Exámenes con Filtros
        composable("listado") {
            val listadoViewModel: ListadoBusquedaViewModel = viewModel(factory = factory)
            ListadoBusquedaScreen(
                viewModel = listadoViewModel,
                onExamSelected = { examId ->
                    navController.navigate("detalle/$examId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Detalle del Examen Clínico con Parámetro examId
        composable(
            route = "detalle/{examId}",
            arguments = listOf(
                navArgument("examId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            val detalleFactory = remember(examId) { AppViewModelFactory(repository, examId) }
            val detalleViewModel: DetalleExamenViewModel = viewModel(
                key = "detalle_$examId",
                factory = detalleFactory
            )
            DetalleExamenScreen(
                examId = examId,
                viewModel = detalleViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Historial Clínico y Línea de Tiempo con Parámetro Opcional patientName
        composable(
            route = "historial?patientName={patientName}",
            arguments = listOf(
                navArgument("patientName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val patientName = backStackEntry.arguments?.getString("patientName")
            val historialViewModel: HistorialClinicoViewModel = viewModel(factory = factory)
            HistorialClinicoScreen(
                initialPatientName = patientName,
                viewModel = historialViewModel,
                onSelectExam = { examId ->
                    navController.navigate("detalle/$examId")
                },
                onNavigateToHome = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                },
                onNavigateToSearch = {
                    navController.navigate("listado")
                },
                onNavigateToProfile = { /* Perfil */ }
            )
        }
    }
}
