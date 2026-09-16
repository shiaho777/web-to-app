package com.webtoapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.webtoapp.core.i18n.Strings

/**
 * Accent color system: the neutral (black/white/grey) skeleton stays fixed,
 * only the primary/secondary/tertiary color roles are overlaid per accent.
 * Each user-facing accent is a dual-color pair: the lead color drives
 * `primary`, the counterpoint color shows through `secondary`/containers.
 */
enum class AppAccent(val swatchPrimary: Color, val swatchSecondary: Color) {
    DEFAULT(Color(0xFF111113), Color(0xFF8A8A8E)),
    DYNAMIC(Color(0xFF88A0F8), Color(0xFFA9E2C3)),
    CHARCOAL_PINK(Color(0xFFE6397C), Color(0xFF1A1A1D)),
    ROYAL_PURPLE(Color(0xFF562583), Color(0xFFFFB929)),
    DEEP_SEA(Color(0xFF122E8A), Color(0xFFF5EFEA)),
    IVY_VIOLET(Color(0xFF5E55A2), Color(0xFF91C53A)),
    SAKURA_FOREST(Color(0xFF014421), Color(0xFFFFB7C5)),
    INK_AQUA(Color(0xFF113056), Color(0xFF91CFD5)),
    SUNSET_HAZE(Color(0xFFFF7D43), Color(0xFF6B88B0)),
    CAMELLIA(Color(0xFFE72D48), Color(0xFFF1DDDF)),
    FROST_CYAN(Color(0xFF4FC3F7), Color(0xFF9E93D4)),
    MOCHA_LATTE(Color(0xFF7B5445), Color(0xFFE9D5C3));

    fun getDisplayName(): String = when (this) {
        DEFAULT -> Strings.accentDefault
        DYNAMIC -> Strings.accentDynamic
        CHARCOAL_PINK -> Strings.accentCharcoalPink
        ROYAL_PURPLE -> Strings.accentRoyalPurple
        DEEP_SEA -> Strings.accentDeepSea
        IVY_VIOLET -> Strings.accentIvyViolet
        SAKURA_FOREST -> Strings.accentSakuraForest
        INK_AQUA -> Strings.accentInkAqua
        SUNSET_HAZE -> Strings.accentSunsetHaze
        CAMELLIA -> Strings.accentCamellia
        FROST_CYAN -> Strings.accentFrostCyan
        MOCHA_LATTE -> Strings.accentMochaLatte
    }
}

@Stable
data class AccentScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val inversePrimary: Color
) {
    fun applyTo(scheme: ColorScheme): ColorScheme {
        // Hue carrier: primary pulled toward gray so tinting shifts hue without
        // visibly changing surface lightness. Dark schemes use pastel primaries,
        // so their surfaces get a slightly stronger lift, matching M3 guidance.
        val dark = scheme.surface.luminance() < 0.3f
        val anchor = blend(primary, Color(0xFF808080), if (dark) 0.42f else 0.52f)
        val s = if (dark) 0.22f else 0.16f
        fun tint(base: Color, factor: Float) = blend(base, anchor, (s * factor).coerceIn(0f, 1f))
        return scheme.copy(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            inversePrimary = inversePrimary,
            surfaceTint = primary,
            // Neutral tokens pick up the accent hue progressively: the closer a
            // container sits to content, the stronger the tint.
            background = tint(scheme.background, 0.7f),
            surface = tint(scheme.surface, 0.7f),
            surfaceDim = tint(scheme.surfaceDim, 0.6f),
            surfaceBright = tint(scheme.surfaceBright, 0.7f),
            surfaceContainerLowest = tint(scheme.surfaceContainerLowest, 0.7f),
            surfaceContainerLow = tint(scheme.surfaceContainerLow, 0.9f),
            surfaceContainer = tint(scheme.surfaceContainer, 1.0f),
            surfaceContainerHigh = tint(scheme.surfaceContainerHigh, 1.25f),
            surfaceContainerHighest = tint(scheme.surfaceContainerHighest, 1.5f),
            surfaceVariant = tint(scheme.surfaceVariant, 1.7f),
            // Text carries the hue too: primary text subtle, secondary stronger.
            onSurface = blend(scheme.onSurface, anchor, if (dark) 0.16f else 0.12f),
            onBackground = blend(scheme.onBackground, anchor, if (dark) 0.16f else 0.12f),
            onSurfaceVariant = blend(scheme.onSurfaceVariant, anchor, if (dark) 0.35f else 0.28f),
            outline = blend(scheme.outline, anchor, if (dark) 0.35f else 0.30f),
            outlineVariant = blend(scheme.outlineVariant, anchor, if (dark) 0.40f else 0.32f),
            // Inverse tokens back snackbars and overlay menus; keep them in-family too.
            inverseSurface = blend(scheme.inverseSurface, anchor, 0.30f),
            inverseOnSurface = blend(scheme.inverseOnSurface, anchor, 0.15f)
        )
    }

    private fun blend(base: Color, toward: Color, amount: Float): Color {
        val a = amount.coerceIn(0f, 1f)
        return Color(
            red = base.red + (toward.red - base.red) * a,
            green = base.green + (toward.green - base.green) * a,
            blue = base.blue + (toward.blue - base.blue) * a,
            alpha = base.alpha
        )
    }
}

object AppAccentPalettes {

    private val CharcoalPinkLight = AccentScheme(
        primary = Color(0xFFC81E63), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E3), onPrimaryContainer = Color(0xFF40001D),
        secondary = Color(0xFF46434A), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE9E6EC), onSecondaryContainer = Color(0xFF1F1D24),
        tertiary = Color(0xFF7E5367), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8E8), onTertiaryContainer = Color(0xFF31101F),
        inversePrimary = Color(0xFFFFACC9)
    )
    private val CharcoalPinkDark = AccentScheme(
        primary = Color(0xFFFF92B7), onPrimary = Color(0xFF5C0032),
        primaryContainer = Color(0xFF841E55), onPrimaryContainer = Color(0xFFFFD9E3),
        secondary = Color(0xFFCBC5CF), onSecondary = Color(0xFF322F35),
        secondaryContainer = Color(0xFF48454C), onSecondaryContainer = Color(0xFFE7E1E9),
        tertiary = Color(0xFFEFB8CD), onTertiary = Color(0xFF4A2535),
        tertiaryContainer = Color(0xFF633A4B), onTertiaryContainer = Color(0xFFFFD8E8),
        inversePrimary = Color(0xFFC81E63)
    )

    private val RoyalPurpleLight = AccentScheme(
        primary = Color(0xFF622B92), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEFDBFF), onPrimaryContainer = Color(0xFF2A0051),
        secondary = Color(0xFF7A5800), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFE197), onSecondaryContainer = Color(0xFF261A00),
        tertiary = Color(0xFF835500), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDDB4), onTertiaryContainer = Color(0xFF2A1800),
        inversePrimary = Color(0xFFD5BAFF)
    )
    private val RoyalPurpleDark = AccentScheme(
        primary = Color(0xFFD5BAFF), onPrimary = Color(0xFF3F0078),
        primaryContainer = Color(0xFF4D1A79), onPrimaryContainer = Color(0xFFEFDBFF),
        secondary = Color(0xFFE4C54B), onSecondary = Color(0xFF3A2E00),
        secondaryContainer = Color(0xFF574500), onSecondaryContainer = Color(0xFFFFE9A8),
        tertiary = Color(0xFFE6BD48), onTertiary = Color(0xFF3E2E00),
        tertiaryContainer = Color(0xFF5B4300), onTertiaryContainer = Color(0xFFFFDDB4),
        inversePrimary = Color(0xFF622B92)
    )

    private val DeepSeaLight = AccentScheme(
        primary = Color(0xFF1D3FA2), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCE4FF), onPrimaryContainer = Color(0xFF00115E),
        secondary = Color(0xFF5B5A55), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF4EFE6), onSecondaryContainer = Color(0xFF2B2721),
        tertiary = Color(0xFF6B5C48), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF3E7D3), onTertiaryContainer = Color(0xFF241A08),
        inversePrimary = Color(0xFFB4C5FF)
    )
    private val DeepSeaDark = AccentScheme(
        primary = Color(0xFFB4C5FF), onPrimary = Color(0xFF10267F),
        primaryContainer = Color(0xFF18348E), onPrimaryContainer = Color(0xFFDCE4FF),
        secondary = Color(0xFFD9D0C2), onSecondary = Color(0xFF3B3833),
        secondaryContainer = Color(0xFF47443E), onSecondaryContainer = Color(0xFFF4EFE6),
        tertiary = Color(0xFFD6C7AD), onTertiary = Color(0xFF3B2E17),
        tertiaryContainer = Color(0xFF54432C), onTertiaryContainer = Color(0xFFF3E7D3),
        inversePrimary = Color(0xFF1D3FA2)
    )

    private val IvyVioletLight = AccentScheme(
        primary = Color(0xFF5E55A2), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE4DFFF), onPrimaryContainer = Color(0xFF1A0F5F),
        secondary = Color(0xFF4D6700), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD3EF9D), onSecondaryContainer = Color(0xFF161F00),
        tertiary = Color(0xFF6C5F95), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEBDDFF), onTertiaryContainer = Color(0xFF26174E),
        inversePrimary = Color(0xFFC7C0FF)
    )
    private val IvyVioletDark = AccentScheme(
        primary = Color(0xFFC7C0FF), onPrimary = Color(0xFF2F2780),
        primaryContainer = Color(0xFF463E8A), onPrimaryContainer = Color(0xFFE4DFFF),
        secondary = Color(0xFFB8D46C), onSecondary = Color(0xFF263500),
        secondaryContainer = Color(0xFF3B4E10), onSecondaryContainer = Color(0xFFD3EF9D),
        tertiary = Color(0xFFC9C0E8), onTertiary = Color(0xFF342A63),
        tertiaryContainer = Color(0xFF4A4276), onTertiaryContainer = Color(0xFFEBDDFF),
        inversePrimary = Color(0xFF5E55A2)
    )

    private val SakuraForestLight = AccentScheme(
        primary = Color(0xFF0E5033), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8F0D7), onPrimaryContainer = Color(0xFF00210F),
        secondary = Color(0xFF8C4A58), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9DF), onSecondaryContainer = Color(0xFF381019),
        tertiary = Color(0xFF8A4F5C), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD9E0), onTertiaryContainer = Color(0xFF35101A),
        inversePrimary = Color(0xFF8CD5A6)
    )
    private val SakuraForestDark = AccentScheme(
        primary = Color(0xFF8CD5A6), onPrimary = Color(0xFF003920),
        primaryContainer = Color(0xFF14522F), onPrimaryContainer = Color(0xFFC8F0D7),
        secondary = Color(0xFFFFB2BF), onSecondary = Color(0xFF5D1F2C),
        secondaryContainer = Color(0xFF71333F), onSecondaryContainer = Color(0xFFFFD9DF),
        tertiary = Color(0xFFECB8C0), onTertiary = Color(0xFF4C252C),
        tertiaryContainer = Color(0xFF663B43), onTertiaryContainer = Color(0xFFFFD9E0),
        inversePrimary = Color(0xFF0E5033)
    )

    private val InkAquaLight = AccentScheme(
        primary = Color(0xFF153A6E), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD6E6FF), onPrimaryContainer = Color(0xFF001B3D),
        secondary = Color(0xFF3E6E75), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFC4ECF2), onSecondaryContainer = Color(0xFF062127),
        tertiary = Color(0xFF50606E), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD3E5F5), onTertiaryContainer = Color(0xFF0D1D28),
        inversePrimary = Color(0xFFA9CAF2)
    )
    private val InkAquaDark = AccentScheme(
        primary = Color(0xFFA9CAF2), onPrimary = Color(0xFF0A305C),
        primaryContainer = Color(0xFF284870), onPrimaryContainer = Color(0xFFD6E6FF),
        secondary = Color(0xFF9ED2D8), onSecondary = Color(0xFF0C373C),
        secondaryContainer = Color(0xFF274C51), onSecondaryContainer = Color(0xFFC4ECF2),
        tertiary = Color(0xFFBAC8D4), onTertiary = Color(0xFF243240),
        tertiaryContainer = Color(0xFF3B4856), onTertiaryContainer = Color(0xFFD3E5F5),
        inversePrimary = Color(0xFF153A6E)
    )

    private val SunsetHazeLight = AccentScheme(
        primary = Color(0xFFA94A13), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBCB), onPrimaryContainer = Color(0xFF341100),
        secondary = Color(0xFF51677E), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD5E4F5), onSecondaryContainer = Color(0xFF0E2235),
        tertiary = Color(0xFF8A4A28), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDBC8), onTertiaryContainer = Color(0xFF311301),
        inversePrimary = Color(0xFFFFB592)
    )
    private val SunsetHazeDark = AccentScheme(
        primary = Color(0xFFFFB592), onPrimary = Color(0xFF571C00),
        primaryContainer = Color(0xFF823307), onPrimaryContainer = Color(0xFFFFDBCB),
        secondary = Color(0xFFB7CBE2), onSecondary = Color(0xFF22354B),
        secondaryContainer = Color(0xFF384C62), onSecondaryContainer = Color(0xFFD5E4F5),
        tertiary = Color(0xFFF4B98E), onTertiary = Color(0xFF4C2811),
        tertiaryContainer = Color(0xFF663D22), onTertiaryContainer = Color(0xFFFFDBC8),
        inversePrimary = Color(0xFFA94A13)
    )

    private val CamelliaLight = AccentScheme(
        primary = Color(0xFFBE1F3C), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDADA), onPrimaryContainer = Color(0xFF40000D),
        secondary = Color(0xFF7C575C), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF7DFE1), onSecondaryContainer = Color(0xFF2E1518),
        tertiary = Color(0xFF815344), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDBD0), onTertiaryContainer = Color(0xFF32130B),
        inversePrimary = Color(0xFFFFB2B8)
    )
    private val CamelliaDark = AccentScheme(
        primary = Color(0xFFFFB2B8), onPrimary = Color(0xFF680016),
        primaryContainer = Color(0xFF910029), onPrimaryContainer = Color(0xFFFFDADA),
        secondary = Color(0xFFE5BFC3), onSecondary = Color(0xFF442A2E),
        secondaryContainer = Color(0xFF5C3F43), onSecondaryContainer = Color(0xFFF7DFE1),
        tertiary = Color(0xFFF0B7A8), onTertiary = Color(0xFF4A2519),
        tertiaryContainer = Color(0xFF65382C), onTertiaryContainer = Color(0xFFFFDBD0),
        inversePrimary = Color(0xFFBE1F3C)
    )

    // Faithful port of the v1.8.0 FROST (冰晶之境) theme: bright cyan primary
    // with dark-navy content, lavender containers as in the original screens.
    private val FrostCyanLight = AccentScheme(
        primary = Color(0xFF4FC3F7), onPrimary = Color(0xFF00344A),
        primaryContainer = Color(0xFFE1F5FE), onPrimaryContainer = Color(0xFF001F2E),
        secondary = Color(0xFF81D4FA), onSecondary = Color(0xFF00354C),
        secondaryContainer = Color(0xFFE8DEF8), onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Color(0xFF0277BD), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFCBE8F9), onTertiaryContainer = Color(0xFF001E30),
        inversePrimary = Color(0xFF0288D1)
    )
    private val FrostCyanDark = AccentScheme(
        primary = Color(0xFF4FC3F7), onPrimary = Color(0xFF00344A),
        primaryContainer = Color(0xFF0277BD), onPrimaryContainer = Color(0xFFE1F5FE),
        secondary = Color(0xFF81D4FA), onSecondary = Color(0xFF00354C),
        secondaryContainer = Color(0xFF4A4458), onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Color(0xFF9CD2F2), onTertiary = Color(0xFF00344A),
        tertiaryContainer = Color(0xFF0F4A6B), onTertiaryContainer = Color(0xFFD4EAFF),
        inversePrimary = Color(0xFF0288D1)
    )

    // Espresso brown lead with latte-foam counterpoint — the warm-neutral entry
    // in the set (Pantone mocha family).
    private val MochaLatteLight = AccentScheme(
        primary = Color(0xFF7B5445), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF1DCD0), onPrimaryContainer = Color(0xFF2A140B),
        secondary = Color(0xFF8A6A55), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF6E7DA), onSecondaryContainer = Color(0xFF2C1F15),
        tertiary = Color(0xFF8C6A54), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF4DFCE), onTertiaryContainer = Color(0xFF29180C),
        inversePrimary = Color(0xFFE4BFA8)
    )
    private val MochaLatteDark = AccentScheme(
        primary = Color(0xFFE4BFA8), onPrimary = Color(0xFF3D2419),
        primaryContainer = Color(0xFF5B4032), onPrimaryContainer = Color(0xFFF7DCCB),
        secondary = Color(0xFFCDB09A), onSecondary = Color(0xFF3A271C),
        secondaryContainer = Color(0xFF4E3B30), onSecondaryContainer = Color(0xFFEADBCB),
        tertiary = Color(0xFFD8BBA6), onTertiary = Color(0xFF3B2415),
        tertiaryContainer = Color(0xFF5D473A), onTertiaryContainer = Color(0xFFF6DFCB),
        inversePrimary = Color(0xFF7B5445)
    )

    fun scheme(accent: AppAccent, darkTheme: Boolean): AccentScheme? = when (accent) {
        AppAccent.DEFAULT, AppAccent.DYNAMIC -> null
        AppAccent.CHARCOAL_PINK -> if (darkTheme) CharcoalPinkDark else CharcoalPinkLight
        AppAccent.ROYAL_PURPLE -> if (darkTheme) RoyalPurpleDark else RoyalPurpleLight
        AppAccent.DEEP_SEA -> if (darkTheme) DeepSeaDark else DeepSeaLight
        AppAccent.IVY_VIOLET -> if (darkTheme) IvyVioletDark else IvyVioletLight
        AppAccent.SAKURA_FOREST -> if (darkTheme) SakuraForestDark else SakuraForestLight
        AppAccent.INK_AQUA -> if (darkTheme) InkAquaDark else InkAquaLight
        AppAccent.SUNSET_HAZE -> if (darkTheme) SunsetHazeDark else SunsetHazeLight
        AppAccent.CAMELLIA -> if (darkTheme) CamelliaDark else CamelliaLight
        AppAccent.FROST_CYAN -> if (darkTheme) FrostCyanDark else FrostCyanLight
        AppAccent.MOCHA_LATTE -> if (darkTheme) MochaLatteDark else MochaLatteLight
    }
}
