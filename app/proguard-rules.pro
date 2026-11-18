# ==============================================================================
# OftAPP - Reglas de Ofuscación y Optimización ProGuard / R8
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. DTOs y Modelos de Red (Retrofit & Gson)
# ------------------------------------------------------------------------------
-keep class com.example.oftapp.data.remote.** { *; }
-keep class com.example.oftapp.data.remote.dto.** { *; }
-keepclassmembers class com.example.oftapp.data.remote.dto.** { <fields>; }

# ------------------------------------------------------------------------------
# 2. Entidades de Base de Datos Local Room
# ------------------------------------------------------------------------------
-keep class com.example.oftapp.data.local.entity.** { *; }
-keepclassmembers class com.example.oftapp.data.local.entity.** { <fields>; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 3. Modelos de Dominio y Conversión
# ------------------------------------------------------------------------------
-keep class com.example.oftapp.model.** { *; }

# ------------------------------------------------------------------------------
# 4. Serialización Gson (@SerializedName)
# ------------------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-keepclassmembers enum * { *; }

# ------------------------------------------------------------------------------
# 5. SDK de Insforge (Auth & BaaS Client)
# ------------------------------------------------------------------------------
-keep class dev.insforge.** { *; }
-keepclassmembers class dev.insforge.** { *; }
-dontwarn dev.insforge.**

# ------------------------------------------------------------------------------
# 6. Retrofit & OkHttp
# ------------------------------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions, InnerClasses, EnclosingMethod
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# ------------------------------------------------------------------------------
# 7. AndroidX Security Crypto & DataStore Preferences
# ------------------------------------------------------------------------------
-keep class androidx.security.crypto.** { *; }
-keep class androidx.datastore.** { *; }
-dontwarn androidx.security.crypto.**
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.slf4j.**

# ------------------------------------------------------------------------------
# 8. Coil Image Loader
# ------------------------------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

