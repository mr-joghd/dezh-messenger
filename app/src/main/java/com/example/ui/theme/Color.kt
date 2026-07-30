package com.starrynightstudio.dezhmessenger.xwpqrs.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.starrynightstudio.dezhmessenger.xwpqrs.DezhTheme

// Core brand elements
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// 1. Simple Dark
val SimpleDarkPrimary = Color(0xFF38BDF8)
val SimpleDarkBackground = Color(0xFF1E1E1E)
val SimpleDarkSurface = Color(0xFF2D2D2D)
val SimpleDarkOnPrimary = Color(0xFF0F172A)
val SimpleDarkOnBackground = Color(0xFFF1F5F9)
val SimpleDarkOnSurface = Color(0xFFF1F5F9)
val SimpleDarkBorder = Color(0xFF334155)

// 2. Light Mode
val LightPrimary = Color(0xFF0EA5E9)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF8FAFC)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF0F172A)
val LightBorder = Color(0xFFE2E8F0)

// 3. Cyber-Like
val CyberPrimary = Color(0xFF00F0FF)
val CyberBackground = Color(0xFF0B0F19)
val CyberSurface = Color(0xFF111827)
val CyberOnPrimary = Color(0xFF030712)
val CyberOnBackground = Color(0xFFE5E7EB)
val CyberOnSurface = Color(0xFFF8FAFC)
val CyberBorder = Color(0xFF00F0FF)

// 4. Pinky
val PinkyPrimary = Color(0xFFEC4899)
val PinkyBackground = Color(0xFFFFF1F2)
val PinkySurface = Color(0xFFFFE4E6)
val PinkyOnPrimary = Color(0xFFFFFFFF)
val PinkyOnBackground = Color(0xFF4C0519)
val PinkyOnSurface = Color(0xFF4C0519)
val PinkyBorder = Color(0xFFFDA4AF)

// 5. Obsidian (Secret)
val ObsidianPrimary = Color(0xFFEF4444)
val ObsidianBackground = Color(0xFF000000)
val ObsidianSurface = Color(0xFF0A0A0A)
val ObsidianOnPrimary = Color(0xFFFFFFFF)
val ObsidianOnBackground = Color(0xFFF3F4F6)
val ObsidianOnSurface = Color(0xFFE5E7EB)
val ObsidianBorder = Color(0xFFEF4444)

// 6. Amber (Secret)
val AmberPrimary = Color(0xFFF59E0B)
val AmberBackground = Color(0xFF000000)
val AmberSurface = Color(0xFF0E0E0E)
val AmberOnPrimary = Color(0xFF000000)
val AmberOnBackground = Color(0xFFFBBF24)
val AmberOnSurface = Color(0xFFF59E0B)
val AmberBorder = Color(0xFFF59E0B)

// 7. RGB Gaming (Secret)
val RgbPrimary = Color(0xFF8B5CF6)
val RgbBackground = Color(0xFF000000)
val RgbSurface = Color(0xFF121212)
val RgbOnPrimary = Color(0xFFFFFFFF)
val RgbOnBackground = Color(0xFFFFFFFF)
val RgbOnSurface = Color(0xFFFFFFFF)
val RgbBorder = Color(0xFF374151)

// 8. Galaxy
val GalaxyPrimary = Color(0xFFC084FC)
val GalaxyBackground = Color(0xFF0D0B1D)
val GalaxySurface = Color(0xFF181433)
val GalaxyOnPrimary = Color(0xFF1E1B4B)
val GalaxyOnBackground = Color(0xFFF3E8FF)
val GalaxyOnSurface = Color(0xFFE9D5FF)
val GalaxyBorder = Color(0xFF7E22CE)

// 9. Emerald
val EmeraldPrimary = Color(0xFF34D399)
val EmeraldBackground = Color(0xFF031A14)
val EmeraldSurface = Color(0xFF0A2920)
val EmeraldOnPrimary = Color(0xFF022C22)
val EmeraldOnBackground = Color(0xFFECFDF5)
val EmeraldOnSurface = Color(0xFFD1FAE5)
val EmeraldBorder = Color(0xFF059669)

data class ThemeColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val border: Color,
    val isDark: Boolean,
    val isObsidian: Boolean = false,
    val isAmber: Boolean = false,
    val isRGB: Boolean = false,
    val isGalaxy: Boolean = false,
    val isEmerald: Boolean = false
)

val LocalThemeColors = staticCompositionLocalOf {
    ThemeColors(
        primary = SimpleDarkPrimary,
        background = SimpleDarkBackground,
        surface = SimpleDarkSurface,
        onPrimary = SimpleDarkOnPrimary,
        onBackground = SimpleDarkOnBackground,
        onSurface = SimpleDarkOnSurface,
        border = SimpleDarkBorder,
        isDark = true
    )
}

fun getThemeColors(theme: DezhTheme): ThemeColors {
    return when (theme) {
        DezhTheme.SIMPLE_DARK -> ThemeColors(
            primary = SimpleDarkPrimary,
            background = SimpleDarkBackground,
            surface = SimpleDarkSurface,
            onPrimary = SimpleDarkOnPrimary,
            onBackground = SimpleDarkOnBackground,
            onSurface = SimpleDarkOnSurface,
            border = SimpleDarkBorder,
            isDark = true
        )
        DezhTheme.LIGHT_MODE -> ThemeColors(
            primary = LightPrimary,
            background = LightBackground,
            surface = LightSurface,
            onPrimary = LightOnPrimary,
            onBackground = LightOnBackground,
            onSurface = LightOnSurface,
            border = LightBorder,
            isDark = false
        )
        DezhTheme.CYBER -> ThemeColors(
            primary = CyberPrimary,
            background = CyberBackground,
            surface = CyberSurface,
            onPrimary = CyberOnPrimary,
            onBackground = CyberOnBackground,
            onSurface = CyberOnSurface,
            border = CyberBorder,
            isDark = true
        )
        DezhTheme.PINKY -> ThemeColors(
            primary = PinkyPrimary,
            background = PinkyBackground,
            surface = PinkySurface,
            onPrimary = PinkyOnPrimary,
            onBackground = PinkyOnBackground,
            onSurface = PinkyOnSurface,
            border = PinkyBorder,
            isDark = false
        )
        DezhTheme.OBSIDIAN -> ThemeColors(
            primary = ObsidianPrimary,
            background = ObsidianBackground,
            surface = ObsidianSurface,
            onPrimary = ObsidianOnPrimary,
            onBackground = ObsidianOnBackground,
            onSurface = ObsidianOnSurface,
            border = ObsidianBorder,
            isDark = true,
            isObsidian = true
        )
        DezhTheme.AMBER -> ThemeColors(
            primary = AmberPrimary,
            background = AmberBackground,
            surface = AmberSurface,
            onPrimary = AmberOnPrimary,
            onBackground = AmberOnBackground,
            onSurface = AmberOnSurface,
            border = AmberBorder,
            isDark = true,
            isAmber = true
        )
        DezhTheme.RGB_GAMING -> ThemeColors(
            primary = RgbPrimary,
            background = RgbBackground,
            surface = RgbSurface,
            onPrimary = RgbOnPrimary,
            onBackground = RgbOnBackground,
            onSurface = RgbOnSurface,
            border = RgbBorder,
            isDark = true,
            isRGB = true
        )
        DezhTheme.GALAXY -> ThemeColors(
            primary = GalaxyPrimary,
            background = GalaxyBackground,
            surface = GalaxySurface,
            onPrimary = GalaxyOnPrimary,
            onBackground = GalaxyOnBackground,
            onSurface = GalaxyOnSurface,
            border = GalaxyBorder,
            isDark = true,
            isGalaxy = true
        )
        DezhTheme.EMERALD -> ThemeColors(
            primary = EmeraldPrimary,
            background = EmeraldBackground,
            surface = EmeraldSurface,
            onPrimary = EmeraldOnPrimary,
            onBackground = EmeraldOnBackground,
            onSurface = EmeraldOnSurface,
            border = EmeraldBorder,
            isDark = true,
            isEmerald = true
        )
    }
}
