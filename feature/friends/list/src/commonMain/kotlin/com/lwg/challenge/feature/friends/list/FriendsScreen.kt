@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.friends.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.feature.friends.list.component.FriendsActionRow
import com.lwg.challenge.feature.friends.list.component.FriendsEmptyState
import com.lwg.challenge.feature.friends.list.component.FriendsListSection
import com.lwg.challenge.feature.friends.list.component.ReceivedRequestsSection
import com.lwg.challenge.feature.friends.list.contract.FriendItemState
import com.lwg.challenge.feature.friends.list.contract.FriendRequestItemState
import com.lwg.challenge.feature.friends.list.contract.FriendsUiState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 탭 화면 — ActionRow + 받은 요청 섹션 + 친구 목록 섹션. design.md §A.2.
 */
@Composable
internal fun FriendsScreen(
    uiState: FriendsUiState.Data,
    onClickAddFriend: () -> Unit,
    onClickInvite: () -> Unit,
    onAcceptRequest: (FriendRequestItemState) -> Unit,
    onRejectRequest: (FriendRequestItemState) -> Unit,
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

        if (uiState.receivedRequests.isNotEmpty()) {
            item {
                ReceivedRequestsSection(
                    requests = uiState.receivedRequests,
                    onAccept = onAcceptRequest,
                    onReject = onRejectRequest,
                )
            }
        }

        if (uiState.friends.isNotEmpty()) {
            item {
                FriendsListSection(friends = uiState.friends)
            }
        }
    }
}

@Composable
internal fun FriendsLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
    }
}

/**
 * 친구 탭 빈 상태 화면 — [FriendsEmptyState] 단일 CTA 만 노출한다.
 */
@Composable
internal fun FriendsEmptyScreen(
    onClickAddFriend: () -> Unit,
    onClickInvite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        FriendsEmptyState(
            title = "아직 친구가 없어요",
            subtitle = "친구를 추가하고 함께 챌린지를 시작해보세요",
            ctaLabel = "친구 추가하기",
            icon = Icons.Filled.Group,
            onClickCta = onClickAddFriend,
            onClickInvite = onClickInvite,
        )
    }
}

@Preview
@Composable
private fun FriendsLoadingScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsLoadingScreen()
        }
    }
}

@Preview
@Composable
private fun FriendsEmptyScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsEmptyScreen(
                onClickAddFriend = {},
                onClickInvite = {},
            )
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

private fun previewFriends(): List<FriendItemState> {
    val now = Clock.System.now()
    return listOf(
        FriendItemState(Friend(userId = 1, nickname = "우건", profileImageUrl = null, since = now - 3.days), now),
        FriendItemState(Friend(userId = 2, nickname = "도전자", profileImageUrl = null, since = now - 10.days), now),
    )
}

private fun previewRequests(): List<FriendRequestItemState> {
    val now = Clock.System.now()
    return listOf(
        FriendRequestItemState(
            FriendRequest(
                requestId = 10,
                fromUser = UserBrief(userId = 3, nickname = "도전왕", profileImageUrl = null),
                requestedAt = now - 1.days,
            ),
            now,
        ),
    )
}
