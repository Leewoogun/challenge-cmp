package com.lwg.challenge.feature.notification.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.push.PushEvent

/** 알림 1건의 아이콘·색을 한 묶음으로 확정한 값. */
@Immutable
data class NotificationTypeVisual(
    val icon: ImageVector,
    val accent: Color,
)

/**
 * 알림 타입 → 아이콘·색의 단일 출처. 카드가 고르면 타입이 늘 때마다 카드를 고치게 된다.
 * 모르는 타입은 무채색 폴백으로 그리되 숨기지 않는다 — 숨기면 푸시로 본 알림이 목록에 없다.
 */
@Composable
internal fun notificationTypeVisualOf(type: String): NotificationTypeVisual = when (type) {
    PushEvent.TYPE_CHALLENGE_REQUEST -> NotificationTypeVisual(
        icon = Icons.Filled.SportsKabaddi,
        accent = ChallengeTheme.colorScheme.point,
    )
    // CheckCircle 은 연달아 오는 상대 인증과 겹쳐 계약서 아이콘을 쓴다.
    PushEvent.TYPE_CHALLENGE_ACCEPTED -> NotificationTypeVisual(
        icon = Icons.Filled.Description,
        accent = ChallengeTheme.colorScheme.success,
    )
    PushEvent.TYPE_CHALLENGE_REJECTED -> NotificationTypeVisual(
        icon = Icons.Filled.Cancel,
        accent = ChallengeTheme.colorScheme.error,
    )
    PushEvent.TYPE_OPPONENT_VERIFIED -> NotificationTypeVisual(
        icon = Icons.Filled.CheckCircle,
        accent = ChallengeTheme.colorScheme.success,
    )
    // 다른 네 아이콘과 겹치지 않는 백기. 제목 톤(`계약 파기.`)에 맞춰 거절과 같은 error 색. 디자이너 확인 대기.
    PushEvent.TYPE_CHALLENGE_FORFEITED -> NotificationTypeVisual(
        icon = Icons.Filled.Flag,
        accent = ChallengeTheme.colorScheme.error,
    )
    // 뜻을 모르는 타입에 의미색을 주지 않는다.
    else -> NotificationTypeVisual(
        icon = Icons.Filled.Notifications,
        accent = ChallengeTheme.colorScheme.text.secondary,
    )
}
