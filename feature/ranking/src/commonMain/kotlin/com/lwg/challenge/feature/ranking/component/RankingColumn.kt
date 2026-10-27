package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.modifier.wiggle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.feature.ranking.contract.cardCaption
import com.lwg.challenge.feature.ranking.contract.displayName
import org.jetbrains.compose.ui.tooling.preview.Preview

private val PedestalShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

/**
 * Top3 카드 1열. 1·2·3위 공용 1종이라 순위별 차이(왕관·아바타·받침대·캡션)는 전부 파라미터다.
 *
 * @param pedestalBrushAlpha [pedestalBrush] 에만 건다. 받침대 전체에 걸면 아이콘까지 흐려져 아이콘 알파는 [pedestalIconTint] 로 준다.
 */
@Composable
internal fun RankingColumn(
    entry: LoserRankingEntry,
    avatarSize: Dp,
    avatarTextStyle: TextStyle,
    nameStyle: TextStyle,
    pedestalWidth: Dp,
    pedestalHeight: Dp,
    pedestalIcon: ImageVector,
    pedestalIconSize: Dp,
    pedestalIconTint: Color,
    modifier: Modifier = Modifier,
    showCrown: Boolean = false,
    avatarBrush: Brush? = null,
    pedestalBrush: Brush? = null,
    pedestalBrushAlpha: Float = 1f,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showCrown) {
            Text(
                text = "🐷",
                style = ChallengeTheme.typography.bold24,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .wiggle(),
            )
        }

        RankingAvatar(
            imageUrl = entry.profileImageUrl,
            nickname = entry.displayName,
            size = avatarSize,
            textStyle = avatarTextStyle,
            brush = avatarBrush,
        )

        Text(
            text = entry.displayName,
            style = nameStyle,
            color = ChallengeTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .width(pedestalWidth),
        )

        Text(
            text = entry.cardCaption,
            style = ChallengeTheme.typography.medium10,
            color = ChallengeTheme.colorScheme.error,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 2.dp)
                .width(pedestalWidth),
        )

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(pedestalWidth)
                .height(pedestalHeight)
                .then(
                    if (pedestalBrush != null) {
                        Modifier.background(
                            brush = pedestalBrush,
                            shape = PedestalShape,
                            alpha = pedestalBrushAlpha,
                        )
                    } else {
                        Modifier.background(
                            color = ChallengeTheme.colorScheme.surfaceVariant,
                            shape = PedestalShape,
                        )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = pedestalIcon,
                contentDescription = null,
                modifier = Modifier.size(pedestalIconSize),
                tint = pedestalIconTint,
            )
        }
    }
}

private fun previewEntry(
    rank: Int,
    nickname: String,
    losses: Int,
    streak: Int,
) = LoserRankingEntry(
    rank = rank,
    userId = rank.toLong(),
    nickname = nickname,
    profileImageUrl = null,
    isMe = false,
    losses = losses,
    lossRate = 70 - rank,
    currentLossStreak = streak,
    totalChallenges = 30,
)

@Preview
@Composable
private fun RankingColumnFirstPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            RankingColumn(
                entry = previewEntry(rank = 1, nickname = "준혁", losses = 25, streak = 7),
                avatarSize = 64.dp,
                avatarTextStyle = ChallengeTheme.typography.medium24,
                nameStyle = ChallengeTheme.typography.bold14,
                pedestalWidth = 80.dp,
                pedestalHeight = 96.dp,
                pedestalIcon = Icons.Filled.LocalFireDepartment,
                pedestalIconSize = 24.dp,
                pedestalIconTint = ChallengeTheme.colorScheme.primary.copy(alpha = 0.40f),
                showCrown = true,
                avatarBrush = ChallengeTheme.brushes.fire,
                pedestalBrush = ChallengeTheme.brushes.fire,
                pedestalBrushAlpha = 0.20f,
            )
        }
    }
}

@Preview
@Composable
private fun RankingColumnRunnerUpsPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RankingColumn(
                entry = previewEntry(rank = 2, nickname = "지우", losses = 18, streak = 4),
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
                entry = previewEntry(rank = 3, nickname = "도윤", losses = 14, streak = 2),
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

/** 연패 0 인 1위 — 캡션에서 🐷 절이 통째로 사라진다. */
@Preview
@Composable
private fun RankingColumnFirstWithoutStreakPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            RankingColumn(
                entry = previewEntry(rank = 1, nickname = "준혁", losses = 25, streak = 0),
                avatarSize = 64.dp,
                avatarTextStyle = ChallengeTheme.typography.medium24,
                nameStyle = ChallengeTheme.typography.bold14,
                pedestalWidth = 80.dp,
                pedestalHeight = 96.dp,
                pedestalIcon = Icons.Filled.LocalFireDepartment,
                pedestalIconSize = 24.dp,
                pedestalIconTint = ChallengeTheme.colorScheme.primary.copy(alpha = 0.40f),
                showCrown = true,
                avatarBrush = ChallengeTheme.brushes.fire,
                pedestalBrush = ChallengeTheme.brushes.fire,
                pedestalBrushAlpha = 0.20f,
            )
        }
    }
}
