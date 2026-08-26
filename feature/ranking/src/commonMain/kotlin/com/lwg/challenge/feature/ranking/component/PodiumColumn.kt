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
import com.lwg.challenge.feature.ranking.contract.LoserRankingItemState
import org.jetbrains.compose.ui.tooling.preview.Preview

private val PedestalShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

/**
 * 포디움 1열. **1·2·3위 공용 1종이다.**
 *
 * 순위별 차이는 ① 왕관 노출 ② 아바타 크기·배경 ③ 받침대 크기·배경 ④ 캡션 문구 넷뿐이고 전부
 * 파라미터다. 1위용을 따로 만들면 이름·캡션의 타이포와 간격이 조용히 갈린다. (design.md §4.3)
 *
 * @param pedestalBrush null 이면 받침대를 `surfaceVariant` 평면색으로 그린다.
 * @param pedestalBrushAlpha [pedestalBrush] 에만 적용되는 알파. 받침대 전체(아이콘 포함)에 알파를
 *   걸면 배경이 다시 안 보이므로 아이콘 알파는 [pedestalIconTint] 로 따로 준다.
 */
@Composable
internal fun PodiumColumn(
    item: LoserRankingItemState,
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

        PodiumAvatar(
            nickname = item.displayName,
            size = avatarSize,
            textStyle = avatarTextStyle,
            brush = avatarBrush,
        )

        Text(
            text = item.displayName,
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
            text = item.podiumCaptionText,
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

private fun previewItem(
    rank: Int,
    nickname: String,
    losses: Int,
    streak: Int,
): LoserRankingItemState = LoserRankingItemState(
    LoserRankingEntry(
        rank = rank,
        userId = rank.toLong(),
        nickname = nickname,
        profileImageUrl = null,
        isMe = false,
        losses = losses,
        lossRate = 70 - rank,
        currentLossStreak = streak,
        totalChallenges = 30,
    ),
)

@Preview
@Composable
private fun PodiumColumnFirstPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            PodiumColumn(
                item = previewItem(rank = 1, nickname = "준혁", losses = 25, streak = 7),
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
private fun PodiumColumnRunnerUpsPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PodiumColumn(
                item = previewItem(rank = 2, nickname = "지우", losses = 18, streak = 4),
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
                item = previewItem(rank = 3, nickname = "도윤", losses = 14, streak = 2),
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
private fun PodiumColumnFirstWithoutStreakPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            PodiumColumn(
                item = previewItem(rank = 1, nickname = "준혁", losses = 25, streak = 0),
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
