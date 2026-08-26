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
import com.lwg.challenge.feature.ranking.contract.LoserRankingItemState
import com.lwg.challenge.feature.ranking.contract.PodiumState
import com.lwg.challenge.feature.ranking.previewAsFirst
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Top3 포디움 카드. 참가자 3명 이상일 때만 호출된다.
 *
 * 받침대 높이(96/64/48dp)가 다른 3열을 하단 정렬하면 단이 저절로 생긴다 — Lovable 의 `items-end` 가
 * 하는 일이 그대로 [Alignment.Bottom] 이다. 1위 열의 `-mt-4` 는 옮기지 않는다. 하단 정렬 상태에서는
 * 키 큰 열이 이미 위로 뻗으므로 옮기면 16dp 가 이중으로 붙는다. (design.md §1.2)
 */
@Composable
internal fun PodiumCard(
    podium: PodiumState,
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
            PodiumColumn(
                item = podium.second,
                avatarSize = 56.dp,
                avatarTextStyle = ChallengeTheme.typography.medium20,
                nameStyle = ChallengeTheme.typography.bold12,
                pedestalWidth = 64.dp,
                pedestalHeight = 64.dp,
                pedestalIcon = Icons.Filled.MilitaryTech,
                pedestalIconSize = 20.dp,
                pedestalIconTint = ChallengeTheme.colorScheme.onSurfaceVariant,
            )

            PodiumColumn(
                item = podium.first,
                avatarSize = 64.dp,
                avatarTextStyle = ChallengeTheme.typography.medium24,
                nameStyle = ChallengeTheme.typography.bold14,
                pedestalWidth = 80.dp,
                pedestalHeight = 96.dp,
                pedestalIcon = Icons.Filled.LocalFireDepartment,
                pedestalIconSize = 24.dp,
                // opacity-40 은 받침대 전체가 아니라 아이콘에만 건다 — 전체에 걸면 20% 배경이 8% 가
                // 되어 다시 안 보인다. (design.md §1.2.4)
                pedestalIconTint = ChallengeTheme.colorScheme.primary.copy(alpha = 0.40f),
                showCrown = true,
                avatarBrush = ChallengeTheme.brushes.fire,
                pedestalBrush = ChallengeTheme.brushes.fire,
                pedestalBrushAlpha = 0.20f,
            )

            PodiumColumn(
                item = podium.third,
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

/** 실서버 5행의 앞 3행이 그대로 포디움 세 열이다. */
private fun previewPodium(
    firstStreak: Int = PreviewLoserRankingEntries.first().currentLossStreak,
    longFirstNickname: Boolean = false,
): PodiumState {
    val items = PreviewLoserRankingEntries.take(3)
        .map { entry ->
            if (entry.rank == 1) entry.previewAsFirst(firstStreak, longFirstNickname) else entry
        }
        .map(::LoserRankingItemState)

    return PodiumState(first = items[0], second = items[1], third = items[2])
}

/** 실측 그대로 — 내가 1위라 이름·이니셜이 "나" 가 되고, 연패 0 이라 세 열의 캡션 형식이 완전히 같아진다. */
@Preview
@Composable
private fun PodiumCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            PodiumCard(podium = previewPodium())
        }
    }
}

/** 연패 중인 1위 — 1위 캡션에만 🐷N연패 절이 붙어 나머지 두 열과 형식이 갈린다. */
@Preview
@Composable
private fun PodiumCardFirstWithStreakPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            PodiumCard(podium = previewPodium(firstStreak = 7))
        }
    }
}

@Preview
@Composable
private fun PodiumCardLongNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            PodiumCard(podium = previewPodium(longFirstNickname = true))
        }
    }
}
