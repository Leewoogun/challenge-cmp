package com.lwg.challenge.feature.friends.list.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Friend
import kotlinx.datetime.LocalDateTime

/** 친구 목록 섹션 — 헤더 "친구 N명" + [FriendListItem] 리스트. */
@Composable
internal fun FriendsListSection(
    friends: List<Friend>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "친구 ${friends.size}명",
            style = ChallengeTheme.typography.bold14,
            color = ChallengeTheme.colorScheme.text.primary,
        )

        friends.forEach { friend ->
            FriendListItem(
                profileImageUrl = friend.profileImageUrl,
                nickname = friend.nickname,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FriendsListSectionPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            FriendsListSection(
                friends = listOf(
                    Friend(userId = 1, nickname = "우건", profileImageUrl = null, since = LocalDateTime.parse("2026-06-24T12:00:00")),
                    Friend(userId = 2, nickname = "도전자", profileImageUrl = null, since = LocalDateTime.parse("2026-06-17T12:00:00")),
                ),
            )
        }
    }
}
