package com.lwg.challenge.feature.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.ranking.component.LoserRankRow
import com.lwg.challenge.feature.ranking.component.PodiumCard
import com.lwg.challenge.feature.ranking.component.RankingEmptyState
import com.lwg.challenge.feature.ranking.component.RankingSectionTitle
import com.lwg.challenge.feature.ranking.component.RankingSubtitle
import com.lwg.challenge.feature.ranking.component.RankingTopBar
import com.lwg.challenge.feature.ranking.contract.LoserRankingItemState
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 개돼지 랭킹 탭 본문 — 부제 + 포디움(조건부) + 수치의 명단 + 빈 상태(조건부). design.md §1.0.
 *
 * 바텀바와 하단 인셋은 `MainScreen` 소관이라 여기서 그리지 않는다.
 */
@Composable
internal fun RankingScreen(
    uiState: RankingUiState.Data,
    onClickAddFriend: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            RankingSubtitle()
        }

        uiState.podium?.let { podium ->
            item {
                // spacedBy 8dp + 8dp = 정본의 mt-4(16dp).
                PodiumCard(podium = podium, modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (uiState.isRankListVisible) {
            item {
                // 정본의 mt-6(24dp) / mb-3(12dp) 를 spacedBy 8dp 와 합쳐 맞춘다.
                RankingSectionTitle(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            }

            items(items = uiState.entries, key = { it.userId }) { item ->
                LoserRankRow(item = item)
            }
        }

        if (uiState.isEmptyStateVisible) {
            item {
                RankingEmptyState(
                    onClickAddFriend = onClickAddFriend,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
internal fun RankingLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
    }
}

/**
 * [PreviewLoserRankingEntries] 를 앞에서부터 [count] 명 잘라 쓴다. 실서버 5행이 표시 분기를 모두 덮으므로
 * 기본값이 곧 대표 화면이다.
 *
 * @param firstStreak 1위의 연패 수. 실측 기본값이 0 이라 기본 프리뷰가 🐷 절 생략 쪽을 밟는다.
 * @param longFirstNickname 1위 닉네임을 긴 값으로. 실측 1위는 `isMe` 라 "나" 로 치환되므로 함께 남으로 뒤집는다.
 */
private fun previewData(
    count: Int = PreviewLoserRankingEntries.size,
    firstStreak: Int = PreviewLoserRankingEntries.first().currentLossStreak,
    longFirstNickname: Boolean = false,
): RankingUiState.Data = RankingUiState.Data(
    entries = PreviewLoserRankingEntries.take(count)
        .map { entry ->
            if (entry.rank == 1) entry.previewAsFirst(firstStreak, longFirstNickname) else entry
        }
        .map(::LoserRankingItemState),
)

@Composable
private fun RankingScreenPreviewBox(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            content()
        }
    }
}

/**
 * 실서버 5행 그대로 — 내가 1위(닉네임이 "나" 로 치환)이면서 연패 0 이고, 명단 끝에 기록 없는 사람이 선다.
 */
@Preview
@Composable
private fun RankingScreenPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(), onClickAddFriend = {})
    }
}

/** N=3 — 포디움이 서는 최소 인원. 빈 상태 카드가 사라지는 경계다. */
@Preview
@Composable
private fun RankingScreenThreeParticipantsPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 3), onClickAddFriend = {})
    }
}

/** N=2 — 포디움 없이 명단 2행 + 빈 상태 카드. */
@Preview
@Composable
private fun RankingScreenTwoParticipantsPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 2), onClickAddFriend = {})
    }
}

/** N=1 — 나 혼자. 신규 사용자의 기본 상태다 (design.md §3.2). */
@Preview
@Composable
private fun RankingScreenSingleParticipantPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 1), onClickAddFriend = {})
    }
}

/** N=0 — 서버가 빈 목록을 준 경우. 명단 섹션이 통째로 빠지고 빈 상태 카드만 남는다. */
@Preview
@Composable
private fun RankingScreenNoParticipantsPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 0), onClickAddFriend = {})
    }
}

/**
 * 연패 중인 1위 — 포디움 캡션에 🐷N연패 절이 붙고 명단 뱃지도 선다.
 *
 * 기본 프리뷰(실서버 데이터)의 1위가 연패 0 이라 반대쪽 분기는 그쪽이 덮는다. 정본에 있는 이 형태가
 * 프리뷰에서 통째로 빠지지 않도록 여기서만 연패를 올린다.
 */
@Preview
@Composable
private fun RankingScreenFirstWithStreakPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(firstStreak = 3), onClickAddFriend = {})
    }
}

/** 긴 닉네임 — 포디움 1위 열과 명단 1행이 동시에 걸린다. */
@Preview
@Composable
private fun RankingScreenLongNicknamePreview() {
    RankingScreenPreviewBox {
        RankingScreen(
            uiState = previewData(longFirstNickname = true),
            onClickAddFriend = {},
        )
    }
}

/**
 * 폰트 스케일 1.3x — 56dp 고정 상단바의 클리핑을 화면 단위로 확인하기 위해 [RankingTopBar] 를 함께 그린다.
 *
 * 부제를 상단바에서 본문으로 내린 판정(design.md §1.1)이 실제로 성립하는지 보는 프리뷰라
 * 상단바가 빠지면 확인 자체가 안 된다. 실제 화면에서 둘을 조립하는 것은 `RankingRoute` 다.
 */
@Preview
@Composable
private fun RankingScreenLargeFontScalePreview() {
    ChallengeTheme {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density = density.density, fontScale = 1.3f),
        ) {
            Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
                RankingTopBar()
                RankingScreen(uiState = previewData(), onClickAddFriend = {})
            }
        }
    }
}

@Preview
@Composable
private fun RankingLoadingScreenPreview() {
    RankingScreenPreviewBox {
        RankingLoadingScreen()
    }
}
