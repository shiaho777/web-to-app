package com.webtoapp.ui.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.webtoapp.core.shell.GalleryShellConfig
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.SplashOrientation
import com.webtoapp.data.repository.WebAppRepository
import com.webtoapp.ui.animation.HostPreviewMotion
import com.webtoapp.ui.shell.GalleryFolderNavigator
import com.webtoapp.ui.shell.ShellGalleryPlayer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

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
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (galleryId <= 0L) putExtra(EXTRA_CONFIG, gson.toJson(config))
                putExtra(EXTRA_START_INDEX, startIndex)
                putExtra(EXTRA_GALLERY_ID, galleryId)
            }
            HostPreviewMotion.launch(context, intent)
        }
    }

    override fun finish() {
        super.finish()
        HostPreviewMotion.onFinish(this)
    }

    private val repository: WebAppRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HostPreviewMotion.install(this)

        val galleryId = intent.getLongExtra(EXTRA_GALLERY_ID, 0L)
        lifecycleScope.launch {
            val galleryConfig = if (galleryId > 0L) {
                repository.getWebApp(galleryId)?.galleryConfig
            } else {
                intent.getStringExtra(EXTRA_CONFIG)?.let { json ->
                    runCatching { gson.fromJson(json, GalleryConfig::class.java) }.getOrNull()
                }
            }
            if (galleryConfig == null || !galleryConfig.hasContent) finish()
            else showGallery(galleryConfig, galleryId)
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    private fun showGallery(galleryConfig: GalleryConfig, galleryId: Long) {
        val positionKey = galleryPositionKey(galleryId).takeIf {
            galleryId > 0 && galleryConfig.rememberPosition && !galleryConfig.useDeviceFolder
        }
        val savedIndex = positionKey?.let {
            getSharedPreferences(GALLERY_POSITION_PREFS, MODE_PRIVATE).getInt(it, -1)
        } ?: -1
        val startIndex = if (savedIndex >= 0) savedIndex else intent.getIntExtra(EXTRA_START_INDEX, 0)

        requestedOrientation = when (galleryConfig.orientation) {
            SplashOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            SplashOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setupImmersiveMode()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val isDark = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme() else lightColorScheme(),
                motionScheme = MotionScheme.expressive()
            ) {
                LaunchedEffect(Unit) {
                    hideSystemBars()
                }

                if (galleryConfig.useDeviceFolder) {
                    ShellGalleryPlayer(
                        galleryConfig = GalleryShellConfig(
                            useDeviceFolder = true,
                            loop = galleryConfig.loop,
                            backgroundColor = galleryConfig.backgroundColor,
                            showThumbnailBar = galleryConfig.showThumbnailBar,
                            showMediaInfo = galleryConfig.showMediaInfo,
                            enableAudio = galleryConfig.enableAudio,
                            videoAutoNext = galleryConfig.videoAutoNext
                        ),
                        onBack = { finish() },
                        positionKeySuffix = "preview/$galleryId"
                    )
                } else {
                    GalleryFolderNavigator(galleryConfig.categories, onBack = { finish() }) { categoryId, back ->
                        val scoped = if (categoryId == null) galleryConfig else galleryConfig.copy(
                            items = galleryConfig.getItemsByCategory(categoryId)
                        )
                        GalleryPlayerScreen(
                            config = scoped,
                            startIndex = if (categoryId == null) startIndex.coerceIn(0, scoped.items.lastIndex.coerceAtLeast(0)) else 0,
                            onBack = back,
                            positionKey = positionKey.takeIf { categoryId == null },
                            startInOverview = categoryId != null || savedIndex <= 0
                        )
                    }
                }
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
