package com.example.oftapp.data.remote

import com.example.oftapp.data.remote.dto.FhirBundleDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para la integración con la API estándar de interoperabilidad en salud HL7 HAPI FHIR R4.
 */
interface HapiFhirApiService {

    @GET("Observation")
    suspend fun getObservations(
        @Query("_count") count: Int = 10,
        @Query("_format") format: String = "json",
        @Query("code") code: String? = null
    ): Response<FhirBundleDto>

    @GET("Patient")
    suspend fun getPatients(
        @Query("_count") count: Int = 10,
        @Query("_format") format: String = "json"
    ): Response<FhirBundleDto>

    companion object {
        const val BASE_URL = "https://hapi.fhir.org/baseR4/"

        fun create(): HapiFhirApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HapiFhirApiService::class.java)
        }
    }
}
