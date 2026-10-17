package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.NewWindowBehavior
import org.junit.Test

class ScriptWindowPolicyTest {

    @Test
    fun `scripted window open stays a real popup`() {
        assertThat(
            ScriptWindowPolicy.keepRealPopup(hitTestUrl = null, behavior = NewWindowBehavior.SAME_WINDOW)
        ).isTrue()
        assertThat(
            ScriptWindowPolicy.keepRealPopup(hitTestUrl = "", behavior = NewWindowBehavior.SAME_WINDOW)
        ).isTrue()
    }

    @Test
    fun `oauth link stays a real popup even in the same window mode`() {
        assertThat(
            ScriptWindowPolicy.keepRealPopup(
                hitTestUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=x",
                behavior = NewWindowBehavior.SAME_WINDOW
            )
        ).isTrue()
    }

    @Test
    fun `ordinary blank target stays in the same window`() {
        assertThat(
            ScriptWindowPolicy.keepRealPopup(
                hitTestUrl = "https://example.com/article",
                behavior = NewWindowBehavior.SAME_WINDOW
            )
        ).isFalse()
    }

    @Test
    fun `blocked windows are not kept`() {
        assertThat(
            ScriptWindowPolicy.keepRealPopup(hitTestUrl = null, behavior = NewWindowBehavior.BLOCK)
        ).isFalse()
    }

    @Test
    fun `popup mode keeps every new window`() {
        assertThat(
            ScriptWindowPolicy.keepRealPopup(
                hitTestUrl = "https://example.com/article",
                behavior = NewWindowBehavior.POPUP_WINDOW
            )
        ).isTrue()
    }
}
