package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.feature.ranking.PREVIEW_LONG_NICKNAME
import com.lwg.challenge.feature.ranking.PreviewLoserRankingEntries
import com.lwg.challenge.feature.ranking.contract.LoserRankingItemState
import com.lwg.challenge.feature.ranking.contract.RankEmphasis
import com.lwg.challenge.ui.components.ProfileImage
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 수치의 명단 1행.
 *
 * rank 숫자는 1위(error)·3위(warning)만 강조되고 2위는 4위 이하와 같은 muted 다. 카드 보더가
 * `rank <= 3` 로 셋을 묶는 것과 축이 어긋나 보이지만 Lovable 정본이 그렇다. (design.md §1.3.1)
 */
@Composable
internal fun LoserRankRow(
    item: LoserRankingItemState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (item.isMe) {
            ChallengeTheme.colorScheme.surfaceVariant
        } else {
            ChallengeTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (item.isTopThree) {
                ChallengeTheme.colorScheme.error.copy(alpha = 0.20f)
            } else {
                ChallengeTheme.colorScheme.outline
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = item.rankText,
                style = ChallengeTheme.typography.bold14,
                color = when (item.rankEmphasis) {
                    RankEmphasis.FIRST -> ChallengeTheme.colorScheme.error
                    RankEmphasis.THIRD -> ChallengeTheme.colorScheme.warning
                    RankEmphasis.PLAIN -> ChallengeTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp),
            )

            ProfileImage(
                imageUrl = item.profileImageUrl,
                nickname = item.displayName,
                size = 40.dp,
                shape = RoundedCornerShape(16.dp),
                textStyle = ChallengeTheme.typography.medium16,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = ChallengeTheme.typography.bold14,
                    color = ChallengeTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.listCaptionText,
                    style = ChallengeTheme.typography.medium10,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            if (item.hasStreakBadge) {
                Text(
                    text = item.streakBadgeText,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.error,
                )
            }
        }
    }
}

/** 실서버 5행에서 [rank] 행을 꺼내 [tweak] 로 필요한 필드만 뒤집는다. */
private fun previewItem(
    rank: Int,
    tweak: LoserRankingEntry.() -> LoserRankingEntry = { this },
): LoserRankingItemState = LoserRankingItemState(PreviewLoserRankingEntries[rank - 1].tweak())

@Composable
private fun LoserRankRowPreviewBox(content: @Composable () -> Unit) {
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

/** 실서버 4행 — rank 색 1·3위 강조와 함께 1위 내 행("나" + surfaceVariant), 2·4위 연패 뱃지가 같이 보인다. */
@Preview
@Composable
private fun LoserRankRowRankColorsPreview() {
    LoserRankRowPreviewBox {
        LoserRankRow(item = previewItem(rank = 1))
        LoserRankRow(item = previewItem(rank = 2))
        LoserRankRow(item = previewItem(rank = 3))
        LoserRankRow(item = previewItem(rank = 4))
    }
}

/** 연패 0 — 뱃지가 통째로 사라진다. */
@Preview
@Composable
private fun LoserRankRowWithoutStreakPreview() {
    LoserRankRowPreviewBox {
        LoserRankRow(item = previewItem(rank = 3))
    }
}

/**
 * 챌린지 0회 — 캡션이 "아직 기록 없음" 으로 통째로 대체된다. 바로 아래는 같은 행에서 참가 횟수만 올린
 * 전승 0패다. 나머지 필드가 전부 같아도 캡션만 갈린다는 대조라 두 줄을 붙여 둔다.
 */
@Preview
@Composable
private fun LoserRankRowNoRecordPreview() {
    LoserRankRowPreviewBox {
        LoserRankRow(item = previewItem(rank = 5))
        LoserRankRow(item = previewItem(rank = 5) { copy(totalChallenges = 12) })
    }
}

/** 내 행이 top3 밖일 때 — 보더는 outline 인데 배경만 surfaceVariant 로 올라간다. */
@Preview
@Composable
private fun LoserRankRowMePreview() {
    LoserRankRowPreviewBox {
        LoserRankRow(item = previewItem(rank = 4) { copy(isMe = true) })
    }
}

@Preview
@Composable
private fun LoserRankRowLongNicknamePreview() {
    LoserRankRowPreviewBox {
        LoserRankRow(item = previewItem(rank = 3) { copy(nickname = PREVIEW_LONG_NICKNAME) })
    }
}
