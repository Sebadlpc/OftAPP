package com.example.oftapp.data.local.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de persistencia segura de sesión clínica utilizando AndroidX Security Crypto.
 *
 * Utiliza [MasterKey] con esquema AES256_GCM respaldado por hardware (Android KeyStore)
 * y [EncryptedSharedPreferences] para resguardar de forma cifrada el token JWT/simulado,
 * el rol clínico activo, el ID del usuario y el RUT del paciente/profesional.
 */
class EncryptedSessionManager(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(
        token: String,
        role: String,
        userId: Long,
        userRut: String? = null,
        userName: String? = null
    ) {
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_RUT, userRut)
            .putString(KEY_USER_NAME, userName)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_ROLE, null)
    }

    fun getUserId(): Long? {
        val id = sharedPreferences.getLong(KEY_USER_ID, -1L)
        return if (id != -1L) id else null
    }

    fun getUserRut(): String? {
        return sharedPreferences.getString(KEY_USER_RUT, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "oftapp_secure_session_prefs"
        private const val KEY_TOKEN = "session_token"
        private const val KEY_ROLE = "session_user_role"
        private const val KEY_USER_ID = "session_user_id"
        private const val KEY_USER_RUT = "session_user_rut"
        private const val KEY_USER_NAME = "session_user_name"

        @Volatile
        private var INSTANCE: EncryptedSessionManager? = null

        fun getInstance(context: Context): EncryptedSessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EncryptedSessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
