package com.lwg.challenge.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateAction = LocalNavigateAction.current
    val mainAction = LocalMainAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onBellClick = { navigateAction.navigateTo(Route.NotificationsRoute.Main) },
        onFabClick = { navigateAction.navigateTo(Route.ChallengeCreateRoute.Main) },
        onChallengeClick = { challengeId ->
            navigateAction.navigateTo(Route.ChallengeDetailRoute.Main(challengeId))
        },
        onAddFriendClick = { navigateAction.switchTab(Route.FriendsRoute.Main) },
        onCreateChallengeClick = { navigateAction.navigateTo(Route.ChallengeCreateRoute.Main) },
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onBellClick: () -> Unit,
    onFabClick: () -> Unit,
    onChallengeClick: (Long) -> Unit,
    onAddFriendClick: () -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
    when (uiState) {
        HomeUiState.Loading -> HomeLoadingScreen(
            onBellClick = onBellClick,
            onFabClick = onFabClick,
        )
        is HomeUiState.Data -> HomeScreen(
            uiState = uiState,
            onBellClick = onBellClick,
            onFabClick = onFabClick,
            onChallengeClick = onChallengeClick,
            onAddFriendClick = onAddFriendClick,
            onCreateChallengeClick = onCreateChallengeClick,
        )
    }
}
