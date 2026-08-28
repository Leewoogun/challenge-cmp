package com.lwg.challenge.feature.mypage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.mypage.component.ArchiveChallengeCard
import com.lwg.challenge.feature.mypage.component.ArchiveMonthHeader
import com.lwg.challenge.feature.mypage.contract.ArchiveUiState
import com.lwg.challenge.feature.mypage.contract.toArchiveMonths
import com.lwg.challenge.ui.components.EmptyStateCard
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약서 보관함 본문. design.md §2.3.
 *
 * 좌우 gutter 를 `contentPadding` 이 아니라 항목마다 넣는 이유: 월 헤더가 `stickyHeader` 라
 * 배경이 화면 끝까지 닿아야 스크롤되는 카드가 헤더 옆으로 비치지 않는다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ArchiveScreen(
    uiState: ArchiveUiState.Data,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        if (uiState.isEmpty) {
            item {
                EmptyStateCard(
                    title = "아직 끝낸 챌린지가 없어요",
                    subtitle = "챌린지가 끝나면 계약서가 여기 쌓여요",
                    ctaLabel = "챌린지 만들기",
                    icon = Icons.Filled.Description,
                    onClickCta = onCreateChallengeClick,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    // 기본값이 PersonAdd 라 그냥 두면 "챌린지 만들기" 에 친구 추가 아이콘이 붙는다.
                    ctaIcon = Icons.Filled.SportsKabaddi,
                )
            }
        }

        uiState.months.forEachIndexed { index, month ->
            stickyHeader(key = month.key) {
                ArchiveMonthHeader(
                    label = month.label,
                    countText = month.countText,
                    // 카드의 하단 12dp 와 합쳐 월 섹션 사이 24dp. 첫 섹션은 contentPadding 이 낸다.
                    topSpacing = if (index == 0) 0.dp else 12.dp,
                )
            }

            items(items = month.challenges, key = { it.challengeId }) { item ->
                ArchiveChallengeCard(
                    item = item,
                    onClick = { onChallengeClick(item.challengeId) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                )
            }
        }
    }
}

/**
 * 조회 실패 본문. design.md §2.5.4.1.
 *
 * 빈 상태 카드와 골격을 공유하되 아이콘까지 바꾸는 이유: 둘 다 "목록이 없는 화면" 이라 문구만 다르면
 * 0건인지 못 불러온 건지 한눈에 갈리지 않는다.
 */
@Composable
internal fun ArchiveFailureScreen(onRetryClick: () -> Unit) {
    // 빈 상태 카드와 같은 자리에 놓는다(LazyColumn 의 contentPadding + 항목 gutter 와 동일) —
    // 재시도로 상태가 바뀔 때 카드가 다른 높이에서 다시 그려지면 튄다.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        EmptyStateCard(
            title = "보관함을 불러오지 못했어요",
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
internal fun ArchiveLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
    }
}

@Preview
@Composable
private fun ArchiveLoadingScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveLoadingScreen()
        }
    }
}

@Preview
@Composable
private fun ArchiveFailureScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveFailureScreen(onRetryClick = {})
        }
    }
}

@Preview
@Composable
private fun ArchiveScreenEmptyPreview() {
    ArchiveScreenPreviewContainer(ArchiveUiState.Data(months = emptyList()))
}

@Preview
@Composable
private fun ArchiveScreenSingleMonthPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState.Data(months = PreviewArchiveHistories.take(1).toArchiveMonths()),
    )
}

/** 3개월(8·7·6월) — 6월 카드에 탈퇴한 상대, 7월에 말일(7/31) 챌린지, 결과 4종이 모두 나온다. */
@Preview
@Composable
private fun ArchiveScreenMultiMonthPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState.Data(months = PreviewArchiveHistories.toArchiveMonths()),
    )
}

@Preview
@Composable
private fun ArchiveScreenLongTextPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState.Data(months = PreviewLongTextArchiveHistories.toArchiveMonths()),
    )
}

@Composable
private fun ArchiveScreenPreviewContainer(uiState: ArchiveUiState.Data) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveScreen(
                uiState = uiState,
                onChallengeClick = {},
                onCreateChallengeClick = {},
            )
        }
    }
}
