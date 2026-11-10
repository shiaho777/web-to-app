package com.webtoapp.ui.shell

import androidx.compose.runtime.compositionLocalOf
import com.webtoapp.core.webview.StatusBarPageColorTracker

val LocalPageScrollTracker = compositionLocalOf<StatusBarPageColorTracker?> { null }
