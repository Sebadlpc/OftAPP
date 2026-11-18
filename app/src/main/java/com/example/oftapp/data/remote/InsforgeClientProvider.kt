package com.example.oftapp.data.remote

import com.example.oftapp.BuildConfig
import dev.insforge.InsforgeClient
import dev.insforge.createInsforgeClient
import dev.insforge.auth.Auth

/**
 * Proveedor de cliente singleton para la integración con Insforge SDK.
 *
 * Configura la conexión al backend de Insforge activando el módulo de Autenticación (Auth).
 * Lee las credenciales de forma segura desde BuildConfig generadas a partir de local.properties.
 */
object InsforgeClientProvider {

    /**
     * Valida si las credenciales de Insforge están configuradas en local.properties.
     */
    val isConfigured: Boolean
        get() = BuildConfig.INSFORGE_BASE_URL.isNotBlank() && BuildConfig.INSFORGE_ANON_KEY.isNotBlank()

    /**
     * Instancia singleton lazy del cliente de Insforge.
     * Valida que INSFORGE_BASE_URL e INSFORGE_ANON_KEY hayan sido configuradas
     * antes de crear el cliente para evitar fallos por URL inválida o credenciales ausentes.
     */
    val client: InsforgeClient by lazy {
        check(isConfigured) {
            "Falta configurar INSFORGE_BASE_URL e INSFORGE_ANON_KEY en local.properties"
        }
        createInsforgeClient(
            baseURL = BuildConfig.INSFORGE_BASE_URL,
            anonKey = BuildConfig.INSFORGE_ANON_KEY
        ) {
            install(Auth)
        }
    }
}
