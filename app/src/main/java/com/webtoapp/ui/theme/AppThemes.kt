package com.webtoapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings

enum class AppThemeType(val icon: String) {
    KIMI_NO_NAWA("Minimize");

    fun getDisplayName(): String = Strings.themeKimiNoNawa
    fun getDescription(): String = Strings.themeKimiNoNawaDesc
}

enum class AnimationStyle {
    SMOOTH, BOUNCY, SNAPPY, ELEGANT, PLAYFUL, DRAMATIC;

    fun getDisplayName(): String = when (this) {
        SMOOTH -> Strings.animSmooth
        BOUNCY -> Strings.animBouncy
        SNAPPY -> Strings.animSnappy
        ELEGANT -> Strings.animElegant
        PLAYFUL -> Strings.animPlayful
        DRAMATIC -> Strings.animDramatic
    }
}

enum class InteractionStyle {
    RIPPLE, GLOW, SCALE, SHAKE, MORPH, PARTICLE;

    fun getDisplayName(): String = when (this) {
        RIPPLE -> Strings.interRipple
        GLOW -> Strings.interGlow
        SCALE -> Strings.interScale
        SHAKE -> Strings.interShake
        MORPH -> Strings.interMorph
        PARTICLE -> Strings.interParticle
    }
}

@Stable
data class AppTheme(
    val type: AppThemeType,
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val animationStyle: AnimationStyle,
    val interactionStyle: InteractionStyle,
    val gradients: ThemeGradients,
    val effects: ThemeEffects,
    val shapes: ThemeShapes
)

@Stable
data class ThemeGradients(
    val primary: List<Color>,
    val secondary: List<Color>,
    val background: List<Color>,
    val accent: List<Color>,
    val shimmer: List<Color>
) {
    val primaryBrush: Brush get() = Brush.linearGradient(primary)
    val secondaryBrush: Brush get() = Brush.linearGradient(secondary)
    val backgroundBrush: Brush get() = Brush.linearGradient(background)
    val accentBrush: Brush get() = Brush.linearGradient(accent)
}

@Stable
data class ThemeEffects(
    val glowColor: Color,
    val glowRadius: Dp,
    val shadowColor: Color,
    val shadowElevation: Dp,
    val blurRadius: Dp,
    val particleColor: Color,
    val enableParticles: Boolean,
    val enableGlow: Boolean,
    val enableGlassmorphism: Boolean
)

@Stable
data class ThemeShapes(
    val cornerRadius: Dp,
    val buttonRadius: Dp,
    val cardRadius: Dp,
    val dialogRadius: Dp,
    val useRoundedButtons: Boolean,
    val useSoftShadows: Boolean
)

object AppThemes {

    val KimiNoNawa = AppTheme(
        type = AppThemeType.KIMI_NO_NAWA,
        lightColors = lightColorScheme(
            primary = Color(0xFF1B5EFC),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDAE1FF),
            onPrimaryContainer = Color(0xFF00174B),
            inversePrimary = Color(0xFFB2C5FF),

            secondary = Color(0xFF555F71),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD9E3F8),
            onSecondaryContainer = Color(0xFF121C2B),

            tertiary = Color(0xFF6F5675),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF9D8FF),
            onTertiaryContainer = Color(0xFF28132F),

            error = Color(0xFFBA1A1A),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),

            background = Color(0xFFF9F9FF),
            onBackground = Color(0xFF1A1B21),
            surface = Color(0xFFF9F9FF),
            onSurface = Color(0xFF1A1B21),
            surfaceVariant = Color(0xFFE1E2EC),
            onSurfaceVariant = Color(0xFF44464F),

            surfaceTint = Color(0xFF1B5EFC),
            surfaceBright = Color(0xFFF9F9FF),
            surfaceDim = Color(0xFFDADAE2),
            surfaceContainer = Color(0xFFEEEFF7),
            surfaceContainerLow = Color(0xFFF3F3FB),
            surfaceContainerHigh = Color(0xFFE8E8F0),
            surfaceContainerHighest = Color(0xFFE2E2EA),
            surfaceContainerLowest = Color(0xFFFFFFFF),

            outline = Color(0xFF757780),
            outlineVariant = Color(0xFFC5C6D0),

            inverseSurface = Color(0xFF2F3036),
            inverseOnSurface = Color(0xFFF1F0F7),
            scrim = Color(0xFF000000)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFB2C5FF),
            onPrimary = Color(0xFF002A78),
            primaryContainer = Color(0xFF0040A8),
            onPrimaryContainer = Color(0xFFDAE1FF),
            inversePrimary = Color(0xFF1B5EFC),

            secondary = Color(0xFFBDC7DC),
            onSecondary = Color(0xFF273141),
            secondaryContainer = Color(0xFF3D4758),
            onSecondaryContainer = Color(0xFFD9E3F8),

            tertiary = Color(0xFFDCBCE1),
            onTertiary = Color(0xFF3E2845),
            tertiaryContainer = Color(0xFF563E5C),
            onTertiaryContainer = Color(0xFFF9D8FF),

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

            surfaceTint = Color(0xFFB2C5FF),
            surfaceBright = Color(0xFF37393E),
            surfaceDim = Color(0xFF111318),
            surfaceContainer = Color(0xFF1E1F25),
            surfaceContainerLow = Color(0xFF1A1B21),
            surfaceContainerHigh = Color(0xFF282A2F),
            surfaceContainerHighest = Color(0xFF33343A),
            surfaceContainerLowest = Color(0xFF0C0E13),

            outline = Color(0xFF8F909A),
            outlineVariant = Color(0xFF44464F),

            inverseSurface = Color(0xFFE2E2E9),
            inverseOnSurface = Color(0xFF2F3036),
            scrim = Color(0xFF000000)
        ),
        animationStyle = AnimationStyle.SMOOTH,
        interactionStyle = InteractionStyle.RIPPLE,
        gradients = ThemeGradients(
            primary = listOf(Color(0xFF1B5EFC), Color(0xFF0040A8)),
            secondary = listOf(Color(0xFF555F71), Color(0xFF3D4758)),
            background = listOf(Color(0xFF111318), Color(0xFF1E1F25)),
            accent = listOf(Color(0xFFB2C5FF), Color(0xFFDAE1FF)),
            shimmer = listOf(Color(0x08FFFFFF), Color(0x22FFFFFF), Color(0x08FFFFFF))
        ),
        effects = ThemeEffects(
            glowColor = Color.Transparent,
            glowRadius = 0.dp,
            shadowColor = Color(0x14000000),
            shadowElevation = 1.dp,
            blurRadius = 0.dp,
            particleColor = Color.Transparent,
            enableParticles = false,
            enableGlow = false,
            enableGlassmorphism = false
        ),
        shapes = ThemeShapes(
            cornerRadius = 16.dp,
            buttonRadius = 20.dp,
            cardRadius = 16.dp,
            dialogRadius = 28.dp,
            useRoundedButtons = true,
            useSoftShadows = false
        )
    )

    val allThemes = listOf(KimiNoNawa)
    fun getTheme(type: AppThemeType): AppTheme = KimiNoNawa
    val Default = KimiNoNawa
}

fun AppThemeType.getLocalizedDisplayName(): String {
    return Strings.themeKimiNoNawa
}

fun AppThemeType.getLocalizedDescription(): String {
    return Strings.themeKimiNoNawaDesc
}

fun AnimationStyle.getLocalizedDisplayName(): String {
    return when (this) {
        AnimationStyle.SMOOTH -> Strings.animSmooth
        AnimationStyle.BOUNCY -> Strings.animBouncy
        AnimationStyle.SNAPPY -> Strings.animSnappy
        AnimationStyle.ELEGANT -> Strings.animElegant
        AnimationStyle.PLAYFUL -> Strings.animPlayful
        AnimationStyle.DRAMATIC -> Strings.animDramatic
    }
}

fun InteractionStyle.getLocalizedDisplayName(): String {
    return when (this) {
        InteractionStyle.RIPPLE -> Strings.interRipple
        InteractionStyle.GLOW -> Strings.interGlow
        InteractionStyle.SCALE -> Strings.interScale
        InteractionStyle.SHAKE -> Strings.interShake
        InteractionStyle.MORPH -> Strings.interMorph
        InteractionStyle.PARTICLE -> Strings.interParticle
    }
}
