package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 프로필 이미지 자리. 이미지 로더 부재로 닉네임 첫 글자 이니셜만 렌더한다. */
@Composable
fun ProfilePlaceholder(
    nickname: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = ChallengeTheme.colorScheme.secondary,
    textStyle: TextStyle = ChallengeTheme.typography.medium20,
    textColor: Color = ChallengeTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nickname.trim().firstOrNull()?.uppercase() ?: "",
            style = textStyle,
            color = textColor,
        )
    }
}

@Preview
@Composable
private fun ProfilePlaceholderPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ProfilePlaceholder(nickname = "우건")
        }
    }
}

@Preview
@Composable
private fun ProfilePlaceholderVariantsPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfilePlaceholder(nickname = "도윤")
            ProfilePlaceholder(nickname = "도전자", size = 44.dp)
            ProfilePlaceholder(nickname = "절친", size = 32.dp, shape = CircleShape)
        }
    }
}

@Preview
@Composable
private fun ProfilePlaceholderBlankNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ProfilePlaceholder(nickname = "   ")
        }
    }
}
