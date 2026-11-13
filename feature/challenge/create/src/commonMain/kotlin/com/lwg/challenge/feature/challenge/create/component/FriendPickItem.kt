package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.ProfileImage

/**
 * @param selected 선택 여부. 선택 시 2.dp primary border 로 강조한다.
 */
@Composable
internal fun FriendPickItem(
    profileImageUrl: String?,
    nickname: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = if (selected) {
            BorderStroke(width = 2.dp, color = ChallengeTheme.colorScheme.point)
        } else {
            BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border)
        },
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
                color = ChallengeTheme.colorScheme.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChallengeTheme.colorScheme.icon.secondary,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FriendPickItemPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            FriendPickItem(
                profileImageUrl = null,
                nickname = "우건",
                selected = false,
                onClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FriendPickItemSelectedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            FriendPickItem(
                profileImageUrl = null,
                nickname = "우건",
                selected = true,
                onClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FriendPickItemLongNicknamePreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            FriendPickItem(
                profileImageUrl = null,
                nickname = "엄청나게긴닉네임을가진사용자가존재한다면어떻게보일까요테스트",
                selected = false,
                onClick = {},
            )
        }
    }
}
