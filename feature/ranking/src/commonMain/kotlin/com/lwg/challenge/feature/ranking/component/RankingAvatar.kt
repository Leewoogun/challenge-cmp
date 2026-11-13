package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.ProfileImage
import org.jetbrains.compose.ui.tooling.preview.Preview

private val AvatarShape = RoundedCornerShape(16.dp)

/** Top3 카드 아바타. [brush](1위 fire) 가 있으면 그 위에 투명 배경으로 [ProfileImage] 를 얹는다. */
@Composable
internal fun RankingAvatar(
    imageUrl: String?,
    nickname: String,
    size: Dp,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    brush: Brush? = null,
) {
    if (brush == null) {
        ProfileImage(
            imageUrl = imageUrl,
            nickname = nickname,
            modifier = modifier,
            size = size,
            shape = AvatarShape,
            textStyle = textStyle,
        )
        return
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(AvatarShape)
            .background(brush),
    ) {
        ProfileImage(
            imageUrl = imageUrl,
            nickname = nickname,
            size = size,
            shape = AvatarShape,
            backgroundColor = Color.Transparent,
            textStyle = textStyle,
            textColor = ChallengeTheme.colorScheme.text.onPoint,
        )
    }
}

@Preview
@Composable
private fun RankingAvatarPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RankingAvatar(
                imageUrl = null,
                nickname = "우건",
                size = 56.dp,
                textStyle = ChallengeTheme.typography.medium20,
            )
            RankingAvatar(
                imageUrl = null,
                nickname = "준혁",
                size = 64.dp,
                textStyle = ChallengeTheme.typography.medium24,
                brush = ChallengeTheme.brushes.fire,
            )
        }
    }
}

@Preview
@Composable
private fun RankingAvatarBlankNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            RankingAvatar(
                imageUrl = null,
                nickname = "   ",
                size = 64.dp,
                textStyle = ChallengeTheme.typography.medium24,
                brush = ChallengeTheme.brushes.fire,
            )
        }
    }
}
