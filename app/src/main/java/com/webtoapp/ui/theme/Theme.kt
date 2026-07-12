package com.webtoapp.ui.theme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val LocalAppTheme = staticCompositionLocalOf { AppThemes.Default }

data class AnimationSettings(
    val enabled: Boolean = true,
    val particlesEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val speedMultiplier: Float = 1f
)

val LocalAnimationSettings = staticCompositionLocalOf { AnimationSettings() }

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B5EFC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDAE1FF),
    onPrimaryContainer = Color(0xFF00174B),
    secondary = Color(0xFF555F71),
    onSecondary = Color.White,
    tertiary = Color(0xFF6F5675),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF1A1B21),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF1A1B21),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44464F),
    surfaceContainer = Color(0xFFEEEFF7),
    surfaceContainerLow = Color(0xFFF3F3FB),
    surfaceContainerHigh = Color(0xFFE8E8F0),
    surfaceContainerHighest = Color(0xFFE2E2EA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    outline = Color(0xFF757780),
    outlineVariant = Color(0xFFC5C6D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB2C5FF),
    onPrimary = Color(0xFF002A78),
    primaryContainer = Color(0xFF0040A8),
    onPrimaryContainer = Color(0xFFDAE1FF),
    secondary = Color(0xFFBDC7DC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    surfaceContainer = Color(0xFF1E1F25),
    surfaceContainerLow = Color(0xFF1A1B21),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33343A),
    surfaceContainerLowest = Color(0xFF0C0E13),
    outline = Color(0xFF8F909A),
    outlineVariant = Color(0xFF44464F)
)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun WebToAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    WebToAppTheme(darkTheme, dynamicColor) { _ ->
        content()
    }
}

@Composable
fun WebToAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable (isDarkTheme: Boolean) -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }

    val themeType by themeManager.themeTypeFlow.collectAsStateWithLifecycle()
    val darkModeSetting by themeManager.darkModeFlow.collectAsStateWithLifecycle()
    val enableAnimations by themeManager.enableAnimationsFlow.collectAsStateWithLifecycle()
    val enableParticles by themeManager.enableParticlesFlow.collectAsStateWithLifecycle()
    val enableHaptics by themeManager.enableHapticsFlow.collectAsStateWithLifecycle()
    val enableSound by themeManager.enableSoundFlow.collectAsStateWithLifecycle()
    val animationSpeed by themeManager.animationSpeedFlow.collectAsStateWithLifecycle()

    val useDarkTheme = when (darkModeSetting) {
        ThemeManager.DarkModeSettings.SYSTEM -> darkTheme
        ThemeManager.DarkModeSettings.LIGHT -> false
        ThemeManager.DarkModeSettings.DARK -> true
    }

    val currentTheme = AppThemes.getTheme(themeType)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        useDarkTheme -> currentTheme.darkColors
        else -> currentTheme.lightColors
    }

    val animationSettings = AnimationSettings(
        enabled = enableAnimations,
        particlesEnabled = enableParticles,
        hapticsEnabled = enableHaptics,
        soundEnabled = enableSound,
        speedMultiplier = animationSpeed.multiplier
    )

    val themeShapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )

    CompositionLocalProvider(
        LocalAppTheme provides currentTheme,
        LocalAnimationSettings provides animationSettings,
        LocalIsDarkTheme provides useDarkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WtaTypography,
            shapes = themeShapes,
            content = { content(useDarkTheme) }
        )
    }
}

@Composable
fun WebToAppThemeSimple(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAppTheme provides AppThemes.Default,
        LocalAnimationSettings provides AnimationSettings(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WtaTypography,
            content = content
        )
    }
}

@Composable
fun ShellTheme(
    themeTypeName: String = "KIMI_NO_NAWA",
    darkModeSetting: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()

    val themeType = try {
        AppThemeType.valueOf(themeTypeName)
    } catch (e: Exception) {
        AppThemeType.KIMI_NO_NAWA
    }

    val useDarkTheme = when (darkModeSetting) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemDarkTheme
    }

    val currentTheme = AppThemes.getTheme(themeType)

    val colorScheme = if (useDarkTheme) currentTheme.darkColors else currentTheme.lightColors

    val animationSettings = AnimationSettings(
        enabled = true,
        particlesEnabled = currentTheme.effects.enableParticles,
        hapticsEnabled = true,
        speedMultiplier = 1f
    )

    val themeShapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )

    CompositionLocalProvider(
        LocalAppTheme provides currentTheme,
        LocalAnimationSettings provides animationSettings,
        LocalIsDarkTheme provides useDarkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WtaTypography,
            shapes = themeShapes,
            content = content
        )
    }
}
