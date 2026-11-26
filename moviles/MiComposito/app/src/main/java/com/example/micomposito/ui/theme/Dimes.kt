package com.example.micomposito.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sistema de dimensiones siguiendo Material Design 3
 * Basado en múltiplos de 4dp para consistencia
 */
object Dimens {
    // Padding y Spacing
    val paddingExtraSmall = 4.dp
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    val paddingExtraLarge = 32.dp

    // Spacing entre elementos
    val spacingExtraSmall = 4.dp
    val spacingSmall = 8.dp
    val spacingMedium = 12.dp
    val spacingLarge = 16.dp
    val spacingExtraLarge = 24.dp

    // Alturas de componentes
    val buttonHeightSmall = 40.dp
    val buttonHeightMedium = 48.dp
    val buttonHeightLarge = 56.dp

    val textFieldHeight = 56.dp
    val textAreaHeight = 100.dp

    // Anchos mínimos
    val minTouchTarget = 48.dp
    val minButtonWidth = 64.dp

    // Bordes y esquinas
    val cornerRadiusSmall = 4.dp
    val cornerRadiusMedium = 8.dp
    val cornerRadiusLarge = 16.dp

    // Elevaciones
    val elevationNone = 0.dp
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationLarge = 8.dp

    // Tamaños de texto (Typography)
    // Siguiendo Material Design 3 Type Scale
    val textSizeExtraSmall = 10.sp  // Caption pequeño
    val textSizeSmall = 12.sp       // Caption, botones pequeños
    val textSizeMedium = 14.sp      // Body small, labels
    val textSizeBody = 16.sp        // Body regular (default)
    val textSizeLarge = 18.sp       // Body large
    val textSizeTitle = 20.sp       // Titles
    val textSizeHeadline = 24.sp    // Headlines
    val textSizeDisplay = 28.sp     // Display text
}
