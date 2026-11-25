package com.webtoapp.core.webview

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.webkit.WebView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.ScriptHandler
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.webtoapp.core.logging.AppLogger
import java.util.Locale
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal class WebSpeechSynthesisBridge private constructor(private val context: Context) {
    private class Speech(
        val id: String,
        val reply: JavaScriptReplyProxy,
        val text: String,
        val language: String,
        val voice: String,
        val rate: Float,
        val pitch: Float,
        val volume: Float
    ) {
        var chunkStart = 0
        var chunkEnd = 0
    }

    private val main = Handler(Looper.getMainLooper())
    private var scriptHandler: ScriptHandler? = null
    private var engine: TextToSpeech? = null
    private var ready = false
    private var unavailable = false
    private var released = false
    private var voiceRequest: JavaScriptReplyProxy? = null
    private var speech: Speech? = null
    private var chunkId = 0L

    private val progress = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) = onChunk(utteranceId) { current ->
            if (current.chunkStart == 0) send(current.reply, "start", current.id)
        }

        override fun onRangeStart(utteranceId: String, start: Int, end: Int, frame: Int) =
            onChunk(utteranceId) { current ->
                val index = (current.chunkStart + start).coerceIn(current.chunkStart, current.chunkEnd)
                val boundary = (current.chunkStart + end).coerceIn(index, current.chunkEnd)
                send(current.reply, "boundary", current.id) {
                    put("charIndex", index)
                    put("charLength", boundary - index)
                }
            }

        override fun onDone(utteranceId: String) = onChunk(utteranceId) { current ->
            if (current.chunkEnd == current.text.length) {
                speech = null
                send(current.reply, "end", current.id)
            } else {
                current.chunkStart = current.chunkEnd
                send(current.reply, "progress", current.id) { put("charIndex", current.chunkStart) }
                playChunk(checkNotNull(engine), current)
            }
        }

        @Deprecated("Android TTS compatibility")
        override fun onError(utteranceId: String) = onError(utteranceId, TextToSpeech.ERROR)

        override fun onError(utteranceId: String, errorCode: Int) = onChunk(utteranceId) { current ->
            fail(current, when (errorCode) {
                TextToSpeech.ERROR_NETWORK, TextToSpeech.ERROR_NETWORK_TIMEOUT -> "network"
                TextToSpeech.ERROR_OUTPUT -> "audio-busy"
                TextToSpeech.ERROR_NOT_INSTALLED_YET -> "language-unavailable"
                TextToSpeech.ERROR_INVALID_REQUEST -> "invalid-argument"
                TextToSpeech.ERROR_SERVICE -> "synthesis-unavailable"
                else -> "synthesis-failed"
            })
        }
    }

    fun onPageStarted(webView: WebView) {
        voiceRequest = null
        stop()
        injectFallback(webView)
    }

    fun injectFallback(webView: WebView) {
        if (scriptHandler == null) webView.evaluateJavascript(WEB_SPEECH_SYNTHESIS_SCRIPT, null)
    }

    fun release(webView: WebView) {
        released = true
        voiceRequest = null
        stop()
        engine?.shutdown()
        engine = null
        runCatching { scriptHandler?.remove() }
            .onFailure { AppLogger.w(TAG, "Could not remove Web Speech script", it) }
        scriptHandler = null
        runCatching { WebViewCompat.removeWebMessageListener(webView, CHANNEL) }
            .onFailure { AppLogger.w(TAG, "Could not remove Web Speech listener", it) }
    }

    private fun receive(data: String?, reply: JavaScriptReplyProxy) {
        if (released || data == null || data.length > MAX_MESSAGE_LENGTH) return
        val message = try {
            JSONObject(data)
        } catch (_: JSONException) {
            return
        }
        when (message.optString("action")) {
            "voices" -> {
                voiceRequest = reply
                withEngine()
            }
            "speak" -> {
                val id = message.optString("id")
                if (id.isEmpty() || id.length > 32) return
                val text = message.optString("text")
                if (text.length > MAX_TEXT_LENGTH) {
                    send(reply, "error", id) { put("error", "text-too-long") }
                    return
                }
                stop()
                speech = Speech(
                    id, reply, text,
                    message.optString("lang").take(128),
                    message.optString("voice").take(512),
                    message.number("rate", 1f, 0.1f, 10f),
                    message.number("pitch", 1f, 0f, 2f),
                    message.number("volume", 1f, 0f, 1f)
                )
                withEngine()
            }
            "stop" -> {
                val current = speech ?: return
                if (current.reply === reply && current.id == message.optString("id")) stop()
            }
        }
    }

    private fun withEngine() {
        when {
            ready -> dispatch()
            unavailable -> reportUnavailable()
            engine == null -> {
                try {
                    engine = TextToSpeech(context) { status -> main.post { onInitialized(status) } }
                } catch (error: RuntimeException) {
                    AppLogger.w(TAG, "Could not initialize Android text-to-speech", error)
                    onInitialized(TextToSpeech.ERROR)
                }
            }
        }
    }

    private fun onInitialized(status: Int) {
        if (released || unavailable || ready) return
        val tts = engine
        if (status == TextToSpeech.SUCCESS && tts != null &&
            tts.setOnUtteranceProgressListener(progress) == TextToSpeech.SUCCESS
        ) {
            ready = true
            dispatch()
        } else {
            tts?.shutdown()
            engine = null
            unavailable = true
            reportUnavailable()
        }
    }

    private fun reportUnavailable() {
        voiceRequest?.let { send(it, "voices") { put("voices", JSONArray()) } }
        voiceRequest = null
        speech?.let { fail(it, "synthesis-unavailable") }
    }

    private fun dispatch() {
        val tts = checkNotNull(engine)
        voiceRequest?.let { reply ->
            voiceRequest = null
            val defaultVoice = tts.defaultVoice?.name
            val voices = JSONArray()
            tts.voices.orEmpty().sortedBy { it.name }.forEach { voice ->
                voices.put(JSONObject().apply {
                    put("voiceURI", voice.name)
                    put("name", voice.name)
                    put("lang", voice.locale.toLanguageTag())
                    put("localService", !voice.isNetworkConnectionRequired)
                    put("default", voice.name == defaultVoice)
                })
            }
            send(reply, "voices") { put("voices", voices) }
        }
        speech?.takeIf { it.chunkEnd == 0 }?.let { current -> start(tts, current) }
    }

    private fun start(tts: TextToSpeech, current: Speech) {
        if (current.text.isEmpty()) {
            speech = null
            send(current.reply, "start", current.id)
            send(current.reply, "end", current.id)
            return
        }
        if (current.voice.isNotEmpty()) {
            val voice = tts.voices.orEmpty().firstOrNull { it.name == current.voice }
            if (voice == null || tts.setVoice(voice) == TextToSpeech.ERROR) {
                fail(current, "voice-unavailable")
                return
            }
        } else if (current.language.isNotEmpty()) {
            val locale = Locale.forLanguageTag(current.language)
            if (locale.language.isEmpty() || tts.setLanguage(locale) < TextToSpeech.LANG_AVAILABLE) {
                fail(current, "language-unavailable")
                return
            }
        } else {
            tts.defaultVoice?.let { tts.setVoice(it) }
        }
        if (tts.setSpeechRate(current.rate) != TextToSpeech.SUCCESS ||
            tts.setPitch(current.pitch.coerceAtLeast(0.1f)) != TextToSpeech.SUCCESS
        ) {
            fail(current, "synthesis-failed")
            return
        }
        playChunk(tts, current)
    }

    private fun playChunk(tts: TextToSpeech, current: Speech) {
        current.chunkEnd = speechChunkEnd(current.text, current.chunkStart, TextToSpeech.getMaxSpeechInputLength())
        val result = tts.speak(
            current.text.substring(current.chunkStart, current.chunkEnd),
            TextToSpeech.QUEUE_FLUSH,
            Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, current.volume) },
            (++chunkId).toString()
        )
        if (result != TextToSpeech.SUCCESS) fail(current, "synthesis-failed")
    }

    private fun onChunk(id: String, action: (Speech) -> Unit) {
        main.post {
            val current = speech ?: return@post
            if (id == chunkId.toString()) action(current)
        }
    }

    private fun stop() {
        val current = speech ?: return
        speech = null
        if (current.chunkEnd > 0) engine?.stop()
    }

    private fun fail(current: Speech, code: String) {
        speech = null
        send(current.reply, "error", current.id) { put("error", code) }
    }

    private fun send(
        reply: JavaScriptReplyProxy,
        event: String,
        id: String? = null,
        fields: JSONObject.() -> Unit = {}
    ) {
        reply.postMessage(JSONObject().apply {
            put("event", event)
            if (id != null) put("id", id)
            fields()
        }.toString())
    }

    private fun JSONObject.number(name: String, default: Float, min: Float, max: Float): Float =
        optDouble(name, default.toDouble()).toFloat().takeIf { it.isFinite() }?.coerceIn(min, max) ?: default

    companion object {
        private const val TAG = "WebSpeechSynthesis"
        private const val CHANNEL = "WtaSpeechSynthesis"
        private const val MAX_TEXT_LENGTH = 32_767
        private const val MAX_MESSAGE_LENGTH = 210_000
        private val origins = setOf("*")

        fun install(webView: WebView): WebSpeechSynthesisBridge? {
            if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) return null
            val bridge = WebSpeechSynthesisBridge(webView.context.applicationContext)
            WebViewCompat.addWebMessageListener(webView, CHANNEL, origins) { _, message, _, isMainFrame, reply ->
                if (isMainFrame) bridge.receive(message.data, reply)
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                bridge.scriptHandler = runCatching {
                    WebViewCompat.addDocumentStartJavaScript(webView, WEB_SPEECH_SYNTHESIS_SCRIPT, origins)
                }.onFailure {
                    AppLogger.w(TAG, "Web Speech document-start registration failed", it)
                }.getOrNull()
            }
            return bridge
        }
    }
}

internal fun speechChunkEnd(text: String, start: Int, limit: Int): Int {
    val end = (start + limit).coerceAtMost(text.length)
    if (end == text.length) return end
    val split = text.lastIndexOf(' ', end - 1)
    if (split > start + limit / 2) return split + 1
    return if (text[end].isLowSurrogate() && text[end - 1].isHighSurrogate()) end - 1 else end
}
