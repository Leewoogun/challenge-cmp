package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.ProfilePlaceholder
import org.jetbrains.compose.ui.tooling.preview.Preview

private val AvatarShape = RoundedCornerShape(16.dp)

/**
 * 포디움 아바타.
 *
 * [brush] 가 null 이면 공용 [ProfilePlaceholder] 를 그대로 쓴다. Brush 배경(1위 fire)일 때만
 * 직접 그리는데, [ProfilePlaceholder] 의 배경 파라미터가 `Color` 라 Brush 를 넘길 수 없기 때문이다.
 * 호출부가 4개 feature 라 공용 컴포넌트에 `Brush?` 를 추가하지 않고 여기서 분기한다. (design.md §4.2)
 */
@Composable
internal fun PodiumAvatar(
    nickname: String,
    size: Dp,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    brush: Brush? = null,
) {
    if (brush == null) {
        ProfilePlaceholder(
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
        contentAlignment = Alignment.Center,
    ) {
        Text(
            // 이니셜 규칙은 ProfilePlaceholder 와 동일해야 한다 — 갈리면 1위만 다르게 보인다.
            text = nickname.trim().firstOrNull()?.uppercase() ?: "",
            style = textStyle,
            color = ChallengeTheme.colorScheme.onPrimary,
        )
    }
}

@Preview
@Composable
private fun PodiumAvatarPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PodiumAvatar(
                nickname = "우건",
                size = 56.dp,
                textStyle = ChallengeTheme.typography.medium20,
            )
            PodiumAvatar(
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
private fun PodiumAvatarBlankNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            PodiumAvatar(
                nickname = "   ",
                size = 64.dp,
                textStyle = ChallengeTheme.typography.medium24,
                brush = ChallengeTheme.brushes.fire,
            )
        }
    }
}
