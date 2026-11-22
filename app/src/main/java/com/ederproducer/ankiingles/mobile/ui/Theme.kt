package com.ederproducer.ankiingles.mobile.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Accent = Color(0xFFE68E0D)
val Background = Color(0xFF121212)
val Surface = Color(0xFF1E1E1E)
val SurfaceDark = Color(0xFF090909)
val TextPrimary = Color(0xFFBEBEBE)
val Again = Color(0xFFEF4444)
val Hard = Color(0xFFF59E0B)
val Good = Color(0xFF10B981)
val Easy = Color(0xFF3B82F6)

private val AnkiColors = darkColorScheme(
    primary = Accent,
    onPrimary = Background,
    primaryContainer = Accent.copy(alpha = 0.18f),
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextPrimary.copy(alpha = 0.68f),
    error = Again,
    outline = TextPrimary.copy(alpha = 0.16f)
)

private val AnkiTypography = Typography(
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp),
    labelLarge = TextStyle(fontSize = 15.sp)
)

private val AnkiShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun AnkiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AnkiColors,
        typography = AnkiTypography,
        shapes = AnkiShapes,
        content = content
    )
}
