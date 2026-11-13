package com.lwg.challenge.feature.ranking.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.ranking.PreviewLoserRankingEntries
import com.lwg.challenge.feature.ranking.component.LoserRankRow
import com.lwg.challenge.feature.ranking.component.RankingCard
import com.lwg.challenge.feature.ranking.component.RankingEmptyState
import com.lwg.challenge.feature.ranking.component.RankingSectionTitle
import com.lwg.challenge.feature.ranking.component.RankingSubtitle
import com.lwg.challenge.feature.ranking.component.RankingTopBar
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import com.lwg.challenge.feature.ranking.previewAsFirst

/** 개돼지 랭킹 본문 — 부제 + Top3 카드(3명 이상) + 명단 + 빈 상태(3명 미만). 바텀바·하단 인셋은 MainScreen 소관이다. */
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

        uiState.topThree?.let { topThree ->
            item {
                // spacedBy 8dp 와 합쳐 위 16dp.
                RankingCard(topThree = topThree, modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (uiState.isRankListVisible) {
            item {
                // spacedBy 8dp 와 합쳐 위 24dp · 아래 12dp.
                RankingSectionTitle(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            }

            items(items = uiState.entries, key = { it.userId }) { entry ->
                LoserRankRow(entry = entry)
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

/** 실서버 5행을 앞에서 [count] 명 잘라 쓴다. 1위만 연패·긴 닉네임으로 뒤집을 수 있다. */
private fun previewData(
    count: Int = PreviewLoserRankingEntries.size,
    firstStreak: Int = PreviewLoserRankingEntries.first().currentLossStreak,
    longFirstNickname: Boolean = false,
): RankingUiState.Data = RankingUiState.Data(
    entries = PreviewLoserRankingEntries.take(count)
        .map { entry ->
            if (entry.rank == 1) entry.previewAsFirst(firstStreak, longFirstNickname) else entry
        }
)

@Composable
private fun RankingScreenPreviewBox(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            content()
        }
    }
}

/** 실서버 5행 — 내가 1위("나")이면서 연패 0, 명단 끝에 기록 없는 사람. */
@ThemePreviews
@Composable
private fun RankingScreenPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(), onClickAddFriend = {})
    }
}

/** N=3 — Top3 카드가 생기는 최소 인원. 빈 상태 카드가 사라지는 경계다. */
@ThemePreviews
@Composable
private fun RankingScreenThreeParticipantsPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 3), onClickAddFriend = {})
    }
}

/** N=2 — Top3 카드 없이 명단 2행 + 빈 상태 카드. */
@ThemePreviews
@Composable
private fun RankingScreenTwoParticipantsPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 2), onClickAddFriend = {})
    }
}

/** N=1 — 나 혼자. 신규 사용자의 기본 상태다. */
@ThemePreviews
@Composable
private fun RankingScreenSingleParticipantPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 1), onClickAddFriend = {})
    }
}

/** N=0 — 서버가 빈 목록을 준 경우. 명단 섹션이 통째로 빠지고 빈 상태 카드만 남는다. */
@ThemePreviews
@Composable
private fun RankingScreenNoParticipantsPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(count = 0), onClickAddFriend = {})
    }
}

/** 연패 중인 1위 — Top3 카드 캡션에 🐷N연패 절이 붙고 명단 뱃지도 선다. */
@ThemePreviews
@Composable
private fun RankingScreenFirstWithStreakPreview() {
    RankingScreenPreviewBox {
        RankingScreen(uiState = previewData(firstStreak = 3), onClickAddFriend = {})
    }
}

/** 긴 닉네임 — Top3 카드 1위 열과 명단 1행이 동시에 걸린다. */
@ThemePreviews
@Composable
private fun RankingScreenLongNicknamePreview() {
    RankingScreenPreviewBox {
        RankingScreen(
            uiState = previewData(longFirstNickname = true),
            onClickAddFriend = {},
        )
    }
}

/** 폰트 1.3배 — 56dp 고정 상단바가 잘리지 않는지 상단바와 함께 본다. */
@ThemePreviews
@Composable
private fun RankingScreenLargeFontScalePreview() {
    ChallengeTheme {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density = density.density, fontScale = 1.3f),
        ) {
            Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
                RankingTopBar()
                RankingScreen(uiState = previewData(), onClickAddFriend = {})
            }
        }
    }
}
