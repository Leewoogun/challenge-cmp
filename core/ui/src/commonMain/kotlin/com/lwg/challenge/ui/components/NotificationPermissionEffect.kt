package com.lwg.challenge.ui.components

import androidx.compose.runtime.Composable

/**
 * 알림 권한을 필요한 플랫폼에서만 1회 요청한다.
 *
 * 거부돼도 앱 동작에 영향이 없어야 하므로 결과를 반환하지 않는다 — 호출부가 분기할 것이 없다.
 */
@Composable
expect fun NotificationPermissionEffect(enabled: Boolean)
