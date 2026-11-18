package com.example.oftapp.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore de preferencias para la persistencia de configuraciones de Accesibilidad WCAG 2.2 AA.
 */
val Context.accessibilityDataStore: DataStore<Preferences> by preferencesDataStore(name = "oftapp_accessibility_prefs")

data class AccessibilitySettings(
    val highContrastMode: Boolean = false,
    val fontScaleFactor: Float = 1.0f
)

class AccessibilityPreferences(context: Context) {

    private val dataStore: DataStore<Preferences> = context.applicationContext.accessibilityDataStore

    companion object {
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("pref_high_contrast_mode")
        val KEY_FONT_SCALE = floatPreferencesKey("pref_font_scale_factor")

        @Volatile
        private var INSTANCE: AccessibilityPreferences? = null

        fun getInstance(context: Context): AccessibilityPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AccessibilityPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val accessibilitySettings: Flow<AccessibilitySettings> = dataStore.data.map { prefs ->
        AccessibilitySettings(
            highContrastMode = prefs[KEY_HIGH_CONTRAST] ?: false,
            fontScaleFactor = prefs[KEY_FONT_SCALE] ?: 1.0f
        )
    }

    suspend fun setHighContrastMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HIGH_CONTRAST] = enabled
        }
    }

    suspend fun setFontScaleFactor(factor: Float) {
        val normalizedFactor = when {
            factor <= 1.1f -> 1.0f
            factor <= 1.3f -> 1.2f
            else -> 1.4f
        }
        dataStore.edit { prefs ->
            prefs[KEY_FONT_SCALE] = normalizedFactor
        }
    }
}
