package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.ranking.PreviewLoserRankingEntries
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import com.lwg.challenge.feature.ranking.contract.RankingTopThree
import com.lwg.challenge.feature.ranking.previewAsFirst
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Top3 카드. 받침대 높이가 다른 3열을 하단 정렬하면 단이 저절로 생긴다.
 * 1위 열을 위로 올리는 여백은 따로 주지 않는다 — 하단 정렬에서 이미 위로 뻗어 이중으로 붙는다.
 */
@Composable
internal fun RankingCard(
    topThree: RankingTopThree,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
        ) {
            RankingColumn(
                entry = topThree.second,
                avatarSize = 56.dp,
                avatarTextStyle = ChallengeTheme.typography.medium20,
                nameStyle = ChallengeTheme.typography.bold12,
                pedestalWidth = 64.dp,
                pedestalHeight = 64.dp,
                pedestalIcon = Icons.Filled.MilitaryTech,
                pedestalIconSize = 20.dp,
                pedestalIconTint = ChallengeTheme.colorScheme.onSurfaceVariant,
            )

            RankingColumn(
                entry = topThree.first,
                avatarSize = 64.dp,
                avatarTextStyle = ChallengeTheme.typography.medium24,
                nameStyle = ChallengeTheme.typography.bold14,
                pedestalWidth = 80.dp,
                pedestalHeight = 96.dp,
                pedestalIcon = Icons.Filled.LocalFireDepartment,
                pedestalIconSize = 24.dp,
                // 알파를 받침대 전체에 걸면 20% 배경이 8% 가 되어 안 보여 아이콘에만 건다.
                pedestalIconTint = ChallengeTheme.colorScheme.primary.copy(alpha = 0.40f),
                showCrown = true,
                avatarBrush = ChallengeTheme.brushes.fire,
                pedestalBrush = ChallengeTheme.brushes.fire,
                pedestalBrushAlpha = 0.20f,
            )

            RankingColumn(
                entry = topThree.third,
                avatarSize = 56.dp,
                avatarTextStyle = ChallengeTheme.typography.medium20,
                nameStyle = ChallengeTheme.typography.bold12,
                pedestalWidth = 64.dp,
                pedestalHeight = 48.dp,
                pedestalIcon = Icons.Filled.MilitaryTech,
                pedestalIconSize = 18.dp,
                pedestalIconTint = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PreviewRankingCard(
    firstStreak: Int = PreviewLoserRankingEntries.first().currentLossStreak,
    longFirstNickname: Boolean = false,
) {
    val entries = PreviewLoserRankingEntries.take(3).map { entry ->
        if (entry.rank == 1) entry.previewAsFirst(firstStreak, longFirstNickname) else entry
    }
    RankingCard(topThree = requireNotNull(RankingUiState.Data(entries).topThree))
}

@Preview
@Composable
private fun RankingCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            PreviewRankingCard()
        }
    }
}

/** 연패 중인 1위 — 1위 캡션에만 🐷N연패 절이 붙어 나머지 두 열과 형식이 갈린다. */
@Preview
@Composable
private fun RankingCardFirstWithStreakPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            PreviewRankingCard(firstStreak = 7)
        }
    }
}

@Preview
@Composable
private fun RankingCardLongNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            PreviewRankingCard(longFirstNickname = true)
        }
    }
}
