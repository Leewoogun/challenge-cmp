package com.lwg.challenge.feature.notification.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.feature.notification.PreviewLongBodyNotification
import com.lwg.challenge.feature.notification.PreviewNotificationNow
import com.lwg.challenge.feature.notification.PreviewSettledRequestNotification
import com.lwg.challenge.feature.notification.PreviewUnknownTypeNotification
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.feature.notification.notificationPreviewItem
import com.lwg.challenge.push.PushEvent
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 알림 카드. 아이콘·색은 [notificationTypeVisualOf] 가 정한 값을 받는다.
 *
 * @param onClick null 이면 탭도 chevron 도 없다. 흐리게 하지는 않는다 — 정상 수신된 알림이 고장난 것처럼 보인다.
 */
@Composable
internal fun NotificationCard(
    body: String,
    timeText: String,
    icon: ImageVector,
    accent: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            // CenterVertically 면 2줄 메시지에서 아이콘이 두 줄 사이에 뜬다.
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChallengeTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = accent,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                // 길이가 튀는 원인이 닉네임이라 줄 수를 자르면 누가 한 일인지 모르게 된다.
                Text(
                    text = body,
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.onBackground,
                )

                VerticalSpacer(4.dp)

                Text(
                    text = timeText,
                    style = ChallengeTheme.typography.medium10,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    // Row 가 Top 정렬이라 그대로 두면 chevron 이 첫 줄만 가리켜 보인다.
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(16.dp),
                    tint = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 아는 타입 4종의 아이콘·색이 서로 갈리는지 본다. */
@Preview
@Composable
private fun NotificationCardTypesPreview() {
    NotificationCardPreviewContainer {
        PreviewCard(
            notificationPreviewItem(
                type = PushEvent.TYPE_CHALLENGE_REQUEST,
                body = "민수과 계약을 하시렵니까?",
            ),
        )
        PreviewCard(
            notificationPreviewItem(
                notificationId = 2L,
                type = PushEvent.TYPE_CHALLENGE_ACCEPTED,
                body = "준혁님이 수락했습니다",
                referencePending = null,
            ),
        )
        PreviewCard(
            notificationPreviewItem(
                notificationId = 3L,
                type = PushEvent.TYPE_CHALLENGE_REJECTED,
                body = "도윤님이 도망쳤습니다",
                referencePending = null,
            ),
        )
        PreviewCard(
            notificationPreviewItem(
                notificationId = 4L,
                type = PushEvent.TYPE_OPPONENT_VERIFIED,
                body = "민지님이 인증 사진을 올렸습니다",
                referencePending = null,
            ),
        )
    }
}

/** 모르는 타입. 무채색 아이콘에 탭은 없지만 메시지·시각은 그대로 읽힌다. */
@Preview
@Composable
private fun NotificationCardUnknownTypePreview() {
    NotificationCardPreviewContainer {
        PreviewCard(PreviewUnknownTypeNotification)
    }
}

/** 메시지 2줄. 아이콘은 첫 줄에, chevron 은 세로 가운데에 붙는다. */
@Preview
@Composable
private fun NotificationCardTwoLineBodyPreview() {
    NotificationCardPreviewContainer {
        PreviewCard(PreviewLongBodyNotification)
    }
}

/** 대기 중 신청에만 chevron 이 있다. 응답이 끝난 신청과 모르는 타입은 chevron 자리도 비우지 않는다. */
@Preview
@Composable
private fun NotificationCardChevronPreview() {
    NotificationCardPreviewContainer {
        PreviewCard(notificationPreviewItem(body = "민수과 계약을 하시렵니까?"))
        PreviewCard(PreviewSettledRequestNotification)
        PreviewCard(PreviewUnknownTypeNotification)
    }
}

@Composable
private fun PreviewCard(notification: Notification) {
    val data = NotificationUiState.Data(listOf(notification), PreviewNotificationNow)
    val visual = notificationTypeVisualOf(notification.type)
    NotificationCard(
        body = notification.body,
        timeText = data.timeTextOf(notification),
        icon = visual.icon,
        accent = visual.accent,
        onClick = if (data.routeOf(notification) != null) PreviewNoOpClick else null,
    )
}

private val PreviewNoOpClick: () -> Unit = {}

@Composable
private fun NotificationCardPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}
