package com.webtoapp.ui.gallery

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.SplashOrientation

class GalleryPlayerActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_CONFIG = "gallery_config"
        private const val EXTRA_START_INDEX = "start_index"
        private const val EXTRA_GALLERY_ID = "gallery_id"
        private val gson = com.webtoapp.util.GsonProvider.gson

        fun launch(
            context: Context,
            config: GalleryConfig,
            startIndex: Int = 0,
            galleryId: Long = 0L
        ) {
            val intent = Intent(context, GalleryPlayerActivity::class.java).apply {
                putExtra(EXTRA_CONFIG, gson.toJson(config))
                putExtra(EXTRA_START_INDEX, startIndex)
                putExtra(EXTRA_GALLERY_ID, galleryId)
            }
            context.startActivity(intent)
        }
    }

    private var config: GalleryConfig? = null
    private var startIndex: Int = 0
    private var positionKey: String? = null
    private var startInOverview: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configJson = intent.getStringExtra(EXTRA_CONFIG)
        config = configJson?.let {
            try {
                gson.fromJson(it, GalleryConfig::class.java)
            } catch (e: Exception) {
                null
            }
        }
        startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0)

        val galleryConfig = config
        if (galleryConfig == null || galleryConfig.items.isEmpty()) {
            finish()
            return
        }

        // rememberPosition: resume where the user left off (keyed per gallery).
        // A restored non-zero position opens the pager directly; otherwise the
        // overview (grid/list/timeline per defaultView) is the entry.
        val galleryId = intent.getLongExtra(EXTRA_GALLERY_ID, 0L)
        if (galleryConfig.rememberPosition && galleryId > 0) {
            val key = galleryPositionKey(galleryId)
            val saved = getSharedPreferences(GALLERY_POSITION_PREFS, MODE_PRIVATE).getInt(key, -1)
            if (saved >= 0) {
                startIndex = saved.coerceIn(0, galleryConfig.items.size - 1)
                positionKey = key
                startInOverview = startIndex > 0
            }
        }

        requestedOrientation = when (galleryConfig.orientation) {
            SplashOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            SplashOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setupImmersiveMode()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val isDark = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
            ) {
                LaunchedEffect(Unit) {
                    hideSystemBars()
                }

                GalleryPlayerScreen(
                    config = galleryConfig,
                    startIndex = startIndex.coerceIn(0, galleryConfig.items.size - 1),
                    onBack = { finish() },
                    positionKey = positionKey,
                    startInOverview = startInOverview
                )
            }
        }
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
