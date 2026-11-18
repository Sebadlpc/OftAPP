package com.example.oftapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.oftapp.data.local.datastore.AccessibilityPreferences
import com.example.oftapp.data.local.datastore.AccessibilitySettings
import com.example.oftapp.navigation.OftAppNavigation
import com.example.oftapp.ui.theme.OftAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val accessibilityPrefs = remember { AccessibilityPreferences.getInstance(applicationContext) }
            val accessibilitySettings by accessibilityPrefs.accessibilitySettings.collectAsState(
                initial = AccessibilitySettings()
            )

            OftAPPTheme(
                highContrast = accessibilitySettings.highContrastMode,
                fontScaleFactor = accessibilitySettings.fontScaleFactor
            ) {
                OftAppNavigation()
            }
        }
    }
}