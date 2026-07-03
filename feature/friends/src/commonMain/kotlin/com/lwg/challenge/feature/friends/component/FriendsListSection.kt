@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.friends.component

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
import com.lwg.challenge.designsystem.components.friend.FriendListItem
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.feature.friends.contract.FriendItemState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 목록 섹션. design.md §A.2 — 헤더 "친구 N명" + [FriendListItem] 리스트.
 */
@Composable
internal fun FriendsListSection(
    friends: List<FriendItemState>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "친구 ${friends.size}명",
            style = ChallengeTheme.typography.bold14,
            color = ChallengeTheme.colorScheme.onBackground,
        )

        friends.forEach { friend ->
            FriendListItem(
                profileImageUrl = friend.profileImageUrl,
                nickname = friend.nickname,
            )
        }
    }
}

@Preview
@Composable
private fun FriendsListSectionPreview() {
    val now = Clock.System.now()
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            FriendsListSection(
                friends = listOf(
                    FriendItemState(Friend(userId = 1, nickname = "우건", profileImageUrl = null, since = now - 3.days), now),
                    FriendItemState(Friend(userId = 2, nickname = "도전자", profileImageUrl = null, since = now - 10.days), now),
                ),
            )
        }
    }
}
