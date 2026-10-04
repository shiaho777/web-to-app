package com.webtoapp.ui.shared

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

/**
 * Fullscreen content padding is a gap at the physical screen edge (#1172).
 * The keyboard covers that edge, so the gap must not be added again above it.
 */
object ScreenEdgePadding {
    fun effectiveBottomPx(configuredPx: Int, imeOcclusionPx: Int): Int {
        if (configuredPx <= 0) return 0
        return (configuredPx - imeOcclusionPx.coerceAtLeast(0)).coerceAtLeast(0)
    }

    /**
     * How many pixels of the screen bottom the keyboard covers.
     *
     * Edge-to-edge windows report this as the IME inset. A decor gap below the
     * window (split-screen, a nav bar) is not the keyboard there. The classic
     * resize path often reports a zero IME inset, so the gap under the decor
     * is used instead, minus the navigation bar that is already present while
     * the keyboard is closed.
     */
    fun imeOcclusionPx(
        imeInsetBottomPx: Int,
        belowDecorPx: Int,
        navigationBarPx: Int,
        classicResize: Boolean
    ): Int {
        val ime = imeInsetBottomPx.coerceAtLeast(0)
        if (!classicResize) return ime
        val fromDecor = (belowDecorPx.coerceAtLeast(0) - navigationBarPx.coerceAtLeast(0)).coerceAtLeast(0)
        return max(ime, fromDecor)
    }

    fun readImeOcclusionPx(view: View, classicResize: Boolean): Int {
        val rootInsets = ViewCompat.getRootWindowInsets(view)
        val ime = rootInsets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
        val nav = rootInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        val decor = view.rootView
        val loc = IntArray(2)
        decor.getLocationOnScreen(loc)
        val belowDecor = view.resources.displayMetrics.heightPixels - (loc[1] + decor.height)
        return imeOcclusionPx(ime, belowDecor, nav, classicResize)
    }

    fun viewBelongsTo(view: View, ancestor: View): Boolean {
        var current: View? = view
        while (current != null) {
            if (current === ancestor) return true
            current = current.parent as? View
        }
        return false
    }
}

/**
 * [configured] is the fullscreen bottom padding. While the keyboard covers
 * that band, the returned padding shrinks so the page sits on the keyboard
 * instead of leaving the band above it.
 */
@Composable
fun effectiveBottomContentPadding(configured: Dp): Dp {
    val occlusionPx = rememberImeScreenOcclusionPx()
    if (configured <= 0.dp) return 0.dp
    val density = LocalDensity.current
    val configuredPx = with(density) { configured.roundToPx() }
    return with(density) { ScreenEdgePadding.effectiveBottomPx(configuredPx, occlusionPx).toDp() }
}

@Composable
private fun rememberImeScreenOcclusionPx(): Int {
    val view = LocalView.current
    val context = LocalContext.current
    val classic = (context as? Activity)?.let { WindowHelper.isClassicSystemBarsWindow(it) } ?: false
    var occlusion by remember { mutableIntStateOf(0) }
    DisposableEffect(view, classic) {
        fun publish(source: View?, px: Int?) {
            val next = if (source != null && px != null && ScreenEdgePadding.viewBelongsTo(view, source)) {
                px
            } else {
                ScreenEdgePadding.readImeOcclusionPx(view, classic)
            }
            if (next != occlusion) occlusion = next
        }
        val removeListener = WindowHelper.addImeOcclusionListener { contentView, px ->
            publish(contentView, px)
        }
        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener { publish(null, null) }
        val observer = view.viewTreeObserver
        observer.addOnGlobalLayoutListener(layoutListener)
        publish(null, null)
        onDispose {
            removeListener()
            if (observer.isAlive) observer.removeOnGlobalLayoutListener(layoutListener)
            else view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        }
    }
    return occlusion
}
