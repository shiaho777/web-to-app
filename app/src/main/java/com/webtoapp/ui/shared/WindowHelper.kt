package com.webtoapp.ui.shared

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Rect
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

    private val imeOcclusionListeners =
        java.util.concurrent.CopyOnWriteArrayList<(View, Int) -> Unit>()
    private val lastImeOcclusionPx =
        Collections.synchronizedMap(WeakHashMap<View, Int>())

    /**
     * Fires with the content view whose bottom padding was just set to the
     * keyboard height, and that height in px. 0 means the keyboard padding
     * was cleared. Listeners re-read their own window; the view argument
     * stops another activity's keyboard from changing this one.
     */
    fun addImeOcclusionListener(listener: (View, Int) -> Unit): () -> Unit {
        imeOcclusionListeners.add(listener)
        return { imeOcclusionListeners.remove(listener) }
    }

    private fun publishImeOcclusion(contentView: View, px: Int) {
        val value = px.coerceAtLeast(0)
        if (lastImeOcclusionPx[contentView] == value) return
        lastImeOcclusionPx[contentView] = value
        for (listener in imeOcclusionListeners) {
            listener(contentView, value)
        }
    }

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
        // Android 15 ignores window status-bar color and may lay a light scrim on top.
        // That scrim stays white when the page or theme goes dark, so the icons vanish.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isStatusBarContrastEnforced = false
        }
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

                // Classic path only: hiding the status bar via the legacy decor flag merely
                // *requests* a relayout. On some pre-30 ROMs (observed on Android 10 cutout
                // devices) the flag holds — the bar is gone — yet the decor never relayouts,
                // leaving the status-bar strip painted as bare window background; the #925
                // re-assert restores cleared flags but cannot force that layout pass.
                // FLAG_FULLSCREEN is a window attribute the system cannot clear, and it
                // resizes the decor to full height through relayoutWindow — the mechanism
                // fullscreen relied on before edge-to-edge existed. Every other branch
                // clears it so the window attribute cannot leak across config changes.
                if (enabled && !showStatusBar && classicKeyboardResize) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }

                if (enabled) {
                    if (!classicKeyboardResize) {
                        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    }
                    val shouldShowStatusBar = showStatusBar

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

                    // Issue #771: a shown status bar must stay (BEHAVIOR_DEFAULT).
                    // Transient bars auto-hide seconds after appearing, which is
                    // why "show status bar" never stayed until swiped back.
                    // Hidden paths keep the transient swipe-to-peek behavior.
                    val barsBehavior = if (shouldShowStatusBar) {
                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                    } else {
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }

                    if (hideNavBar) {
                        controller.hide(WindowInsetsCompat.Type.navigationBars())
                    } else {
                        controller.show(WindowInsetsCompat.Type.navigationBars())
                    }
                    controller.systemBarsBehavior = barsBehavior
                } else {
                    decorFitsSystemWindows = classicKeyboardResize
                    WindowCompat.setDecorFitsSystemWindows(activity.window, classicKeyboardResize)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                    // Non-fullscreen bars are permanently shown chrome: default
                    // behavior, never transient auto-hide (issue #771).
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
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
                    // One fixed delay is a race — the clear lands whenever the hide
                    // animation finishes, which varies by device — so re-check a few
                    // times over ~1.6s. Each pass is a no-op while flags still hold.
                    listOf(350L, 900L, 1600L).forEach { delayMs ->
                        activity.window.decorView.postDelayed({
                            try {
                                reAssertImmersiveFlags(activity.window, hideNavBar, !showStatusBar)
                            } catch (e: Exception) {
                                AppLogger.w(tag, "re-assert immersive flags failed", e)
                            }
                        }, delayMs)
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.w(tag, "applyImmersiveFullscreen failed", e)
        }
    }

    /**
     * Re-set the hidden-bar bits the system dropped. [hideNavBar] and [hideStatusBar]
     * mirror the caller's bar choices so a config that keeps a bar visible is not
     * force-hidden here.
     */
    @Suppress("DEPRECATION")
    private fun reAssertImmersiveFlags(window: Window, hideNavBar: Boolean, hideStatusBar: Boolean) {
        val decorView = window.decorView
        val target = decorView.systemUiVisibility
        val required = (if (hideStatusBar) View.SYSTEM_UI_FLAG_FULLSCREEN else 0) or
            (if (hideNavBar) View.SYSTEM_UI_FLAG_HIDE_NAVIGATION else 0)
        if (required != 0 && (target and required) != required) {
            decorView.systemUiVisibility = target or required or
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        // The window attribute cannot be cleared by the system, but re-add it
        // defensively in case an OEM rewrites window flags on a config change.
        if (hideStatusBar &&
            window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == 0) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
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

    /**
     * Runtime truth for "this window does not draw behind the status bar and
     * [androidx.compose.foundation.layout.WindowInsets.statusBars] cannot be used to size a
     * status-bar band": on the classic pre-API-30 resize path the decor fits system windows,
     * the insets value is 0 whether or not a bar is actually shown, and nothing renders behind
     * the bar area. Compose callers must not fall back to a made-up 24dp band there (issue
     * #683: fullscreen + "show status bar" left a blank strip because the content padding and
     * the overlay each guessed an independent height).
     */
    fun isClassicSystemBarsWindow(activity: Activity): Boolean =
        isClassicKeyboardResizeWindow(activity.window)

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
                    installManualImePadding(contentView)
                    AppLogger.d(tag, "键盘模式: RESIZE (边到边，只滚动被挡住的内容并裁到键盘上沿，结束时不改高度)")
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

    private fun installManualImePadding(contentView: View) {
        if (contentView in manualImeInstalled) {
            ViewCompat.requestApplyInsets(contentView)
            return
        }
        manualImeInstalled.add(contentView)

        var imeAnimating = false
        val follow = ImeAnimationFollow()
        val documentScroll = ImeDocumentScroll()

        fun applyFrame(frame: ImeFrame) {
            // Never leave a pan or a bottom padding in place. Padding shrinks
            // the WebView at the moment the field meets the keyboard, and
            // Chromium then applies the window IME inset again, so the field
            // flashes. Translation does the same to fixed elements (#1398).
            if (contentView.translationY != 0f) contentView.translationY = 0f
            if (contentView.paddingBottom != 0) {
                contentView.setPadding(
                    contentView.paddingLeft,
                    contentView.paddingTop,
                    contentView.paddingRight,
                    0
                )
                publishImeOcclusion(contentView, 0)
            }
            val clip = frame.clipBottom
            if (clip != null && clip > 0 && contentView.height > 0 && contentView.width > 0) {
                val clipBottom = (contentView.height - clip).coerceAtLeast(0)
                contentView.clipBounds = Rect(0, 0, contentView.width, clipBottom)
            } else if (clip == 0 && contentView.clipBounds != null) {
                contentView.clipBounds = null
            }
        }

        var latestImeBottom = 0

        fun beginDocumentScroll() {
            documentScroll.begin()
        }

        fun applyDocumentScroll(webView: android.webkit.WebView, imeBottom: Int) {
            if (imeBottom <= 0 || webView.height <= 0) return
            val web = IntArray(2)
            val content = IntArray(2)
            webView.getLocationInWindow(web)
            contentView.getLocationInWindow(content)
            val overlap = keyboardOverlapPx(
                web[1], webView.height, content[1], contentView.height, imeBottom,
            )
            val delta = documentScroll.scrollDelta(webView.height, overlap)
            if (delta > 0) webView.scrollBy(0, delta)
        }

        fun scrollCoveredField(imeBottom: Int) {
            latestImeBottom = imeBottom
            val webView = findWebViewInHierarchy(contentView) ?: return
            if (webView.height <= 0 || imeBottom <= 0) return
            val generation = documentScroll.requestSnapshot()
            if (generation != null) {
                val heightPx = webView.height
                webView.evaluateJavascript(FOCUSED_FIELD_SNAPSHOT_JS) { raw ->
                    val snap = parseFocusedFieldSnapshot(raw)
                    if (snap == null) {
                        documentScroll.snapshotMissed(generation)
                        return@evaluateJavascript
                    }
                    documentScroll.capture(
                        snap.topCss,
                        snap.bottomCss,
                        snap.viewportHeightCss,
                        heightPx,
                        generation,
                    )
                    // The rect arrives after this frame. Catch up immediately
                    // instead of waiting for the next inset tick.
                    webView.post {
                        if (webView.isAttachedToWindow) applyDocumentScroll(webView, latestImeBottom)
                    }
                }
            }
            applyDocumentScroll(webView, imeBottom)
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            contentView,
            object : WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
            ) {
                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
                        imeAnimating = true
                        follow.onPrepare()
                        beginDocumentScroll()
                    }
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    if (runningAnimations.any { it.typeMask and WindowInsetsCompat.Type.ime() != 0 }) {
                        if (!imeAnimating) {
                            imeAnimating = true
                            follow.onPrepare()
                            beginDocumentScroll()
                        }
                        val shown = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        val frame = follow.onFrame(shown)
                        applyFrame(frame)
                        if (frame.scrollDocument) scrollCoveredField(shown)
                    }
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if (animation.typeMask and WindowInsetsCompat.Type.ime() == 0) return
                    imeAnimating = false

                    val rootInsets = ViewCompat.getRootWindowInsets(contentView)
                    val imeVisible = rootInsets?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
                    val imeBottom = rootInsets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
                    val finalBottom = if (imeVisible) imeBottom else 0
                    // Last step of the same scroll, still in the unresized
                    // coordinate space. Do not scrollIntoView afterwards:
                    // the field is already on the keyboard, and a second
                    // scroll is the flash.
                    if (finalBottom > follow.settledBottom) scrollCoveredField(finalBottom)
                    documentScroll.end()
                    applyFrame(follow.onEnd(finalBottom))
                }
            }
        )

        ViewCompat.setOnApplyWindowInsetsListener(contentView) { _, windowInsets ->
            val imeBottom = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            if (imeAnimating) {
                // The dispatch during an animation carries the final inset. Keep it
                // for direction, and let onProgress move the page without a layout.
                follow.onTarget(imeBottom)
            } else {
                applyFrame(follow.jumpTo(imeBottom))
            }
            windowInsets
        }

        ViewCompat.requestApplyInsets(contentView)
    }

    private fun clearImePadding(contentView: View) {
        manualImeInstalled.remove(contentView)
        ViewCompat.setWindowInsetsAnimationCallback(contentView, null)
        ViewCompat.setOnApplyWindowInsetsListener(contentView, null)
        contentView.translationY = 0f
        contentView.clipBounds = null
        if (contentView.paddingBottom != 0) {
            contentView.setPadding(
                contentView.paddingLeft,
                contentView.paddingTop,
                contentView.paddingRight,
                0
            )
        }
        publishImeOcclusion(contentView, 0)
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
