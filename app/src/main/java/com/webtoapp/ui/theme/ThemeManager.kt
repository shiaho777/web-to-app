package com.webtoapp.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.webtoapp.core.i18n.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

@SuppressLint("StaticFieldLeak")
class ThemeManager(private val context: Context) {

    @Volatile
    private var cachedDarkMode: DarkModeSettings? = null

    val currentDarkMode: DarkModeSettings
        get() = cachedDarkMode ?: run {
            // Main-safe: never block UI thread on DataStore. Flow value is kept
            // fresh by Eagerly-started stateIn; export path (IO thread) still gets
            // the exact value via the blocking read below.
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                darkModeFlow.value.also { cachedDarkMode = it }
            } else {
                readDarkModeBlocking().also { cachedDarkMode = it }
            }
        }

    private fun readDarkModeBlocking(): DarkModeSettings = try {
        kotlinx.coroutines.runBlocking {
            val prefs = context.themeDataStore.data.first()
            val modeName = prefs[KEY_DARK_MODE] ?: DarkModeSettings.SYSTEM.name
            try {
                DarkModeSettings.valueOf(modeName)
            } catch (e: Exception) {
                DarkModeSettings.SYSTEM
            }
        }
    } catch (e: Exception) {
        darkModeFlow.value
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        private val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
        private val KEY_ACCENT = stringPreferencesKey("accent_color")
        private val KEY_ENABLE_ANIMATIONS = booleanPreferencesKey("enable_animations")
        private val KEY_ENABLE_PARTICLES = booleanPreferencesKey("enable_particles")
        private val KEY_ENABLE_HAPTICS = booleanPreferencesKey("enable_haptics")
        private val KEY_ENABLE_SOUND = booleanPreferencesKey("enable_sound")
        private val KEY_ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        private val KEY_FONT_SIZE = stringPreferencesKey("font_size")
        private val KEY_CORNER_STYLE = stringPreferencesKey("corner_style")

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

    enum class FontSize(val scale: Float) {
        SMALL(0.9f),
        STANDARD(1.0f),
        LARGE(1.15f),
        EXTRA_LARGE(1.3f);

        fun getDisplayName(): String = when (this) {
            SMALL -> Strings.fontSizeSmall
            STANDARD -> Strings.fontSizeStandard
            LARGE -> Strings.fontSizeLarge
            EXTRA_LARGE -> Strings.fontSizeXLarge
        }
    }

    enum class CornerStyle(val scale: Float) {
        SHARP(0.6f),
        STANDARD(1.0f),
        ROUNDED(1.5f);

        fun getDisplayName(): String = when (this) {
            SHARP -> Strings.cornerSharp
            STANDARD -> Strings.cornerStandard
            ROUNDED -> Strings.cornerRounded
        }
    }

    val themeTypeFlow: StateFlow<AppThemeType> = kotlinx.coroutines.flow.MutableStateFlow(AppThemeType.KIMI_NO_NAWA)

    val darkModeFlow: StateFlow<DarkModeSettings> = context.themeDataStore.data.map { prefs ->
        val modeName = prefs[KEY_DARK_MODE] ?: DarkModeSettings.SYSTEM.name
        try {
            DarkModeSettings.valueOf(modeName)
        } catch (e: Exception) {
            DarkModeSettings.SYSTEM
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = DarkModeSettings.SYSTEM
    )

    val accentFlow: StateFlow<AppAccent> = context.themeDataStore.data.map { prefs ->
        val accentName = prefs[KEY_ACCENT] ?: AppAccent.DEFAULT.name
        try {
            AppAccent.valueOf(accentName)
        } catch (e: Exception) {
            AppAccent.DEFAULT
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AppAccent.DEFAULT
    )

    val enableAnimationsFlow: StateFlow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_ENABLE_ANIMATIONS] ?: true
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val enableParticlesFlow: StateFlow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_ENABLE_PARTICLES] ?: true
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val enableHapticsFlow: StateFlow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_ENABLE_HAPTICS] ?: true
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val enableSoundFlow: StateFlow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_ENABLE_SOUND] ?: true
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    val animationSpeedFlow: StateFlow<AnimationSpeed> = context.themeDataStore.data.map { prefs ->
        val speedName = prefs[KEY_ANIMATION_SPEED] ?: AnimationSpeed.NORMAL.name
        try {
            AnimationSpeed.valueOf(speedName)
        } catch (e: Exception) {
            AnimationSpeed.NORMAL
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AnimationSpeed.NORMAL
    )

    suspend fun setThemeType(type: AppThemeType) {

    }

    suspend fun setDarkMode(mode: DarkModeSettings) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = mode.name
        }
        cachedDarkMode = mode
    }

    suspend fun setAccent(accent: AppAccent) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_ACCENT] = accent.name
        }
    }

    val fontSizeFlow: StateFlow<FontSize> = context.themeDataStore.data.map { prefs ->
        val name = prefs[KEY_FONT_SIZE] ?: FontSize.STANDARD.name
        try {
            FontSize.valueOf(name)
        } catch (e: Exception) {
            FontSize.STANDARD
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = FontSize.STANDARD
    )

    val cornerStyleFlow: StateFlow<CornerStyle> = context.themeDataStore.data.map { prefs ->
        val name = prefs[KEY_CORNER_STYLE] ?: CornerStyle.STANDARD.name
        try {
            CornerStyle.valueOf(name)
        } catch (e: Exception) {
            CornerStyle.STANDARD
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = CornerStyle.STANDARD
    )

    suspend fun setFontSize(size: FontSize) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_FONT_SIZE] = size.name
        }
    }

    suspend fun setCornerStyle(style: CornerStyle) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_CORNER_STYLE] = style.name
        }
    }

    suspend fun setEnableAnimations(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_ENABLE_ANIMATIONS] = enabled
        }
    }

    suspend fun setEnableParticles(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_ENABLE_PARTICLES] = enabled
        }
    }

    suspend fun setEnableHaptics(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_ENABLE_HAPTICS] = enabled
        }
    }

    suspend fun setEnableSound(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_ENABLE_SOUND] = enabled
        }
    }

    suspend fun setAnimationSpeed(speed: AnimationSpeed) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_ANIMATION_SPEED] = speed.name
        }
    }
}
