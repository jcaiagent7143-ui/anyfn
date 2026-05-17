/**
 * AnyfnTheme — Material 3 dark-first theme used by the entire app.
 * There is intentionally no light variant in v0.1; the brand is dark.
 */
package dev.anyfn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AnyfnDarkColors = darkColorScheme(
    primary = Sky500,
    onPrimary = BgDark,
    primaryContainer = Sky700,
    onPrimaryContainer = TextPrimary,
    secondary = Green500,
    onSecondary = BgDark,
    error = Red500,
    background = BgDark,
    onBackground = TextPrimary,
    surface = BgDarkElevated,
    onSurface = TextPrimary,
    surfaceVariant = BgDarkCard,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
)

@Composable
fun AnyfnTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AnyfnDarkColors,
        typography = AnyfnTypography,
        content = content,
    )
}
