package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/** step0 — 대결 상대 선택. design.md §1.2. */
@Composable
internal fun FriendPickStep(
    uiState: ChallengeCreateUiState.Data,
    onSelectFriend: (Long) -> Unit,
    onClickAddFriend: () -> Unit,
    onRetryLoadFriends: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
    ) {
        StepHeader(
            title = "누구에게 걸래? 🔥",
            subtitle = "대결 상대를 선택하세요",
        )

        VerticalSpacer(24.dp)

        when {
            uiState.loadFailed -> FriendPickPlaceholderCard(
                title = "친구 목록을 불러오지 못했어요",
                subtitle = "잠시 후 다시 시도해주세요",
                ctaLabel = "다시 시도",
                ctaIcon = Icons.Filled.Refresh,
                onClickCta = onRetryLoadFriends,
            )

            uiState.isFriendsEmpty -> FriendPickPlaceholderCard(
                title = "아직 친구가 없어요",
                subtitle = "친구를 추가하고 챌린지를 걸어보세요",
                ctaLabel = "친구 추가하기",
                ctaIcon = Icons.Filled.PersonAdd,
                onClickCta = onClickAddFriend,
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = uiState.friends,
                    key = { it.userId },
                ) { friend ->
                    FriendPickItem(
                        profileImageUrl = friend.profileImageUrl,
                        nickname = friend.nickname,
                        selected = friend.userId == uiState.selectedFriendId,
                        onClick = { onSelectFriend(friend.userId) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun FriendPickStepDataPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendPickStep(
                uiState = previewState(
                    friends = listOf(previewFriend(1, "우건"), previewFriend(2, "도전왕")),
                    selectedFriendId = 2L,
                ),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
            )
        }
    }
}

@Preview
@Composable
private fun FriendPickStepEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendPickStep(
                uiState = previewState(friends = emptyList()),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
            )
        }
    }
}

@Preview
@Composable
private fun FriendPickStepLoadFailedPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendPickStep(
                uiState = previewState(friends = emptyList(), loadFailed = true),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
            )
        }
    }
}

private fun previewFriend(id: Long, nickname: String) = Friend(
    userId = id,
    nickname = nickname,
    profileImageUrl = null,
    since = LocalDateTime.parse("2026-01-01T00:00:00"),
)

private fun previewState(
    friends: List<Friend>,
    selectedFriendId: Long? = null,
    loadFailed: Boolean = false,
) = ChallengeCreateUiState.Data(
    step = ChallengeCreateStep.FRIEND_PICK,
    friends = friends,
    selectedFriendId = selectedFriendId,
    myMission = "",
    betContent = "",
    deadlineType = DeadlineType.TODAY,
    todayDeadlineHint = "7/28 24:00",
    tomorrowDeadlineHint = "7/29 24:00",
    signature = Signature.Empty,
    isSubmitting = false,
    loadFailed = loadFailed,
)
