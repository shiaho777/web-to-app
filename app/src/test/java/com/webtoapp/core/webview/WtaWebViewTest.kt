package com.webtoapp.core.webview

import android.view.inputmethod.EditorInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WtaWebViewTest {

    @Test
    fun `stripEnterAction removes the send action`() {
        val stripped = WtaWebView.stripEnterAction(
            EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_FORCE_ASCII
        )
        assertThat(stripped and EditorInfo.IME_MASK_ACTION)
            .isEqualTo(EditorInfo.IME_ACTION_NONE)
        assertThat(stripped and EditorInfo.IME_FLAG_NO_ENTER_ACTION)
            .isEqualTo(EditorInfo.IME_FLAG_NO_ENTER_ACTION)
    }

    @Test
    fun `stripEnterAction preserves non-action flags`() {
        val flags = EditorInfo.IME_FLAG_NO_FULLSCREEN or
            EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        val stripped = WtaWebView.stripEnterAction(EditorInfo.IME_ACTION_GO or flags)
        assertThat(stripped and flags).isEqualTo(flags)
    }

    @Test
    fun `stripEnterAction leaves action-less options intact`() {
        val stripped = WtaWebView.stripEnterAction(EditorInfo.IME_ACTION_NONE)
        assertThat(stripped and EditorInfo.IME_MASK_ACTION)
            .isEqualTo(EditorInfo.IME_ACTION_NONE)
    }

    @Test
    fun `stripEnterAction on every send-style action`() {
        for (action in listOf(
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_NEXT
        )) {
            assertThat(WtaWebView.stripEnterAction(action) and EditorInfo.IME_MASK_ACTION)
                .isEqualTo(EditorInfo.IME_ACTION_NONE)
        }
    }
}
