package br.edu.utfpr.unihelper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    secondary = Secondary,
    background = Background,
    onBackground = Primary,
    surface = Surface,
    onSurface = Primary,
    outline = Border,
    error = Alert,
    onError = Surface,
)

@Composable
fun UnihelperTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}