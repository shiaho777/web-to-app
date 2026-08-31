package com.webtoapp.ui.shared

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.KeyboardAdjustMode
import java.util.Collections
import java.util.WeakHashMap

object WindowHelper {

    private val manualImeInstalled =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<View, Boolean>()))

    fun applyStatusBarColor(
        activity: Activity,
        colorMode: String,
        customColor: String?,
        darkIcons: Boolean?,
        isDarkTheme: Boolean,
        backgroundAlpha: Float = 1f
    ) {
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        val alpha = backgroundAlpha.coerceIn(0f, 1f)
        // The classic resize window (below API 30, RESIZE mode) does not draw behind the
        // status bar, so a transparent bar would just show the window background.
        val colorMode = if (colorMode == "TRANSPARENT" && isClassicKeyboardResizeWindow(activity.window)) {
            "THEME"
        } else {
            colorMode
        }

        when (colorMode) {
            "TRANSPARENT" -> {
                activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
                val useDarkIcons = darkIcons ?: !isDarkTheme
                controller.isAppearanceLightStatusBars = useDarkIcons
            }
            "CUSTOM" -> {
                val baseColor = try {
                    android.graphics.Color.parseColor(customColor ?: "#FFFFFF")
                } catch (e: Exception) {
                    android.graphics.Color.WHITE
                }
                activity.window.statusBarColor = applyAlpha(baseColor, alpha)
                val useDarkIcons = darkIcons ?: isColorLight(baseColor)
                controller.isAppearanceLightStatusBars = useDarkIcons
            }
            else -> {
                if (isDarkTheme) {
                    val base = android.graphics.Color.parseColor("#1C1B1F")
                    activity.window.statusBarColor = applyAlpha(base, alpha)
                    controller.isAppearanceLightStatusBars = false
                } else {
                    val base = android.graphics.Color.parseColor("#FFFBFE")
                    activity.window.statusBarColor = applyAlpha(base, alpha)
                    controller.isAppearanceLightStatusBars = true
                }
            }
        }

        controller.isAppearanceLightNavigationBars = controller.isAppearanceLightStatusBars
    }

    private fun applyAlpha(color: Int, alpha: Float): Int {
        val a = (alpha.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
        return android.graphics.Color.argb(
            a,
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color)
        )
    }

    fun isColorLight(color: Int): Boolean {
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        return luminance > 0.5
    }

    fun applyImmersiveFullscreen(
        activity: Activity,
        enabled: Boolean,
        hideNavBar: Boolean = true,
        isDarkTheme: Boolean = false,
        showStatusBar: Boolean = false,
        forceHideSystemUi: Boolean = false,
        statusBarColorMode: String = "THEME",
        statusBarCustomColor: String? = null,
        statusBarDarkIcons: Boolean? = null,
        statusBarBgType: String = "COLOR",
        keyboardAdjustMode: KeyboardAdjustMode? = null,
        tag: String = "WindowHelper"
    ) {
        try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                activity.window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            // Below API 30 the platform has no native IME-inset dispatch: with the window laid
            // out edge-to-edge, the system neither resizes it for the keyboard nor reports IME
            // insets, leaving RESIZE mode with no working keyboard avoidance at all (issue #613;
            // the #634 softInputMode-only fix did not help because of these layout flags).
            // Those devices keep the decor fitting system windows and rely on the classic
            // SOFT_INPUT_ADJUST_RESIZE path instead. Nothing draws behind the system bars
            // there, so translucent status-bar styles degrade to solid theme colors.
            val classicKeyboardResize = isClassicKeyboardResize(keyboardAdjustMode)
            val effectiveStatusBarColorMode =
                if (classicKeyboardResize && statusBarColorMode == "TRANSPARENT") "THEME" else statusBarColorMode
            val effectiveStatusBarBgType =
                if (classicKeyboardResize && statusBarBgType == "IMAGE") "COLOR" else statusBarBgType

            WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
                var decorFitsSystemWindows = true
                if (enabled) {
                    if (!classicKeyboardResize) {
                        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    }
                    val shouldShowStatusBar = if (forceHideSystemUi) false else showStatusBar

                    if (shouldShowStatusBar) {
                        decorFitsSystemWindows = classicKeyboardResize
                        WindowCompat.setDecorFitsSystemWindows(activity.window, classicKeyboardResize)
                        controller.show(WindowInsetsCompat.Type.statusBars())

                        if (effectiveStatusBarBgType == "IMAGE") {
                            activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
                            val useDarkIcons = statusBarDarkIcons ?: !isDarkTheme
                            controller.isAppearanceLightStatusBars = useDarkIcons
                        } else {
                            when (effectiveStatusBarColorMode) {
                                "CUSTOM" -> {
                                    val color = try {
                                        android.graphics.Color.parseColor(statusBarCustomColor ?: "#000000")
                                    } catch (e: Exception) {
                                        android.graphics.Color.BLACK
                                    }
                                    activity.window.statusBarColor = color
                                    val useDarkIcons = statusBarDarkIcons ?: isColorLight(color)
                                    controller.isAppearanceLightStatusBars = useDarkIcons
                                }
                                "TRANSPARENT" -> {
                                    activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
                                    val useDarkIcons = statusBarDarkIcons ?: !isDarkTheme
                                    controller.isAppearanceLightStatusBars = useDarkIcons
                                }
                                else -> {
                                    if (isDarkTheme) {
                                        activity.window.statusBarColor = android.graphics.Color.parseColor("#1C1B1F")
                                        controller.isAppearanceLightStatusBars = false
                                    } else {
                                        activity.window.statusBarColor = android.graphics.Color.parseColor("#FFFBFE")
                                        controller.isAppearanceLightStatusBars = true
                                    }
                                }
                            }
                        }
                    } else {
                        decorFitsSystemWindows = classicKeyboardResize
                        WindowCompat.setDecorFitsSystemWindows(activity.window, classicKeyboardResize)
                        activity.window.statusBarColor = if (classicKeyboardResize) {
                            // nothing draws behind a hidden bar on the classic path
                            android.graphics.Color.parseColor(if (isDarkTheme) "#1C1B1F" else "#FFFBFE")
                        } else {
                            android.graphics.Color.TRANSPARENT
                        }
                        controller.hide(WindowInsetsCompat.Type.statusBars())
                    }

                    if (hideNavBar || forceHideSystemUi) {
                        controller.hide(WindowInsetsCompat.Type.navigationBars())
                        controller.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        controller.show(WindowInsetsCompat.Type.navigationBars())
                        controller.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    decorFitsSystemWindows = classicKeyboardResize
                    WindowCompat.setDecorFitsSystemWindows(activity.window, classicKeyboardResize)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    if (!classicKeyboardResize) {
                        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    }

                    if (effectiveStatusBarBgType == "IMAGE") {
                        activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
                        val useDarkIcons = statusBarDarkIcons ?: !isDarkTheme
                        controller.isAppearanceLightStatusBars = useDarkIcons
                        controller.isAppearanceLightNavigationBars = useDarkIcons
                    } else {
                        when (effectiveStatusBarColorMode) {
                            "CUSTOM" -> {
                                val color = try {
                                    android.graphics.Color.parseColor(statusBarCustomColor ?: "#000000")
                                } catch (e: Exception) {
                                    android.graphics.Color.BLACK
                                }
                                activity.window.statusBarColor = color
                                val useDarkIcons = statusBarDarkIcons ?: isColorLight(color)
                                controller.isAppearanceLightStatusBars = useDarkIcons
                                controller.isAppearanceLightNavigationBars = useDarkIcons
                            }
                            "TRANSPARENT" -> {
                                activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
                                val useDarkIcons = statusBarDarkIcons ?: !isDarkTheme
                                controller.isAppearanceLightStatusBars = useDarkIcons
                                controller.isAppearanceLightNavigationBars = useDarkIcons
                            }
                            else -> {
                                applyStatusBarColor(
                                    activity,
                                    effectiveStatusBarColorMode,
                                    statusBarCustomColor,
                                    statusBarDarkIcons,
                                    isDarkTheme
                                )
                            }
                        }
                    }
                    activity.window.decorView.post {
                        ViewCompat.requestApplyInsets(activity.window.decorView)
                    }
                }
                if (classicKeyboardResize) {
                    // Not edge-to-edge: keep the nav bar in sync with the solid status bar
                    // instead of leaving a default dark bar under light nav icons.
                    activity.window.navigationBarColor = activity.window.statusBarColor
                }
                applyKeyboardMode(activity, keyboardAdjustMode, tag, decorFitsSystemWindows)
                if (classicKeyboardResize && enabled) {
                    // Classic (below API 30, RESIZE) windows have no insets controller: hiding
                    // a bar sets the legacy SYSTEM_UI_FLAG_* bits, and the system clears the
                    // fullscreen flag again whenever the hidden bar finishes animating away
                    // (no ViewCompat.requestApplyInsets pass re-asserts it there). Without
                    // this re-assert the window background shows through as a white strip
                    // where the status bar was (2.5.5 fullscreen regression on Android 10).
                    activity.window.decorView.postDelayed({
                        try {
                            val target = activity.window.decorView.systemUiVisibility
                            val want = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            if ((target and (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)) !=
                                (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                            ) {
                                activity.window.decorView.systemUiVisibility = target or want
                            }
                        } catch (e: Exception) {
                            AppLogger.w(tag, "re-assert immersive flags failed", e)
                        }
                    }, 350)
                }
            }
        } catch (e: Exception) {
            AppLogger.w(tag, "applyImmersiveFullscreen failed", e)
        }
    }

    private fun isClassicKeyboardResize(keyboardAdjustMode: KeyboardAdjustMode?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return false
        return (keyboardAdjustMode ?: KeyboardAdjustMode.RESIZE) == KeyboardAdjustMode.RESIZE
    }

    /** True when the window runs the classic (non-edge-to-edge) resize path below API 30. */
    private fun isClassicKeyboardResizeWindow(window: Window): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return false
        return window.attributes.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE != 0
    }

    private fun applyKeyboardMode(
        activity: Activity,
        keyboardAdjustMode: KeyboardAdjustMode?,
        tag: String,
        decorFitsSystemWindows: Boolean
    ) {
        val contentView = activity.findViewById<View>(android.R.id.content) ?: return

        val mode = keyboardAdjustMode ?: KeyboardAdjustMode.RESIZE

        when (mode) {
            KeyboardAdjustMode.RESIZE -> {

                // Manual IME padding needs native IME-inset dispatch (API 30+). Below that,
                // applyImmersiveFullscreen keeps the window fitting system windows so the
                // system SOFT_INPUT_ADJUST_RESIZE path actually resizes for the keyboard and
                // the WebView scrolls the focused input into view on its own (issue #613).
                val useManualImePadding = !decorFitsSystemWindows &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

                if (!useManualImePadding) {
                    @Suppress("DEPRECATION")
                    activity.window.setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
                    )
                    clearImePadding(contentView)
                    AppLogger.d(tag, "键盘模式: RESIZE (系统调整)")
                } else {
                    @Suppress("DEPRECATION")
                    activity.window.setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
                    )
                    installManualImePadding(activity, contentView)
                    AppLogger.d(tag, "键盘模式: RESIZE (边到边 + 手动动画)")
                }
            }

            KeyboardAdjustMode.NOTHING -> {

                @Suppress("DEPRECATION")
                activity.window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
                )

                clearImePadding(contentView)

                AppLogger.d(tag, "键盘模式: NOTHING (覆盖)")
            }
        }
    }

    private fun installManualImePadding(activity: Activity, contentView: View) {
        if (contentView in manualImeInstalled) {
            ViewCompat.requestApplyInsets(contentView)
            return
        }
        manualImeInstalled.add(contentView)

        var imeAnimating = false
        var targetImeBottom = 0

        fun applyImeBottomPadding(bottom: Int) {
            if (contentView.paddingBottom != bottom) {
                contentView.setPadding(
                    contentView.paddingLeft,
                    contentView.paddingTop,
                    contentView.paddingRight,
                    bottom
                )
            }
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            contentView,
            object : WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
            ) {
                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
                        imeAnimating = true
                    }
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    applyImeBottomPadding(insets.getInsets(WindowInsetsCompat.Type.ime()).bottom)
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if (animation.typeMask and WindowInsetsCompat.Type.ime() == 0) return
                    imeAnimating = false

                    val rootInsets = ViewCompat.getRootWindowInsets(contentView)
                    val imeVisible = rootInsets?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
                    val imeBottom = rootInsets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
                    targetImeBottom = if (imeVisible) imeBottom else 0
                    applyImeBottomPadding(targetImeBottom)

                    if (imeVisible) {
                        checkAndScrollWebViewToFocusedInput(activity)
                    }
                }
            }
        )

        ViewCompat.setOnApplyWindowInsetsListener(contentView) { _, windowInsets ->
            if (!imeAnimating) {
                val imeBottom = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                targetImeBottom = imeBottom
                applyImeBottomPadding(imeBottom)
            }
            windowInsets
        }

        ViewCompat.requestApplyInsets(contentView)
    }

    private fun clearImePadding(contentView: View) {
        manualImeInstalled.remove(contentView)
        ViewCompat.setWindowInsetsAnimationCallback(contentView, null)
        ViewCompat.setOnApplyWindowInsetsListener(contentView, null)
        if (contentView.paddingBottom != 0) {
            contentView.setPadding(
                contentView.paddingLeft,
                contentView.paddingTop,
                contentView.paddingRight,
                0
            )
        }
    }

    private fun checkAndScrollWebViewToFocusedInput(activity: Activity) {
        val webView = findWebViewInHierarchy(activity.window.decorView) ?: return
        webView.post {
            try {
                webView.evaluateJavascript("""
                    (function() {
                        var el = document.activeElement;
                        if (!el || (el.tagName !== 'INPUT' && el.tagName !== 'TEXTAREA' && !el.isContentEditable)) {
                            return 'no_input';
                        }
                        var rect = el.getBoundingClientRect();
                        var viewportHeight = window.visualViewport ? window.visualViewport.height : window.innerHeight;
                        var viewportTop = window.visualViewport ? window.visualViewport.offsetTop : 0;
                        if (rect.top >= viewportTop && rect.bottom <= (viewportTop + viewportHeight)) {
                            return 'already_visible';
                        }
                        el.scrollIntoView({ block: 'center' });
                        return 'scrolled';
                    })();
                """.trimIndent(), null)
            } catch (e: Exception) {
                AppLogger.w("WindowHelper", "checkAndScrollWebViewToFocusedInput failed", e)
            }
        }
    }

    private fun scrollWebViewToFocusedInput(activity: Activity) {
        try {

            val webView = findWebViewInHierarchy(activity.window.decorView)
            webView?.evaluateJavascript("""
                (function() {
                    var el = document.activeElement;
                    if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.isContentEditable)) {
                        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                })();
            """.trimIndent(), null)
        } catch (e: Exception) {
            AppLogger.w("WindowHelper", "scrollWebViewToFocusedInput failed", e)
        }
    }

    private fun findWebViewInHierarchy(view: View): android.webkit.WebView? {
        if (view is android.webkit.WebView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findWebViewInHierarchy(view.getChildAt(i))
                if (result != null) return result
            }
        }
        return null
    }

    fun showCustomView(
        activity: Activity,
        view: View
    ): Int {
        val originalOrientation = activity.requestedOrientation

        // Only re-parent the view if it isn't already attached. System WebView hands us a
        // detached video surface (add it to the decorView); GeckoView fullscreen passes the
        // already-attached GeckoView itself, which must stay in its current container while we
        // change orientation and hide the system bars — re-parenting it throws
        // "The specified child already has a parent" (issue #298).
        if (view.parent == null) {
            val decorView = activity.window.decorView as FrameLayout
            decorView.addView(
                view,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        return originalOrientation
    }

    private const val FULLSCREEN_VIDEO_DETECT_JS = """
        (function() {
          try {
            var fe = document.fullscreenElement
              || document.webkitFullscreenElement
              || document.webkitCurrentFullScreenElement;
            if (fe) {
              if (fe.tagName === 'VIDEO') return 'video';
              if (fe.querySelector && fe.querySelector('video')) return 'video';
              return 'other';
            }
            var vids = document.getElementsByTagName('video');
            for (var i = 0; i < vids.length; i++) {
              var v = vids[i];
              if (!v.paused && !v.ended && v.readyState > 2) return 'video';
            }
            return 'none';
          } catch (e) {
            return 'none';
          }
        })();
    """

    fun applyFullscreenVideoOrientation(
        activity: Activity,
        webView: android.webkit.WebView?,
        fullscreenOrientation: com.webtoapp.data.model.FullscreenVideoOrientation =
            com.webtoapp.data.model.FullscreenVideoOrientation.AUTO_SENSOR_LANDSCAPE
    ) {
        val targetOrientation = when (fullscreenOrientation) {
            com.webtoapp.data.model.FullscreenVideoOrientation.AUTO_SENSOR_LANDSCAPE ->
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            com.webtoapp.data.model.FullscreenVideoOrientation.FORCE_LANDSCAPE ->
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            com.webtoapp.data.model.FullscreenVideoOrientation.KEEP_CURRENT -> {
                AppLogger.d("WindowHelper", "Fullscreen: keeping current orientation")
                return
            }
        }

        if (webView == null) {
            activity.requestedOrientation = targetOrientation
            AppLogger.d("WindowHelper", "Fullscreen orientation applied without detection (no WebView)")
            return
        }

        webView.evaluateJavascript(FULLSCREEN_VIDEO_DETECT_JS) { result ->
            val isVideo = result != null && result.contains("video")
            if (isVideo) {
                activity.requestedOrientation = targetOrientation
                AppLogger.d("WindowHelper", "Fullscreen video detected, orientation -> $targetOrientation")
            } else {
                AppLogger.d("WindowHelper", "Fullscreen content is not a video ($result), orientation kept")
            }
        }
    }

    fun hideCustomView(
        activity: Activity,
        view: View,
        callback: WebChromeClient.CustomViewCallback?,
        originalOrientation: Int
    ) {
        // Only remove the view if we were the ones who added it (to the decorView). A GeckoView
        // fullscreen view stays attached to its original container (see showCustomView), so there
        // is nothing to remove here.
        val decorView = activity.window.decorView as FrameLayout
        if (view.parent === decorView) {
            decorView.removeView(view)
        }
        callback?.onCustomViewHidden()
        activity.requestedOrientation = originalOrientation
    }
}
