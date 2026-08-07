package com.lwg.challenge.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun NotificationPermissionEffect(enabled: Boolean) {
    // iOS 푸시 수신은 APNs 인증키가 선행돼야 하고 그 발급이 유료 Apple Developer Program 전용이라
    // 이번 범위 밖이다. 권한만 먼저 물어도 받을 푸시가 없으므로 요청하지 않는다.
}
