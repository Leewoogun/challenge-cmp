package com.lwg.challenge.feature.friends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.friend.FriendsEmptyState
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.component.FriendsTopBar
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun FriendsScreen(
    uiState: FriendsUiState,
    onClickAddFriend: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = { FriendsTopBar() },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                FriendsUiState.Loading -> LoadingContent()
                FriendsUiState.Data -> FriendsEmptyState(
                    title = "아직 친구가 없어요",
                    subtitle = "친구를 추가하고 함께 챌린지를 시작해보세요",
                    ctaLabel = "친구 추가하기",
                    icon = Icons.Filled.Group,
                    onClickCta = onClickAddFriend,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
    }
}

@Preview
@Composable
private fun FriendsScreenDataPreview() {
    ChallengeTheme {
        FriendsScreen(
            uiState = FriendsUiState.Data,
            onClickAddFriend = {},
        )
    }
}

@Preview
@Composable
private fun FriendsScreenLoadingPreview() {
    ChallengeTheme {
        FriendsScreen(
            uiState = FriendsUiState.Loading,
            onClickAddFriend = {},
        )
    }
}
