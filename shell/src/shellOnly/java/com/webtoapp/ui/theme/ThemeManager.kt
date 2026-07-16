package com.webtoapp.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import com.webtoapp.core.i18n.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("StaticFieldLeak")
class ThemeManager(private val context: Context) {

    @Volatile
    private var cachedDarkMode: DarkModeSettings = DarkModeSettings.SYSTEM

    val currentDarkMode: DarkModeSettings
        get() = cachedDarkMode

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    enum class DarkModeSettings {
        SYSTEM,
        LIGHT,
        DARK;

        fun getDisplayName(): String = when (this) {
            SYSTEM -> Strings.followSystem
            LIGHT -> Strings.alwaysLight
            DARK -> Strings.alwaysDark
        }
    }

    enum class AnimationSpeed(val multiplier: Float) {
        SLOW(1.5f),
        NORMAL(1.0f),
        FAST(0.7f),
        INSTANT(0.3f);

        fun getDisplayName(): String = when (this) {
            SLOW -> Strings.speedSlow
            NORMAL -> Strings.speedNormal
            FAST -> Strings.speedFast
            INSTANT -> Strings.speedInstant
        }
    }

    val themeTypeFlow: StateFlow<AppThemeType> = MutableStateFlow(AppThemeType.KIMI_NO_NAWA)
    val darkModeFlow: StateFlow<DarkModeSettings> = MutableStateFlow(DarkModeSettings.SYSTEM)
    val enableAnimationsFlow: StateFlow<Boolean> = MutableStateFlow(true)
    val enableParticlesFlow: StateFlow<Boolean> = MutableStateFlow(true)
    val enableHapticsFlow: StateFlow<Boolean> = MutableStateFlow(true)
    val enableSoundFlow: StateFlow<Boolean> = MutableStateFlow(true)
    val animationSpeedFlow: StateFlow<AnimationSpeed> = MutableStateFlow(AnimationSpeed.NORMAL)

    suspend fun setThemeType(type: AppThemeType) {
    }

    suspend fun setDarkMode(mode: DarkModeSettings) {
        cachedDarkMode = mode
        (darkModeFlow as MutableStateFlow).value = mode
    }

    suspend fun setEnableAnimations(enabled: Boolean) {
        (enableAnimationsFlow as MutableStateFlow).value = enabled
    }

    suspend fun setEnableParticles(enabled: Boolean) {
        (enableParticlesFlow as MutableStateFlow).value = enabled
    }

    suspend fun setEnableHaptics(enabled: Boolean) {
        (enableHapticsFlow as MutableStateFlow).value = enabled
    }

    suspend fun setEnableSound(enabled: Boolean) {
        (enableSoundFlow as MutableStateFlow).value = enabled
    }

    suspend fun setAnimationSpeed(speed: AnimationSpeed) {
        (animationSpeedFlow as MutableStateFlow).value = speed
    }
}
