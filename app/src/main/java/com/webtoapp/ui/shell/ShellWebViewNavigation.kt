package com.webtoapp.ui.shell

import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.webtoapp.core.engine.BrowserSurface

object ShellWebViewNavigation {

    fun goBackOrFinish(activity: AppCompatActivity, webView: WebView?) {
        goBackOrFinish(activity, webView, useJsHistoryBack = false)
    }

    fun goBackOrFinish(
        activity: AppCompatActivity,
        webView: WebView?,
        useJsHistoryBack: Boolean
    ) {
        val wv = webView ?: run {
            activity.finish()
            return
        }

        if (useJsHistoryBack) {
            goBackViaJsHistoryBack(activity, wv)
            return
        }
        goBackNative(activity, wv)
    }

    /**
     * Surface-aware back navigation. On the System WebView kernel this delegates to the
     * WebView resolver above (history heuristics + optional JS history.back). On GeckoView
     * there is no WebView handle and no JS-result channel (Gecko's evaluateJavascript cannot
     * return values), so back walks the engine's own history — previously this path fell
     * through to finish(), making the back button always exit generated Gecko apps.
     */
    fun goBackOrFinish(
        activity: AppCompatActivity,
        surface: BrowserSurface?,
        useJsHistoryBack: Boolean = false
    ) {
        val wv = surface?.webView
        if (wv != null) {
            goBackOrFinish(activity, wv, useJsHistoryBack)
            return
        }
        if (surface != null && surface.canGoBack()) {
            surface.goBack()
            return
        }
        activity.finish()
    }

    private fun goBackViaJsHistoryBack(activity: AppCompatActivity, webView: WebView) {
        // Prefer JS history.back() over the native WebView.goBack(): the JS
        // route runs through the page's own popstate/pageshow handlers and is
        // more likely to restore the previous DOM via bfcache instead of doing
        // a fresh load. If the page has nowhere to go back to, fall back to the
        // native resolver (which will finish the activity when at the bottom of
        // the history stack).
        val js = """(function(){
            try {
                if (history.length > 1 && window.location.href !== 'about:blank') {
                    history.back();
                    return 'back';
                }
            } catch (e) {}
            return 'none';
        })();""".trimIndent()
        webView.evaluateJavascript(js) { result ->
            if ("back" == result) return@evaluateJavascript
            goBackNative(activity, webView)
        }
    }

    private fun goBackNative(activity: AppCompatActivity, wv: WebView) {
        val list = wv.copyBackForwardList()
        val currentIndex = list.currentIndex
        val currentItem = list.getItemAtIndex(currentIndex)
        val previousItem = if (currentIndex > 0) list.getItemAtIndex(currentIndex - 1) else null
        val beforePreviousItem = if (currentIndex > 1) list.getItemAtIndex(currentIndex - 2) else null
        val currentUrls = listOf(
            currentItem?.url.orEmpty(),
            currentItem?.originalUrl.orEmpty(),
            wv.url.orEmpty(),
            wv.originalUrl.orEmpty()
        )
        val previousUrls = listOf(
            previousItem?.url.orEmpty(),
            previousItem?.originalUrl.orEmpty()
        )
        val beforePreviousUrls = listOf(
            beforePreviousItem?.url.orEmpty(),
            beforePreviousItem?.originalUrl.orEmpty()
        )

        when (resolveBackAction(wv.canGoBack(), currentIndex, currentUrls, previousUrls, beforePreviousUrls)) {
            BackAction.FINISH -> activity.finish()
            BackAction.GO_BACK -> wv.goBack()
            BackAction.SKIP_PREVIOUS -> wv.goBackOrForward(-2)
        }
    }

    internal fun shouldFinishInsteadOfBack(currentUrl: String, previousUrl: String): Boolean {
        return resolveBackAction(
            canGoBack = true,
            currentIndex = 1,
            currentUrls = listOf(currentUrl),
            previousUrls = listOf(previousUrl),
            beforePreviousUrls = emptyList()
        ) == BackAction.FINISH
    }

    internal fun resolveBackAction(
        canGoBack: Boolean,
        currentIndex: Int,
        currentUrls: List<String>,
        previousUrls: List<String>,
        beforePreviousUrls: List<String> = emptyList()
    ): BackAction {
        if (!canGoBack || currentIndex <= 0) return BackAction.FINISH

        val currentHasBlank = currentUrls.any(::isBlankPage)
        val currentHasError = currentUrls.any(::isGeneratedErrorPage)
        val currentHasLocal = currentUrls.any(::isLocalRuntimeUrl)
        val previousHasBlank = previousUrls.any(::isBlankPage)
        val previousHasError = previousUrls.any(::isGeneratedErrorPage)
        val previousHasLocal = previousUrls.any(::isLocalRuntimeUrl)
        val previousHasRemote = previousUrls.any(::isRemoteWebUrl)
        val beforePreviousHasRealPage = beforePreviousUrls.any(::isRealHistoryPage)

        if (currentHasError && previousHasRemote) {
            return if (beforePreviousHasRealPage && currentIndex > 1) {
                BackAction.SKIP_PREVIOUS
            } else {
                BackAction.FINISH
            }
        }

        val shouldFinish = previousHasBlank ||
            previousHasError ||
            currentHasBlank && (previousHasLocal || previousHasError) ||
            currentHasError && (previousHasLocal || previousHasBlank) ||
            currentHasLocal && previousHasError

        return if (shouldFinish) BackAction.FINISH else BackAction.GO_BACK
    }

    internal enum class BackAction {
        FINISH,
        GO_BACK,
        SKIP_PREVIOUS
    }

    private fun isBlankPage(url: String): Boolean {
        return url.isBlank() || url == "about:blank"
    }

    private fun isGeneratedErrorPage(url: String): Boolean {
        return url.startsWith("data:text/html", ignoreCase = true) ||
            url.startsWith("data:text/plain", ignoreCase = true)
    }

    private fun isLocalRuntimeUrl(url: String): Boolean {
        return url.startsWith("http://127.0.0.1:", ignoreCase = true) ||
            url.startsWith("http://localhost:", ignoreCase = true)
    }

    private fun isRemoteWebUrl(url: String): Boolean {
        return (url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)) &&
            !isLocalRuntimeUrl(url)
    }

    private fun isRealHistoryPage(url: String): Boolean {
        return url.isNotBlank() &&
            !isBlankPage(url) &&
            !isGeneratedErrorPage(url)
    }
}
