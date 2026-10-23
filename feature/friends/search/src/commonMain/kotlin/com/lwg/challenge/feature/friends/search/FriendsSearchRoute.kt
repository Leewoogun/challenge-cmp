package com.lwg.challenge.feature.friends.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.feature.friends.search.component.FriendsSearchTopBar
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiEffect
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiState
import com.lwg.challenge.feature.friends.search.screen.FriendsSearchEmptyScreen
import com.lwg.challenge.feature.friends.search.screen.FriendsSearchIdleScreen
import com.lwg.challenge.feature.friends.search.screen.FriendsSearchLoadingScreen
import com.lwg.challenge.feature.friends.search.screen.FriendsSearchScreen
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FriendsSearchRoute(
    viewModel: FriendsSearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigateAction.current
    val mainAction = LocalMainAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is FriendsSearchUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    FriendsSearchContent(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::search,
        onBack = { navigator.popBackStack() },
        onClickAction = viewModel::clickAction,
        onClickCancel = viewModel::cancelRequest,
    )
}

@Composable
private fun FriendsSearchContent(
    query: String,
    uiState: FriendsSearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClickAction: (UserSearchResult) -> Unit,
    onClickCancel: (UserSearchResult) -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            FriendsSearchTopBar(
                query = query,
                onQueryChange = onQueryChange,
                onBack = onBack,
            )
        },
    ) {
        when (uiState) {
            FriendsSearchUiState.Idle -> FriendsSearchIdleScreen()

            FriendsSearchUiState.Searching -> FriendsSearchLoadingScreen()

            FriendsSearchUiState.Empty -> FriendsSearchEmptyScreen()

            is FriendsSearchUiState.Result -> FriendsSearchScreen(
                uiState = uiState,
                onClickAction = onClickAction,
                onClickCancel = onClickCancel,
            )
        }
    }
}
