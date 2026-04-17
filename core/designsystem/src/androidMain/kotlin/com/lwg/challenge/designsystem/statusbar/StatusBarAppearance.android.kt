package com.lwg.challenge.designsystem.statusbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun SetStatusBarAppearance(statusBarColor: Color) {
    val view = LocalView.current
    val useLightStatusBarIcons = statusBarColor.luminance() > 0.5f

    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useLightStatusBarIcons
    }
}
