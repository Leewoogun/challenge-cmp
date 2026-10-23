package com.lwg.challenge.feature.friends.list.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.feature.friends.list.component.FriendsActionRow
import com.lwg.challenge.feature.friends.list.component.FriendsListSection
import com.lwg.challenge.feature.friends.list.component.ReceivedRequestsSection
import com.lwg.challenge.feature.friends.list.contract.FriendsUiState
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 친구 탭 화면 — ActionRow + 받은 요청 섹션 + 친구 목록 섹션. */
@Composable
internal fun FriendsScreen(
    uiState: FriendsUiState.Data,
    onClickAddFriend: () -> Unit,
    onClickInvite: () -> Unit,
    onAcceptRequest: (FriendRequest) -> Unit,
    onRejectRequest: (FriendRequest) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            FriendsActionRow(
                onClickAddFriend = onClickAddFriend,
                onClickInvite = onClickInvite,
            )
        }

        if (uiState.hasReceivedRequests) {
            item {
                ReceivedRequestsSection(
                    requests = uiState.receivedRequests,
                    onAccept = onAcceptRequest,
                    onReject = onRejectRequest,
                )
            }
        }

        if (uiState.hasFriends) {
            item {
                FriendsListSection(friends = uiState.friends)
            }
        }
    }
}

@Preview
@Composable
private fun FriendsScreenFriendsOnlyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsScreen(
                uiState = FriendsUiState.Data(
                    receivedRequests = emptyList(),
                    friends = previewFriends(),
                ),
                onClickAddFriend = {},
                onClickInvite = {},
                onAcceptRequest = {},
                onRejectRequest = {},
            )
        }
    }
}

@Preview
@Composable
private fun FriendsScreenRequestsAndFriendsPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsScreen(
                uiState = FriendsUiState.Data(
                    receivedRequests = previewRequests(),
                    friends = previewFriends(),
                ),
                onClickAddFriend = {},
                onClickInvite = {},
                onAcceptRequest = {},
                onRejectRequest = {},
            )
        }
    }
}

private fun previewFriends(): List<Friend> = listOf(
    Friend(userId = 1, nickname = "우건", profileImageUrl = null, since = LocalDateTime.parse("2026-06-24T12:00:00")),
    Friend(userId = 2, nickname = "도전자", profileImageUrl = null, since = LocalDateTime.parse("2026-06-17T12:00:00")),
)

private fun previewRequests(): List<FriendRequest> = listOf(
    FriendRequest(
        requestId = 10,
        fromUser = UserBrief(userId = 3, nickname = "도전왕", profileImageUrl = null),
        requestedAt = LocalDateTime.parse("2026-06-26T12:00:00"),
    ),
)
