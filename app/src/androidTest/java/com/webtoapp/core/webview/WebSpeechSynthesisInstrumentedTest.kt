package com.webtoapp.core.webview

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.webkit.WebViewFeature
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebSpeechSynthesisInstrumentedTest {
    @Test
    fun localHtmlCanDiscoverVoicesAndSpeakFromItsFirstScript() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        assumeTrue(WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER))
        assumeTrue(WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT))
        assumeTrue(context.packageManager.queryIntentServices(Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0).isNotEmpty())

        val page = File.createTempFile("web-speech", ".html", context.cacheDir).apply { writeText(SPEECH_TEST_PAGE) }
        val result = ArrayBlockingQueue<String>(1)
        var webView: WebView? = null
        var bridge: WebSpeechSynthesisBridge? = null
        try {
            instrumentation.runOnMainSync {
                val view = WebView(context)
                webView = view
                view.apply {
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            val text = message.message()
                            if (!text.startsWith("WtaSpeechTest:")) return false
                            result.offer(text.removePrefix("WtaSpeechTest:"))
                            return true
                        }
                    }
                    bridge = checkNotNull(WebSpeechSynthesisBridge.install(this))
                    loadUrl(Uri.fromFile(page).toString())
                }
            }
            assertEquals("voices,start:first,end:first,start:second,end:second", result.poll(30, TimeUnit.SECONDS))
        } finally {
            instrumentation.runOnMainSync {
                webView?.let {
                    bridge?.release(it)
                    it.destroy()
                }
            }
            page.delete()
        }
    }
}

internal val SPEECH_TEST_PAGE = """
    <!doctype html>
    <meta charset="utf-8">
    <title>Web Speech test</title>
    <h1 id="result">Waiting for speech</h1>
    <script>
    const events = [];
    function report(text) {
        document.getElementById('result').textContent = 'WtaSpeechTest:' + text;
        console.log('WtaSpeechTest:' + text);
    }
    if (typeof speechSynthesis === 'undefined' || typeof SpeechSynthesisUtterance !== 'function') {
        report('missing API in first script');
    } else {
        function speak(label, text) {
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.onstart = () => events.push('start:' + label);
            utterance.addEventListener('end', () => {
                events.push('end:' + label);
                if (label === 'second') report(events.join(','));
            });
            utterance.onerror = event => report(event.error + ':' + label);
            speechSynthesis.speak(utterance);
        }
        speechSynthesis.addEventListener('voiceschanged', () => {
            if (!speechSynthesis.getVoices().length) {
                report('empty voice list');
                return;
            }
            events.push('voices');
            speak('first', 'First sentence.');
            speak('second', 'Second sentence.');
        }, {once: true});
    }
    </script>
""".trimIndent()
