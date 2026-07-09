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

    HomeContent(
        uiState = uiState,
        onBellClick = { navigateAction.navigateTo(Route.Notifications) },
        onFabClick = { navigateAction.navigateTo(Route.Challenge.Create) },
        onChallengeClick = { challengeId ->
            navigateAction.navigateTo(Route.Challenge.Detail(challengeId))
        },
        onAddFriendClick = { navigateAction.switchTab(Route.Friends.List) },
        onCreateChallengeClick = { navigateAction.navigateTo(Route.Challenge.Create) },
    )

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }
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
