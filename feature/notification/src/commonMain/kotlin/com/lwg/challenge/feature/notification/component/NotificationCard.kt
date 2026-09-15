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
import com.lwg.challenge.feature.notification.PreviewLongBodyNotification
import com.lwg.challenge.feature.notification.PreviewSettledRequestNotification
import com.lwg.challenge.feature.notification.PreviewUnknownTypeNotification
import com.lwg.challenge.feature.notification.contract.NotificationItemState
import com.lwg.challenge.feature.notification.notificationPreviewItem
import com.lwg.challenge.push.PushEvent
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 알림 카드. design.md §3.
 *
 * **아이콘·색을 스스로 고르지 않는다** — 이미 결정된 값을 받는다. 카드가 타입을 보고 고르면 타입이
 * 늘 때마다 카드를 고치게 되고, 그 `when` 이 폴백을 빠뜨린다. 판정은 [notificationTypeVisualOf]
 * 한 곳에 있고 화면이 호출한다.
 *
 * @param onClick null 이면 `clickable` 을 아예 붙이지 않고 chevron 도 그리지 않는다 — 목적지를
 *   모르는데 누르게 하면 아무 일도 안 일어나거나 엉뚱한 데로 간다. 🔴 흐리게 처리하지는 않는다.
 *   톤을 낮추면 정상 수신된 알림이 **고장난 알림**처럼 보인다. 리플이 안 뜰 뿐이고, 모르는 타입은
 *   무채색 아이콘이라 이미 톤이 낮다.
 *
 *   chevron 은 그 반대편을 채운다 — *"누를 수 없다"* 를 표시하는 대신 **누를 수 있다는 것만**
 *   표시한다(design.md §3.5). 그래서 비탭 카드에는 **자리도 예약하지 않는다**: 텍스트 컬럼이 그만큼
 *   넓어질 뿐이고, 메시지가 좌측 정렬이라 눈이 따라가는 왼쪽 모서리는 어차피 같다.
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
            // 메시지가 2줄이 되면 아이콘 박스가 위에 붙는다. CenterVertically 로 두면 아이콘이
            // 두 줄 사이 허공에 뜬다.
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
                // 줄 수 상한을 두지 않는다 — 길이가 튀는 원인이 닉네임 하나라 자르면 하필 닉네임이
                // 잘려 누가 한 일인지 모르는 알림이 된다.
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
                    // Row 기본 정렬이 Top 이라 그대로 두면 2줄 메시지에서 chevron 이 첫 줄에 붙는다.
                    // 카드 전체를 가리키는 기호가 한 줄만 가리키는 것처럼 보인다.
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(16.dp),
                    tint = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * 발송 5종 중 4종(2026-09-05 기준). 아이콘·색이 서로 갈리는지, 수락과 인증이 나란히 놓였을 때
 * 구분되는지 본다. 기권(`CHALLENGE_FORFEITED`)은 여기 없고 `NotificationScreenPreview` 의
 * `PreviewNotifications` 에만 들어 있다.
 */
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

/**
 * 모르는 타입. 무채색 아이콘 + 탭 비활성인데도 **메시지·시각은 그대로 읽힌다** — 폴백이 하는 일이
 * 그게 전부다.
 */
@Preview
@Composable
private fun NotificationCardUnknownTypePreview() {
    NotificationCardPreviewContainer {
        PreviewCard(PreviewUnknownTypeNotification)
    }
}

/**
 * 메시지 2줄. 아이콘이 첫 줄에 붙는지(`Alignment.Top`), **chevron 만 세로 중앙에 있는지** 확인하는
 * 자리다. chevron 이 Top 이면 아이콘과 같은 높이에 붙어 카드 전체가 아니라 첫 줄을 가리켜 보인다.
 */
@Preview
@Composable
private fun NotificationCardTwoLineBodyPreview() {
    NotificationCardPreviewContainer {
        PreviewCard(PreviewLongBodyNotification)
    }
}

/**
 * 🔴 목적지 축의 대비. 첫 카드에만 chevron 이 있고 나머지는 **그 자리가 비지 않는다** — 텍스트가
 * 그만큼 넓게 쓸 뿐이다. 세 카드의 색·문구가 똑같은 것도 요지다(톤 다운 금지, design.md §3.5.3).
 *
 * 위 둘은 **같은 타입인데 갈린다** — 응답을 기다리는 신청만 갈 곳이 있다. 셋째는 타입 자체를 몰라
 * 갈린다. chevron 이 죽는 이유가 둘이고, 카드는 그 이유를 구분해 그리지 않는다.
 */
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
private fun PreviewCard(item: NotificationItemState) {
    val visual = notificationTypeVisualOf(item.type)
    NotificationCard(
        body = item.body,
        timeText = item.timeText,
        icon = visual.icon,
        accent = visual.accent,
        onClick = if (item.isTappable) PreviewNoOpClick else null,
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
