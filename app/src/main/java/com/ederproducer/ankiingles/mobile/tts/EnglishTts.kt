package com.ederproducer.ankiingles.mobile.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Voz em inglês americano usando o mecanismo de TTS do próprio Android.
 * Não depende do Google Tradutor pela rede e não salva nenhum áudio no aparelho.
 */
class EnglishTts(context: Context, private val onStatus: (Boolean) -> Unit) {

    private var tts: TextToSpeech? = null
    private var done: ((Boolean) -> Unit)? = null
    var available: Boolean = false
        private set

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            available = status == TextToSpeech.SUCCESS
            if (available) {
                val result = tts?.setLanguage(Locale.US) ?: TextToSpeech.LANG_NOT_SUPPORTED
                available = result == TextToSpeech.LANG_AVAILABLE ||
                    result == TextToSpeech.LANG_COUNTRY_AVAILABLE
                tts?.setSpeechRate(1f)
            }
            onStatus(available)
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { done?.invoke(true) }
            override fun onError(utteranceId: String?) { done?.invoke(false) }
        })
    }

    fun speak(text: String, rate: Float = 1f, onDone: ((Boolean) -> Unit)? = null) {
        done = onDone
        if (!available || text.isBlank()) {
            onDone?.invoke(false)
            return
        }
        tts?.stop()
        tts?.setSpeechRate(rate)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "anki-card")
        }
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "anki-card")
        if (result == TextToSpeech.ERROR) onDone?.invoke(false)
    }

    fun stop() { tts?.stop() }

    fun shutdown() { tts?.shutdown() }
}
