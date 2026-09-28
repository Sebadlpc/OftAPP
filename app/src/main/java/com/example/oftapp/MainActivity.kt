package com.example.oftapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.oftapp.navigation.OftAppNavigation
import com.example.oftapp.ui.theme.OftAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OftAPPTheme {
                OftAppNavigation()
            }
        }
    }
}