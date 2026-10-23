package com.lwg.challenge.feature.friends.list.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.ProfileImage
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 친구 목록 항목 카드. 친구 1명을 가로 1줄로 표시. */
@Composable
internal fun FriendListItem(
    profileImageUrl: String?,
    nickname: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileImage(imageUrl = profileImageUrl, nickname = nickname)

            Text(
                text = nickname,
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun FriendListItemPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendListItem(
                profileImageUrl = null,
                nickname = "우건",
            )
        }
    }
}

@Preview
@Composable
private fun FriendListItemLongNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendListItem(
                profileImageUrl = null,
                nickname = "엄청나게긴닉네임을가진사용자가존재한다면어떻게보일까요테스트",
            )
        }
    }
}

@Preview
@Composable
private fun FriendListItemNoProfilePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendListItem(
                profileImageUrl = null,
                nickname = "도전자",
            )
        }
    }
}
