package com.lwg.challenge.feature.notification.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.notification.PreviewNotificationNow
import com.lwg.challenge.feature.notification.PreviewNotifications
import com.lwg.challenge.feature.notification.component.NotificationAppendLoading
import com.lwg.challenge.feature.notification.component.NotificationAppendRetry
import com.lwg.challenge.feature.notification.component.NotificationCard
import com.lwg.challenge.feature.notification.component.NotificationDateHeader
import com.lwg.challenge.feature.notification.component.notificationTypeVisualOf
import com.lwg.challenge.feature.notification.contract.NotificationAppendState
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.navigation.Route
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 알림 목록. 진입마다 다시 받는 화면이라 스태거 애니메이션을 넣지 않는다 — 갱신마다 카드가 밀려 올라와 읽던 자리를 잃는다.
 * 좌우 gutter 는 카드가 스크롤 인디케이터와 겹치지 않게 카드마다 준다.
 */
@Composable
internal fun NotificationScreen(
    uiState: NotificationUiState.Data,
    onNotificationClick: (Route) -> Unit,
    onLoadMore: () -> Unit,
) {
    val groups = uiState.dateGroups

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        groups.forEachIndexed { groupIndex, group ->
            item(key = group.headerKey) {
                NotificationDateHeader(
                    label = group.label,
                    // 첫 그룹의 위 여백은 contentPadding 이 이미 냈다.
                    modifier = Modifier.padding(top = if (groupIndex == 0) 0.dp else 16.dp),
                )
            }

            itemsIndexed(
                items = group.notifications,
                key = { _, notification -> notification.notificationId },
            ) { index, notification ->
                // 마지막 항목이 조성되는 순간을 "바닥에 닿았다" 로 알린다. 더 받을지는 ViewModel 이 판단한다.
                if (groupIndex == groups.lastIndex && index == group.notifications.lastIndex) {
                    LaunchedEffect(notification.notificationId) { onLoadMore() }
                }

                val visual = notificationTypeVisualOf(notification.type)
                val route = uiState.routeOf(notification)

                NotificationCard(
                    body = notification.body,
                    timeText = uiState.timeTextOf(notification),
                    icon = visual.icon,
                    accent = visual.accent,
                    onClick = route?.let { target -> { onNotificationClick(target) } },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }
        }

        // 끝에 도달하면 아무것도 그리지 않는다 — "여기까지" 를 알리지 않는다.
        when (uiState.append) {
            NotificationAppendState.Idle -> Unit
            NotificationAppendState.Loading -> item { NotificationAppendLoading() }
            NotificationAppendState.Failed -> item { NotificationAppendRetry(onClick = onLoadMore) }
        }
    }
}

/** 세 날짜에 걸친 목록. `오늘` 은 경과 시간, 나머지는 `HH:mm`. 같은 신청 타입이라도 대기 중인 행에만 chevron 이 붙는다. */
@Preview
@Composable
private fun NotificationScreenPreview() {
    NotificationScreenPreviewContainer(NotificationUiState.Data(PreviewNotifications, PreviewNotificationNow))
}

/** 다음 쪽 로딩. 목록은 그대로 두고 마지막에 인디케이터 한 줄만 붙는다. */
@Preview
@Composable
private fun NotificationScreenAppendingPreview() {
    NotificationScreenPreviewContainer(
        NotificationUiState.Data(PreviewNotifications, PreviewNotificationNow, NotificationAppendState.Loading),
    )
}

/** 다음 쪽 실패. 인디케이터 자리가 "다시 시도" 로 바뀐다. */
@Preview
@Composable
private fun NotificationScreenAppendFailedPreview() {
    NotificationScreenPreviewContainer(
        NotificationUiState.Data(PreviewNotifications, PreviewNotificationNow, NotificationAppendState.Failed),
    )
}

@Composable
private fun NotificationScreenPreviewContainer(uiState: NotificationUiState.Data) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            NotificationScreen(
                uiState = uiState,
                onNotificationClick = {},
                onLoadMore = {},
            )
        }
    }
}
