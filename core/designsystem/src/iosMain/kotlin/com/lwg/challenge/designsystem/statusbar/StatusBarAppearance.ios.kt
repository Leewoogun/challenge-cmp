package com.lwg.challenge.designsystem.statusbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * iOS에서 StatusBar 아이콘 색상은 ViewController의 preferredStatusBarStyle로 제어.
 * ComposeUIViewController를 감싸는 Swift 측에서 설정 필요:
 *
 * ```swift
 * override var preferredStatusBarStyle: UIStatusBarStyle {
 *     return .darkContent // 또는 .lightContent
 * }
 * ```
 *
 * StatusBar 영역의 배경색은 ChallengeScaffold의 statusBarColor +
 * statusBarsPadding()으로 처리되므로 시각적으로 동일한 결과.
 */
@Composable
actual fun SetStatusBarAppearance(statusBarColor: Color) {
    // iOS: StatusBar 아이콘 스타일은 Info.plist의 UIStatusBarStyle 또는
    // ViewController에서 제어. Compose 레벨에서는 배경색만 처리.
}
