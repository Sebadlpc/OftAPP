package com.example.oftapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestUI {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun verificarBotonIngresoVisible(){
        rule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
    }

    @Test
    fun loginExitoso_muestraPantallaInicio(){
        rule.onNodeWithTag("Usuario").performTextInput("tecnologo")
        rule.onNodeWithTag("Contraseña").performTextInput("1234")

        rule.onNodeWithText("Iniciar Sesión").performClick()

        rule.onNodeWithText("Bienvenido/a,").assertIsDisplayed()
    }

    @Test
    fun loginFallido_muestraMensajeError(){
        rule.onNodeWithText("Iniciar Sesión").performClick()


        rule.onNodeWithText("Por favor, ingrese usuario y contraseña").assertIsDisplayed()
    }
}