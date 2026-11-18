package com.example.oftapp.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * Motor de síntesis de voz (TextToSpeech) para cumplir con el estándar WCAG 2.2 AA.
 *
 * Configurado primordialmente para español de Chile (es-CL) con fallback automático
 * a español general (es-ES / es) y liberación segura de recursos en onDispose.
 */
class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized: Boolean = false
    private val pendingQueue = mutableListOf<String>()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            val localeChile = Locale.forLanguageTag("es-CL")
            val localeSpain = Locale.forLanguageTag("es-ES")
            val localeGenericSpanish = Locale.forLanguageTag("es")

            when {
                engine.isLanguageAvailable(localeChile) >= TextToSpeech.LANG_AVAILABLE -> {
                    engine.language = localeChile
                }
                engine.isLanguageAvailable(localeSpain) >= TextToSpeech.LANG_AVAILABLE -> {
                    engine.language = localeSpain
                }
                engine.isLanguageAvailable(localeGenericSpanish) >= TextToSpeech.LANG_AVAILABLE -> {
                    engine.language = localeGenericSpanish
                }
                else -> {
                    engine.language = Locale.getDefault()
                }
            }

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })

            isInitialized = true
            synchronized(pendingQueue) {
                for (text in pendingQueue) {
                    speak(text)
                }
                pendingQueue.clear()
            }
        } else {
            _isSpeaking.value = false
        }
    }

    /**
     * Lee en voz alta el texto suministrado. Si el motor aún no inicializa, encola el mensaje.
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        val engine = tts ?: return
        if (text.isBlank()) return

        if (!isInitialized) {
            synchronized(pendingQueue) {
                pendingQueue.add(text)
            }
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        _isSpeaking.value = true
        engine.speak(text, queueMode, null, utteranceId)
    }

    /**
     * Detiene la lectura en curso.
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * Libera de forma limpia los recursos de TextToSpeech.
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        } finally {
            tts = null
            isInitialized = false
            _isSpeaking.value = false
        }
    }
}

/**
 * Composable helper que proporciona una instancia de [TextToSpeechHelper] vinculada
 * al ciclo de vida composable con liberación automática en `onDispose`.
 */
@Composable
fun rememberTextToSpeechHelper(): TextToSpeechHelper {
    val context = LocalContext.current
    val helper = remember { TextToSpeechHelper(context) }
    DisposableEffect(helper) {
        onDispose {
            helper.shutdown()
        }
    }
    return helper
}
