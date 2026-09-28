package com.example.oftapp.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.oftapp.ui.screens.CargaDocumentosScreen
import com.example.oftapp.ui.screens.DashboardScreen
import com.example.oftapp.ui.screens.DetalleExamenScreen
import com.example.oftapp.ui.screens.HistorialClinicoScreen
import com.example.oftapp.ui.screens.ListadoBusquedaScreen
import com.example.oftapp.ui.screens.LoginScreen
import com.example.oftapp.ui.screens.RegistroAtencionScreen

@Composable
fun OftAppNavigation() {
    val navController = rememberNavController()
    var currentRole by remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    currentRole = role
                    navController.navigate("dashboard")
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigateToRegister = { navController.navigate("registro") },
                onNavigateToList = { navController.navigate("listado") },
                onNavigateToHistory = { navController.navigate("historial") }
            )
        }

        composable("registro") {
            RegistroAtencionScreen(
                onNextToDocumentUpload = { navController.navigate("carga_documentos") }
            )
        }

        composable("carga_documentos") {
            CargaDocumentosScreen(
                onFinishUpload = {
                    navController.popBackStack("dashboard", inclusive = false)
                }
            )
        }

        composable("listado") {
            ListadoBusquedaScreen(
                onExamSelected = { examId ->
                    navController.navigate("detalle/$examId")
                }
            )
        }

        composable("detalle/{examId}") { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            DetalleExamenScreen(
                examId = examId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("historial") {
            HistorialClinicoScreen(
                onSelectHistoricExam = { examId ->
                    navController.navigate("detalle/$examId")
                }
            )
        }
    }
}