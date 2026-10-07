package pe.novatick.anitec.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// Esquema de Colores Claro (Light Theme)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = PrimaryGreen,

    secondary = PrimaryGreenMedium,
    onSecondary = Color.White,

    tertiary = PrimaryGreenAccent,
    onTertiary = NeutralDark,

    background = AppBackground,
    onBackground = NeutralDark,

    surface = Color.White,
    onSurface = NeutralDark,

    surfaceVariant = PrimaryGreenLight,
    onSurfaceVariant = NeutralGrayDark,

    outline = NeutralGrayLight,
    outlineVariant = NeutralSlate
)

// Esquema de Colores Oscuro (Dark Theme)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenAccent,
    onPrimary = NeutralDark,
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = PrimaryGreenLight,

    secondary = PrimaryGreenMedium,
    onSecondary = NeutralDark,

    tertiary = PrimaryGreen,
    onTertiary = Color.White,

    background = NeutralDark,
    onBackground = PrimaryGreenLight,

    surface = Color(0xFF1E1E1E),
    onSurface = PrimaryGreenLight,

    surfaceVariant = NeutralGrayDark,
    onSurfaceVariant = NeutralGrayLight,

    outline = NeutralSlate,
    outlineVariant = NeutralGrayLight
)

@Composable
fun AniTecTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}