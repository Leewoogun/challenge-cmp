package com.lwg.challenge.feature.notification.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
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
 * 알림 타입을 아이콘·색으로 옮기는 **단일 출처**. design.md §3.2.
 *
 * 색이 [ChallengeTheme] 에서 오므로 `@Composable` 함수가 값을 반환하고 **화면이 호출해 결과를
 * 카드에 넘긴다** — 카드가 스스로 고르면 타입이 늘 때마다 카드를 고치게 되고, 그 `when` 이 아래
 * 폴백을 빠뜨린다. `challengeOutcomeVisualOf` 와 같은 형태다.
 *
 * `CHALLENGE_ACCEPTED` 에 [Icons.Filled.CheckCircle] 을 쓰지 않는 이유: `OPPONENT_VERIFIED` 와 같은
 * 아이콘이 된다. 둘은 한 챌린지에서 수락 → 인증 순으로 연달아 도착해 목록에 나란히 놓이므로,
 * 아이콘이 같으면 구분이 시각 한 줄에만 남는다. 수락 시점에 계약서가 존재하므로(수락=서명)
 * 앱이 계약서에 쓰는 [Icons.Filled.Description] 을 쓴다.
 *
 * 🔴 폴백에 `warning`·`success` 같은 의미색을 주지 않는다 — 뜻을 모르는데 없는 뜻을 지어내는 것이다.
 * 앱이 모르는 값을 받는 경로는 실재한다 — 서버 `NotificationType` enum 8종 중 발송되는 것은 4종이고,
 * 남은 `RESULT`·`TAUNT`·`REMIND`·`FRIEND_REQUEST` 는 이미 enum 안이라 서버의 강등(enum 밖 문자열 →
 * `CHALLENGE_REQUEST`)을 거치지 않고 원문 그대로 round-trip 해 온다. 그 타입의 발송이 열리는 날
 * 구버전 앱을 막아 주는 것은 이 폴백 말고 없다. 그래도 **목록에서 숨기지 않는다** — 숨기면 푸시로
 * 본 알림이 목록에 없고 홈 뱃지 개수와 목록 길이가 어긋난다. 읽을 수 있게 그리되 못 가는 곳으로
 * 보내지 않는 것이 폴백의 전부다.
 *
 * 🔴 그 강등이 나중에 고쳐지더라도 이 폴백을 같이 걷어내지 마라. 강등은 이 폴백의 근거가 아니라
 * 폴백을 일부 구간에서 무력화하던 쪽이고, 고쳐지면 폴백이 닿는 범위가 오히려 넓어진다.
 */
@Composable
internal fun notificationTypeVisualOf(type: String): NotificationTypeVisual = when (type) {
    PushEvent.TYPE_CHALLENGE_REQUEST -> NotificationTypeVisual(
        icon = Icons.Filled.SportsKabaddi,
        accent = ChallengeTheme.colorScheme.primary,
    )
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
    else -> NotificationTypeVisual(
        icon = Icons.Filled.Notifications,
        accent = ChallengeTheme.colorScheme.onSurfaceVariant,
    )
}
