package com.lwg.challenge.designsystem.statusbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * StatusBar 아이콘 색상을 제어하는 플랫폼별 구현.
 * - statusBarColor의 밝기(luminance)에 따라 아이콘을 밝게/어둡게 설정
 * - Android: WindowCompat.getInsetsController 사용
 * - iOS: UIApplication.statusBarStyle 사용
 */
@Composable
expect fun SetStatusBarAppearance(statusBarColor: Color)
