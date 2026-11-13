package com.lwg.challenge.designsystem.statusbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun SetNavigationBarAppearance(darkTheme: Boolean) {
    if (LocalInspectionMode.current) return
    val view = LocalView.current

    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
    }
}
