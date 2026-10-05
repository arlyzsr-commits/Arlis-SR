package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LapoorLightColorScheme = lightColorScheme(
    primary = PastelSagePrimary,
    onPrimary = Color.White,
    primaryContainer = PastelSageContainer,
    onPrimaryContainer = PastelSageOnContainer,
    secondary = PastelDustyBlue,
    onSecondary = Color.White,
    secondaryContainer = PastelDustyBlueContainer,
    onSecondaryContainer = PastelDustyBlueOnContainer,
    tertiary = PastelMutedPeach,
    onTertiary = Color.White,
    tertiaryContainer = PastelMutedPeachContainer,
    onTertiaryContainer = PastelMutedPeachOnContainer,
    background = PastelCreamBackground,
    onBackground = PastelCharcoalText,
    surface = PastelSurfaceCard,
    onSurface = PastelCharcoalText,
    surfaceVariant = PastelSurfaceVariant,
    onSurfaceVariant = PastelMutedText,
    outline = PastelBorderColor,
    outlineVariant = PastelSoftBeige
)

val LapoorShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Keep intentional brand pastel palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LapoorLightColorScheme,
        typography = Typography,
        shapes = LapoorShapes,
        content = content
    )
}
