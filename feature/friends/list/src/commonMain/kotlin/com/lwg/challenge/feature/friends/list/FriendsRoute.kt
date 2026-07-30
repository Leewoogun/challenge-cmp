package com.lwg.challenge.feature.friends.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.list.contract.FriendRequestItemState
import com.lwg.challenge.feature.friends.list.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.list.contract.FriendsUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FriendsRoute(
    viewModel: FriendsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is FriendsUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    FriendsContent(
        uiState = uiState,
        onClickAddFriend = { navigator.navigateTo(Route.Friends.Search) },
        onClickInvite = viewModel::inviteFriend,
        onAcceptRequest = viewModel::acceptRequest,
        onRejectRequest = viewModel::rejectRequest,
    )
}

@Composable
private fun FriendsContent(
    uiState: FriendsUiState,
    onClickAddFriend: () -> Unit,
    onClickInvite: () -> Unit,
    onAcceptRequest: (FriendRequestItemState) -> Unit,
    onRejectRequest: (FriendRequestItemState) -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = { ChallengeTopBar(title = "친구") },
    ) {
        when (uiState) {
            FriendsUiState.Loading -> FriendsLoadingScreen()

            FriendsUiState.Empty -> FriendsEmptyScreen(
                onClickAddFriend = onClickAddFriend,
                onClickInvite = onClickInvite,
            )

            is FriendsUiState.Data -> FriendsScreen(
                uiState = uiState,
                onClickAddFriend = onClickAddFriend,
                onClickInvite = onClickInvite,
                onAcceptRequest = onAcceptRequest,
                onRejectRequest = onRejectRequest,
            )
        }
    }
}
