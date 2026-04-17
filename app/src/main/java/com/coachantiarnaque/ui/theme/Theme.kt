package com.coachantiarnaque.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Palette mauve/noir moderne
val PurplePrimary = Color(0xFF9C27B0)
val PurpleLight = Color(0xFFCE93D8)
val PurpleDark = Color(0xFF7B1FA2)
val PurpleDeep = Color(0xFF4A148C)
val PurpleAccent = Color(0xFFE040FB)
val PurpleSurface = Color(0xFF1A1A2E)
val PurpleSurfaceLight = Color(0xFF16213E)
val NeonPurple = Color(0xFFBB86FC)

val DarkBg = Color(0xFF0F0F1A)
val DarkSurface = Color(0xFF1A1A2E)
val DarkCard = Color(0xFF222244)
val DarkCardLight = Color(0xFF2A2A4A)

val SafeGreen = Color(0xFF00E676)
val SuspiciousOrange = Color(0xFFFFAB40)
val ScamRed = Color(0xFFFF5252)

val TextWhite = Color(0xFFF5F5F5)
val TextGray = Color(0xFFB0B0C0)

// Gradients
val PurpleGradient = Brush.verticalGradient(
    colors = listOf(PurpleDeep, PurpleDark, PurplePrimary)
)
val CardGradient = Brush.verticalGradient(
    colors = listOf(DarkCard, DarkCardLight)
)

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.Black,
    secondary = PurpleAccent,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray,
    error = ScamRed,
    outline = PurpleLight.copy(alpha = 0.3f)
)

val ElderlyTypography = Typography(
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp),
    bodySmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp)
)

@Composable
fun CoachAntiArnaqueTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            window.navigationBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ElderlyTypography,
        content = content
    )
}
