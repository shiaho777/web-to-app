package com.webtoapp.core.webview

/**
 * Decides whether a WebView history entry restored from the Activity bundle is
 * worth keeping.
 *
 * `WebView.restoreState` happily restores whatever entry was current when the
 * process died, including entries that can never produce content again:
 *
 *  - `about:blank` / non-web schemes → restore renders a white page and every
 *    later `reload()` re-loads the same dead entry (#1030).
 *  - The page that launched an external app (WeChat/Alipay OAuth trampolines,
 *    `intent://` bounces). Those are usually one-shot: reloading them either
 *    shows an expired-token page or immediately bounces back out to the other
 *    app, which reads as an app that "froze white" after returning.
 *
 * For those cases the caller falls back to the start/resume URL instead.
 */
object WebViewRestoreGuard {

    fun isUsableRestoredUrl(url: String?, externalJumpUrl: String?): Boolean {
        if (url.isNullOrBlank() || url == "about:blank") return false
        val isWebContent = url.startsWith("http://") ||
            url.startsWith("https://") ||
            url.startsWith("file://")
        if (!isWebContent) return false
        return url != externalJumpUrl
    }
}
