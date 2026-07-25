package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Guards the contract of [AudioUnlockInjector.UNLOCK_JS] — the script must unlock both Web Audio and
 * HTMLAudioElement playback paths inside a real user-gesture handler, without bypassing the gesture
 * requirement (i.e. it must never call `play()` at the top level of the injected script).
 *
 * See issue #268: AI voice-chat apps play audio asynchronously after the gesture window closes, so
 * the unlock bridge warms up the audio session on the first genuine interaction.
 */
class AudioUnlockInjectorTest {

    private val script = AudioUnlockInjector.UNLOCK_JS

    @Test
    fun `script is guarded against re-injection`() {
        assertThat(script).contains("__wta_audio_unlocked__")
        // Early-return on re-entry must come before any listener registration.
        val guardIdx = script.indexOf("__wta_audio_unlocked__")
        val addListenerIdx = script.indexOf("addEventListener('pointerdown'")
        assertThat(addListenerIdx).isGreaterThan(0)
        assertThat(guardIdx).isLessThan(addListenerIdx)
    }

    @Test
    fun `script listens for real user gestures only`() {
        // Must hook a genuine gesture (not e.g. load/custom events), in capture phase so it fires
        // before the page's own handlers can stop propagation.
        assertThat(script).contains("addEventListener('pointerdown'")
        assertThat(script).contains("addEventListener('keydown'")
        assertThat(script).contains(", true)")
    }

    @Test
    fun `script warms up Web Audio path`() {
        assertThat(script).contains("AudioContext")
        assertThat(script).contains("webkitAudioContext")
        assertThat(script).contains("createOscillator")
        assertThat(script).contains("createGain")
    }

    @Test
    fun `script warms up HTMLAudioElement path with a valid silent media source`() {
        // <audio>.play() needs a real media resource to count as playback; a silent WAV data URL
        // is the smallest valid payload.
        assertThat(script).contains("new Audio(")
        assertThat(script).contains("data:audio/wav;base64,")
    }

    @Test
    fun `script never calls play at top level - only inside the gesture handler`() {
        // The unlock must run inside a user gesture, never automatically on load. Match the real
        // invocation `a.play()` (the assigned Audio element), which only exists inside the handler
        // body — not the `<audio>.play()` mention in the leading comment.
        val realCallIdx = script.indexOf("a.play()")
        assertThat(realCallIdx).isGreaterThan(0)
        val handlerBodyIdx = script.indexOf("function unlock()")
        assertThat(handlerBodyIdx).isGreaterThan(0)
        assertThat(realCallIdx).isGreaterThan(handlerBodyIdx)
    }

    @Test
    fun `script removes its listeners after unlocking to avoid repeated work`() {
        assertThat(script).contains("removeEventListener('pointerdown'")
        assertThat(script).contains("removeEventListener('keydown'")
    }

    @Test
    fun `script is an IIFE with strict mode`() {
        assertThat(script.trimStart().startsWith("(function()")).isTrue()
        assertThat(script).contains("'use strict'")
    }
}
