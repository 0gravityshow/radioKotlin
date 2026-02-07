package com.killedbythegalaxy.radiokotlin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpaceDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = VoidBlack,
    primaryContainer = CyberBlue,
    onPrimaryContainer = CyberCyan,
    
    secondary = NebulaPurple,
    onSecondary = StarWhite,
    secondaryContainer = DeepSpace,
    onSecondaryContainer = NebulaPink,
    
    tertiary = PaywallGold,
    onTertiary = VoidBlack,
    
    background = VoidBlack,
    onBackground = StarWhite,
    
    surface = CosmicGray,
    onSurface = StarWhite,
    surfaceVariant = DeepSpace,
    onSurfaceVariant = CyberCyanDark,
    
    error = StatusOffline,
    onError = StarWhite,
    
    outline = CyberCyanDark,
    outlineVariant = CyberBlue,
    
    inverseSurface = StarWhite,
    inverseOnSurface = VoidBlack,
    inversePrimary = CyberBlue
)

@Composable
fun RadioKotlinTheme(
    darkTheme: Boolean = true, // Always dark for space theme
    dynamicColor: Boolean = false, // Disable dynamic colors
    content: @Composable () -> Unit
) {
    val colorScheme = SpaceDarkColorScheme // Always use space theme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VoidBlack.toArgb()
            window.navigationBarColor = VoidBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
