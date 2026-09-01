package com.lwg.challenge.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.notification.component.NotificationAppendLoading
import com.lwg.challenge.feature.notification.component.NotificationAppendRetry
import com.lwg.challenge.feature.notification.component.NotificationCard
import com.lwg.challenge.feature.notification.component.NotificationDateHeader
import com.lwg.challenge.feature.notification.component.NotificationEmptyState
import com.lwg.challenge.feature.notification.component.notificationTypeVisualOf
import com.lwg.challenge.feature.notification.contract.NotificationAppendState
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.ui.components.EmptyStateCard
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 알림 목록 본문. design.md §1.
 *
 * 진입 애니메이션을 넣지 않는다. 스태거는 **한 번 그려지는 목록**의 어휘인데, 이 화면은 진입할
 * 때마다 최신 상태를 다시 받으므로 갱신마다 카드가 순차로 밀려 올라오면 읽던 자리를 잃는다.
 * 홈·친구·랭킹·보관함 네 목록 화면이 이미 전부 애니메이션 없이 출시됐다.
 */
@Composable
internal fun NotificationScreen(
    uiState: NotificationUiState,
    onNotificationClick: (Route) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
) {
    when (uiState) {
        NotificationUiState.Loading -> NotificationLoadingScreen()

        is NotificationUiState.Data -> NotificationList(
            uiState = uiState,
            onNotificationClick = onNotificationClick,
            onLoadMore = onLoadMore,
        )

        NotificationUiState.Empty -> NotificationEmptyState()

        NotificationUiState.Failure -> NotificationFailureScreen(onRetryClick = onRetryClick)
    }
}

/**
 * 상한 없이 쌓이는 유일한 목록이라 `LazyColumn` 을 쓴다 — 마이페이지가 `Column(verticalScroll)` 을
 * 쓴 근거("항목이 고정 5개 이하")가 여기엔 없다.
 *
 * 좌우 gutter 를 `contentPadding` 이 아니라 카드마다 넣는 이유는 보관함의 판단을 승계한다 —
 * 카드가 스크롤 인디케이터와 겹치지 않게 카드 자신이 여백을 낸다. 항목 간격 8dp 는 카드의
 * vertical 4dp 가 맞물려 만든다.
 *
 * 날짜 그룹은 `NotificationUiState.Data` 가 **누적 목록 전체에서** 확정해 넘겨준다 — 여기서 쪽을
 * 보지 않는 것이 같은 헤더가 두 번 찍히지 않는 이유다 (design.md §3.6.3).
 */
@Composable
private fun NotificationList(
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
                    // 첫 그룹의 위 여백은 contentPadding 이 이미 냈다. 여기서 또 주면 두 배가 된다.
                    modifier = Modifier.padding(top = if (groupIndex == 0) 0.dp else 16.dp),
                )
            }

            itemsIndexed(
                items = group.items,
                key = { _, item -> item.notificationId },
            ) { index, item ->
                // 마지막 항목이 조성되는 순간이 곧 "바닥에 닿았다" 는 신호다. 화면은 커서를 보지 않고
                // 그 사실만 알린다 — 더 받을 것이 남았는지, 이미 받는 중인지는 ViewModel 이 판정한다.
                if (groupIndex == groups.lastIndex && index == group.items.lastIndex) {
                    LaunchedEffect(item.notificationId) { onLoadMore() }
                }

                // 타입 → 아이콘·색 판정을 카드가 아니라 여기서 끝낸다 (design.md §3.2.1).
                val visual = notificationTypeVisualOf(item.type)

                NotificationCard(
                    body = item.body,
                    timeText = item.timeText,
                    icon = visual.icon,
                    accent = visual.accent,
                    onClick = item.route?.let { target -> { onNotificationClick(target) } },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }
        }

        // 마지막 그룹 뒤에 오고 자기 헤더를 갖지 않는다 — 푸터는 알림이 아니다 (design.md §3.6.4).
        // 끝에 도달하면 인디케이터가 사라지고 목록이 그냥 끝난다 — "여기까지" 를 그리지 않는다.
        when (uiState.append) {
            NotificationAppendState.Idle -> Unit
            NotificationAppendState.Loading -> item { NotificationAppendLoading() }
            NotificationAppendState.Failed -> item { NotificationAppendRetry(onClick = onLoadMore) }
        }
    }
}

/**
 * 첫 조회 실패 본문. design.md §6.1.
 *
 * 0건과 실패를 뭉개지 않는다 — 빈 상태로 그리면 *"알림이 없다"* 와 *"알림을 못 불러왔다"* 가 같아
 * 보이는데, 둘은 사용자가 취할 행동이 정반대다. 아이콘까지 빈 상태와 다르게 두는 이유도 같다.
 */
@Composable
private fun NotificationFailureScreen(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        EmptyStateCard(
            title = "알림을 불러오지 못했어요",
            subtitle = "잠시 후 다시 시도해주세요",
            ctaLabel = "다시 시도",
            icon = Icons.Filled.CloudOff,
            onClickCta = onRetryClick,
            // 기본값이 PersonAdd 라 그냥 두면 "다시 시도" 에 친구 추가 아이콘이 붙는다.
            ctaIcon = Icons.Filled.Refresh,
        )
    }
}

@Composable
private fun NotificationLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
    }
}

/**
 * 🔴 **세 날짜에 걸친 목록** — `오늘`·`어제`·`8월 28일` 헤더가 한 화면에 있다. 이 개정의 요지 둘을
 * 한 그림에서 본다.
 *
 * ① 헤더가 날짜를 말하니 **행은 그 날 안에서 언제인지만** 말한다 — `오늘` 그룹은 `방금 전`·`N분 전`,
 * 아래 두 그룹은 `HH:mm` 이다.
 * ② **chevron 이 붙는 행은 셋뿐**인데 색도 문구도 나머지와 같다 — *"갈 곳이 있는가"* 는 표시와 독립된
 * 축이다. `오늘` 그룹의 신청 두 건이 **같은 타입인데 갈리는 것**이 목록 탭 게이트다.
 *
 * 발송 4종 + 2줄 메시지도 그대로 들어 있어 아이콘 구분과 `Alignment.Top` 정렬을 함께 본다.
 */
@Preview
@Composable
private fun NotificationScreenPreview() {
    NotificationScreenPreviewContainer(NotificationUiState.Data(PreviewNotifications))
}

/** 0건. CTA 도 카드 크롬도 없는 담담한 사실 한 장이다. */
@Preview
@Composable
private fun NotificationScreenEmptyPreview() {
    NotificationScreenPreviewContainer(NotificationUiState.Empty)
}

/** 첫 조회 실패 — "다시 시도" 가 유일한 복구 수단이다. 위 빈 상태와 아이콘부터 갈린다. */
@Preview
@Composable
private fun NotificationScreenFailurePreview() {
    NotificationScreenPreviewContainer(NotificationUiState.Failure)
}

@Preview
@Composable
private fun NotificationScreenLoadingPreview() {
    NotificationScreenPreviewContainer(NotificationUiState.Loading)
}

/**
 * 다음 쪽 로딩. 🔴 **목록이 그대로 남아 있는 것**이 이 프리뷰의 요지다 — 화면을 덮는 로딩이었다면
 * 위 카드들이 사라졌을 것이다. 마지막 카드 아래 20dp 인디케이터 한 줄만 늘어난다.
 */
@Preview
@Composable
private fun NotificationScreenAppendingPreview() {
    NotificationScreenPreviewContainer(
        NotificationUiState.Data(PreviewNotifications, NotificationAppendState.Loading),
    )
}

/** 다음 쪽 실패. 인디케이터 자리가 "다시 시도" 로 바뀔 뿐, 읽던 목록은 하나도 사라지지 않는다. */
@Preview
@Composable
private fun NotificationScreenAppendFailedPreview() {
    NotificationScreenPreviewContainer(
        NotificationUiState.Data(PreviewNotifications, NotificationAppendState.Failed),
    )
}

@Composable
private fun NotificationScreenPreviewContainer(uiState: NotificationUiState) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            NotificationScreen(
                uiState = uiState,
                onNotificationClick = {},
                onRetryClick = {},
                onLoadMore = {},
            )
        }
    }
}
