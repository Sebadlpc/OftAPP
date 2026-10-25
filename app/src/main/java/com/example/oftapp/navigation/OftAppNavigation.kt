package com.example.oftapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.oftapp.ui.screens.PerfilScreen
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
 * la inyección de ViewModels reactivos con Room ([AppDatabase] y [OftAppRepositoryImpl]),
 * la transferencia de parámetros de navegación (como examId y patientName) y control de acceso por rol.
 */
@Composable
fun OftAppNavigation() {
    val navController = rememberNavController()
    var currentRole by rememberSaveable { mutableStateOf("Tecnólogo Médico") }

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
                onNavigateToSearch = { navController.navigate("listado") },
                onNavigateToHistory = { navController.navigate("historial") },
                onNavigateToProfile = { navController.navigate("perfil") },
                onNavigateToDetail = { examId -> navController.navigate("detalle/$examId") }
            )
        }

        // 3. Formulario de Registro de Atención Médica (Flujo directo hacia Carga de Documentos)
        composable("registro") {
            val registroViewModel: RegistroAtencionViewModel = viewModel(factory = factory)
            RegistroAtencionScreen(
                viewModel = registroViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUpload = { examId ->
                    navController.navigate("carga_documentos/$examId")
                }
            )
        }

        // 4. Pantalla de Carga de Documentos con examId asociado
        composable(
            route = "carga_documentos/{examId}",
            arguments = listOf(
                navArgument("examId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            CargaDocumentosScreen(
                examId = examId,
                repository = repository,
                onFinishUpload = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            )
        }

        // 4b. Pantalla de Carga de Documentos general (fallback)
        composable("carga_documentos") {
            CargaDocumentosScreen(
                examId = null,
                repository = repository,
                onFinishUpload = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            )
        }

        // 5. Pantalla de Listado y Búsqueda de Exámenes con Filtros y Restricción por Rol
        composable("listado") {
            val listadoViewModel: ListadoBusquedaViewModel = viewModel(factory = factory)
            ListadoBusquedaScreen(
                viewModel = listadoViewModel,
                userRole = currentRole,
                onExamSelected = { examId ->
                    navController.navigate("detalle/$examId")
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate("historial")
                },
                onNavigateToProfile = {
                    navController.navigate("perfil")
                }
            )
        }

        // 6. Detalle del Examen Clínico con Parámetro examId y acciones de previsualización y descarga
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

        // 7. Historial Clínico y Línea de Tiempo con Buscador Dinámico y Carga Perezosa
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
                userRole = currentRole,
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
                onNavigateToProfile = {
                    navController.navigate("perfil")
                }
            )
        }

        // 8. Pantalla de Perfil de Usuario con Conteo Exclusivo y Cierre de Sesión
        composable("perfil") {
            val loginViewModel: LoginViewModel = viewModel(factory = factory)
            PerfilScreen(
                userRole = currentRole,
                repository = repository,
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                },
                onNavigateToSearch = {
                    navController.navigate("listado")
                },
                onNavigateToHistory = {
                    navController.navigate("historial")
                }
            )
        }
    }
}
