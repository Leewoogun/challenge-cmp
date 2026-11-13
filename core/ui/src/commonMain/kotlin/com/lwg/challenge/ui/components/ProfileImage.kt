package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/** 프로필 이미지. URL 이 없거나 로딩 중·실패면 닉네임 이니셜을 보여준다. */
@Composable
fun ProfileImage(
    imageUrl: String?,
    nickname: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = ChallengeTheme.colorScheme.background.field,
    textStyle: TextStyle = ChallengeTheme.typography.medium20,
    textColor: Color = ChallengeTheme.colorScheme.text.primary,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl == null) {
            NicknameInitial(nickname = nickname, textStyle = textStyle, textColor = textColor)
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { NicknameInitial(nickname = nickname, textStyle = textStyle, textColor = textColor) },
                error = { NicknameInitial(nickname = nickname, textStyle = textStyle, textColor = textColor) },
            )
        }
    }
}

/** SubcomposeAsyncImage 슬롯은 최소 크기를 강제(propagateMinConstraints)하므로 Box 로 감싸 가운데 둔다. */
@Composable
private fun NicknameInitial(nickname: String, textStyle: TextStyle, textColor: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = nickname.trim().firstOrNull()?.uppercase() ?: "",
            style = textStyle,
            color = textColor,
        )
    }
}

@ThemePreviews
@Composable
private fun ProfileImagePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ProfileImage(imageUrl = null, nickname = "우건")
        }
    }
}

@ThemePreviews
@Composable
private fun ProfileImageVariantsPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileImage(imageUrl = null, nickname = "도윤")
            ProfileImage(imageUrl = null, nickname = "도전자", size = 44.dp)
            ProfileImage(imageUrl = null, nickname = "절친", size = 32.dp, shape = CircleShape)
        }
    }
}

@ThemePreviews
@Composable
private fun ProfileImageBlankNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ProfileImage(imageUrl = null, nickname = "   ")
        }
    }
}

/** URL 이 있으면 로딩·실패 슬롯도 이니셜이다. 프리뷰는 네트워크를 못 타 로딩 슬롯까지만 보인다. */
@ThemePreviews
@Composable
private fun ProfileImageWithUrlPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ProfileImage(imageUrl = "https://k.kakaocdn.net/dn/sample/img_640x640.jpg", nickname = "우건")
        }
    }
}
