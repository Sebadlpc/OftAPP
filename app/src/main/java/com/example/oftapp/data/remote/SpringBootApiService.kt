package com.example.oftapp.data.remote

import android.content.Context
import android.os.Build
import com.example.oftapp.data.local.security.EncryptedSessionManager
import com.example.oftapp.data.remote.dto.ActualizarEstadoDto
import com.example.oftapp.data.remote.dto.ExamenSyncDto
import com.example.oftapp.data.remote.dto.LoginRequestDto
import com.example.oftapp.data.remote.dto.LoginResponseDto
import com.example.oftapp.data.remote.dto.SpringPacienteDto
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para el consumo de microservicios Spring Boot de OftAPP.
 */
interface SpringBootApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("api/v1/examenes")
    suspend fun getExamenes(
        @Query("pacienteId") pacienteId: Long? = null,
        @Query("rol") rol: String? = null
    ): Response<List<ExamenSyncDto>>

    @POST("api/v1/examenes")
    suspend fun createExamen(@Body examen: ExamenSyncDto): Response<ExamenSyncDto>

    @PUT("api/v1/examenes/{id}/estado")
    suspend fun updateEstado(
        @Path("id") id: Long,
        @Body request: ActualizarEstadoDto
    ): Response<ExamenSyncDto>

    @POST("api/v1/sync")
    suspend fun syncExamenes(@Body examenes: List<ExamenSyncDto>): Response<List<ExamenSyncDto>>

    @GET("api/v1/pacientes")
    suspend fun getPacientes(): Response<List<SpringPacienteDto>>

    companion object {
        const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/"
        const val DEVICE_BASE_URL = "http://127.0.0.1:8080/"

        private fun isEmulator(): Boolean {
            return Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    "google_sdk" == Build.PRODUCT
        }

        fun create(context: Context): SpringBootApiService {
            val sessionManager = EncryptedSessionManager.getInstance(context)

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                val token = sessionManager.getToken()
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                val role = sessionManager.getUserRole() ?: "TECNOLOGO"
                requestBuilder.addHeader("X-User-Role", role)

                chain.proceed(requestBuilder.build())
            }

            // Fallback dinámico multinivel: 127.0.0.1 (adb reverse), 192.168.1.9 (red local Wi-Fi) y 10.0.2.2 (emulador)
            val hostFallbackInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val candidateHosts = if (isEmulator()) {
                    listOf("10.0.2.2", "127.0.0.1", "192.168.1.9")
                } else {
                    listOf("127.0.0.1", "192.168.1.9", "10.0.2.2")
                }

                var lastException: IOException? = null
                for (host in candidateHosts) {
                    try {
                        val retryUrl = originalRequest.url.newBuilder().host(host).build()
                        val retryRequest = originalRequest.newBuilder().url(retryUrl).build()
                        return@Interceptor chain.proceed(retryRequest)
                    } catch (e: IOException) {
                        lastException = e
                    }
                }
                throw lastException ?: IOException("No se pudo conectar con el servidor Spring Boot")
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(hostFallbackInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

            val initialBaseUrl = if (isEmulator()) EMULATOR_BASE_URL else DEVICE_BASE_URL

            return Retrofit.Builder()
                .baseUrl(initialBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpringBootApiService::class.java)
        }
    }
}
