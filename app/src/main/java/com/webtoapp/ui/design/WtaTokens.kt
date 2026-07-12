package com.webtoapp.ui.design

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
enum class WtaCapabilityLevel {
    Common,
    Advanced,
    Lab
}

@Stable
enum class WtaStatusTone {
    Info,
    Success,
    Warning,
    Error
}

@Stable
enum class WtaRowTone {
    Normal,
    Danger
}

@Stable
enum class WtaSectionHeaderStyle {
    Prominent,
    Quiet,
    Hidden
}

@Stable
enum class WtaButtonVariant {

    Primary,

    Tonal,

    Outlined,

    Text,

    Destructive
}

@Stable
enum class WtaButtonSize {
    Small,
    Medium,
    Large
}

object WtaSpacing {
    val Tiny: Dp = 4.dp
    val Small: Dp = 8.dp
    val Medium: Dp = 12.dp
    val Large: Dp = 16.dp
    val ExtraLarge: Dp = 24.dp

    val ScreenHorizontal: Dp = 16.dp
    val ScreenVertical: Dp = 16.dp
    val SectionGap: Dp = 20.dp
    val CardGap: Dp = 12.dp
    val RowHorizontal: Dp = 16.dp
    val RowVertical: Dp = 12.dp
    val ContentGap: Dp = 8.dp
    val IconTextGap: Dp = 12.dp
}

object WtaRadius {
    val Pill: Dp = 999.dp
    val Card: Dp = 16.dp
    val Button: Dp = 20.dp
    val Control: Dp = 12.dp
    val IconPlate: Dp = 12.dp
    val Chip: Dp = 8.dp
    val Badge: Dp = 8.dp
    val Dialog: Dp = 28.dp
}

object WtaSize {
    val Icon: Dp = 22.dp
    val IconSmall: Dp = 16.dp
    val IconLarge: Dp = 26.dp
    val IconPlate: Dp = 40.dp
    val IconPlateLarge: Dp = 48.dp
    val RowMinHeight: Dp = 60.dp
    val RowTrailingMaxWidth: Dp = 148.dp
    val BannerActionMaxWidth: Dp = 132.dp
    val TouchTarget: Dp = 48.dp
    val ButtonHeightSmall: Dp = 32.dp
    val ButtonHeightMedium: Dp = 40.dp
    val ButtonHeightLarge: Dp = 48.dp
    val TextFieldHeight: Dp = 56.dp
    val AvatarSmall: Dp = 32.dp
    val AvatarMedium: Dp = 40.dp
    val AvatarLarge: Dp = 56.dp
}

object WtaElevation {

    val Level0: Dp = 0.dp

    val Level1: Dp = 1.dp

    val Level2: Dp = 3.dp

    val Level3: Dp = 6.dp

    val Level4: Dp = 12.dp
}

object WtaAlpha {
    const val Disabled = 0.38f
    const val Divider = 0.55f
    const val MutedContainer = 0.12f
    const val PressedContainer = 0.12f
    const val Subtle = 0.16f
    const val Medium = 0.32f
    const val Strong = 0.64f
}
