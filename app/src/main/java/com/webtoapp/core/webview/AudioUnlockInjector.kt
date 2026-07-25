package com.webtoapp.core.webview

import android.webkit.WebView

/**
 * Unlocks the WebView audio session for apps that play audio *asynchronously* after a user gesture,
 * such as AI voice-chat apps (Grok / ChatGPT / Gemini voice mode).
 *
 * Problem: when [WebSettings.mediaPlaybackRequiresUserGesture] is true (the default, since
 * `mediaAutoplayEnabled` defaults to false), `audio.play()` is silently rejected unless it runs in
 * the synchronous call stack of a user gesture. Voice-chat apps receive the streamed audio from the
 * server *after* the gesture window has closed, so playback is dropped — even though the user
 * clearly intended to hear it.
 *
 * Fix: on the first real user gesture (pointerdown / keydown), synchronously "warm up" both audio
 * paths (Web Audio + HTMLAudioElement) with a near-silent buffer. That lifts the gesture gate for
 * the rest of the session, so later async `play()` calls succeed. This is the standard workaround
 * for autoplay-restricted voice-chat scenarios and does not bypass the policy for non-interactive
 * media: it still requires a genuine user gesture to fire.
 *
 * Shell-synced: injected unconditionally from [WebViewManager.onPageStarted], so it takes effect in
 * both host preview and exported APKs without any config field.
 */
object AudioUnlockInjector {

    internal const val UNLOCK_JS = """
        (function() {
            'use strict';
            if (window.__wta_audio_unlocked__) return;
            window.__wta_audio_unlocked__ = true;

            // 0.1s of silence, 8-bit mono PCM @ 8kHz, Base64. Tiny but valid so <audio>.play()
            // counts as real media playback and lifts the gesture gate for the session.
            var SILENCE_WAV = 'data:audio/wav;base64,UklGRiQAAABXQVZFZm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQAAAAA=';

            function unlock() {
                try {
                    var AudioCtx = window.AudioContext || window.webkitAudioContext;
                    if (AudioCtx) {
                        var ctx = new AudioCtx();
                        var osc = ctx.createOscillator();
                        var gain = ctx.createGain();
                        gain.gain.value = 0;
                        osc.connect(gain);
                        gain.connect(ctx.destination);
                        osc.start();
                        osc.stop(ctx.currentTime + 0.001);
                        if (ctx.state === 'suspended') { ctx.resume(); }
                    }
                } catch (e) {}
                try {
                    var a = new Audio(SILENCE_WAV);
                    a.muted = false;
                    a.volume = 0.0001;
                    var p = a.play();
                    if (p && typeof p.then === 'function') { p.catch(function () {}); }
                } catch (e) {}
                try {
                    window.removeEventListener('pointerdown', unlock, true);
                    window.removeEventListener('keydown', unlock, true);
                } catch (e) {}
            }

            try {
                window.addEventListener('pointerdown', unlock, true);
                window.addEventListener('keydown', unlock, true);
            } catch (e) {}
        })();
    """

    /** Injects the unlock bridge into [webView]. Safe to call on every page load (re-entrant). */
    fun inject(webView: WebView) {
        webView.evaluateJavascript(UNLOCK_JS, null)
    }
}
