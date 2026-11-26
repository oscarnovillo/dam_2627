package com.example.micomposito.ui.common
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP;

    companion object {
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            // Breakpoints oficiales de Material 3:
            // Ancho: Compact (< 600), Medium (600..839), Expanded (>= 840)
            // Alto:  Compact (< 480), Medium (480..899), Expanded (>= 900)
            val isWidthMediumOrMore = windowSizeClass.isWidthAtLeastBreakpoint(
                WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND // 600
            )
            val isWidthExpandedOrMore = windowSizeClass.isWidthAtLeastBreakpoint(
                WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND // 840
            )

            val isHeightMediumOrMore = windowSizeClass.isHeightAtLeastBreakpoint(
                WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND // 480
            )
            val isHeightExpandedOrMore = windowSizeClass.isHeightAtLeastBreakpoint(
                WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND // 900
            )

            return when {
                // Ancho < 600dp (Compact)
                !isWidthMediumOrMore -> MOBILE_PORTRAIT

                // Alto < 480dp (Compact) con ancho >= 600dp (Móvil en horizontal)
                !isHeightMediumOrMore -> MOBILE_LANDSCAPE

                // Ancho 600..839dp (Medium) y Alto >= 900dp (Expanded)
                !isWidthExpandedOrMore && isHeightExpandedOrMore -> TABLET_PORTRAIT

                // Ancho >= 840dp (Expanded) y Alto 480..899dp (Medium)
                isWidthExpandedOrMore && !isHeightExpandedOrMore -> TABLET_LANDSCAPE

                // Ancho >= 840dp y Alto >= 900dp (o ventana mediana cuadrada)
                else -> DESKTOP
            }
        }
    }
}

// Función de ayuda para obtenerlo directamente en cualquier @Composable
@Composable
fun currentDeviceConfiguration(): DeviceConfiguration {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
}