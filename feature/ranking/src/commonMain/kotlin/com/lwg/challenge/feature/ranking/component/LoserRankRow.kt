package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.feature.ranking.PREVIEW_LONG_NICKNAME
import com.lwg.challenge.feature.ranking.PreviewLoserRankingEntries
import com.lwg.challenge.feature.ranking.contract.displayName
import com.lwg.challenge.feature.ranking.contract.isTopThree
import com.lwg.challenge.feature.ranking.contract.listCaption
import com.lwg.challenge.feature.ranking.contract.streakBadge
import com.lwg.challenge.ui.components.ProfileImage

/** 수치의 명단 1행. */
@Composable
internal fun LoserRankRow(
    entry: LoserRankingEntry,
    modifier: Modifier = Modifier,
) {
    val streakBadge = entry.streakBadge

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (entry.isMe) {
            ChallengeTheme.colorScheme.background.cardSub
        } else {
            ChallengeTheme.colorScheme.background.card
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (entry.isTopThree) {
                ChallengeTheme.colorScheme.error.copy(alpha = 0.20f)
            } else {
                ChallengeTheme.colorScheme.line.divider
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RankingNumberLabel(rank = entry.rank)

            ProfileImage(
                imageUrl = entry.profileImageUrl,
                nickname = entry.displayName,
                size = 40.dp,
                shape = RoundedCornerShape(16.dp),
                textStyle = ChallengeTheme.typography.medium16,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    style = ChallengeTheme.typography.bold14,
                    color = ChallengeTheme.colorScheme.text.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.listCaption,
                    style = ChallengeTheme.typography.medium10,
                    color = ChallengeTheme.colorScheme.text.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            if (streakBadge != null) {
                Text(
                    text = streakBadge,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.error,
                )
            }
        }
    }
}

/** 실서버 5행에서 [rank] 행을 꺼내 [tweak] 로 필요한 필드만 뒤집어 그린다. */
@Composable
private fun PreviewRow(
    rank: Int,
    tweak: LoserRankingEntry.() -> LoserRankingEntry = { this },
) {
    LoserRankRow(entry = PreviewLoserRankingEntries[rank - 1].tweak())
}

@Composable
private fun LoserRankRowPreviewBox(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

/** 1·3위 rank 강조, 1위 내 행("나" + surfaceVariant), 2·4위 연패 뱃지. */
@ThemePreviews
@Composable
private fun LoserRankRowRankColorsPreview() {
    LoserRankRowPreviewBox {
        PreviewRow(rank = 1)
        PreviewRow(rank = 2)
        PreviewRow(rank = 3)
        PreviewRow(rank = 4)
    }
}

/** 연패 0 — 뱃지가 통째로 사라진다. */
@ThemePreviews
@Composable
private fun LoserRankRowWithoutStreakPreview() {
    LoserRankRowPreviewBox {
        PreviewRow(rank = 3)
    }
}

/** 기록 없음(챌린지 0회)과 전승 0패. 나머지가 같아도 캡션만 갈린다. */
@ThemePreviews
@Composable
private fun LoserRankRowNoRecordPreview() {
    LoserRankRowPreviewBox {
        PreviewRow(rank = 5)
        PreviewRow(rank = 5) { copy(totalChallenges = 12) }
    }
}

/** 내 행이 top3 밖일 때 — 보더는 outline 인데 배경만 surfaceVariant 로 올라간다. */
@ThemePreviews
@Composable
private fun LoserRankRowMePreview() {
    LoserRankRowPreviewBox {
        PreviewRow(rank = 4) { copy(isMe = true) }
    }
}

@ThemePreviews
@Composable
private fun LoserRankRowLongNicknamePreview() {
    LoserRankRowPreviewBox {
        PreviewRow(rank = 3) { copy(nickname = PREVIEW_LONG_NICKNAME) }
    }
}
