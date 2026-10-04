package com.lwg.challenge.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.home.component.HomeFab
import com.lwg.challenge.feature.home.component.HomeLoadingScreen
import com.lwg.challenge.feature.home.component.HomeTopBar
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
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
        onAcceptClick = { challenge ->
            navigateAction.navigateTo(Route.Challenge.Oath(challenge.challengeId))
        },
        onRejectClick = viewModel::rejectChallenge,
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
    onAcceptClick: (ReceivedChallenge) -> Unit,
    onRejectClick: (ReceivedChallenge) -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            HomeTopBar(
                hasUnreadNotification = uiState.hasUnreadNotification,
                onBellClick = onBellClick,
                debugAccountLabel = uiState.debugAccountLabel,
            )
        },
        floatingActionButton = {
            HomeFab(onClick = onFabClick)
        },
    ) {
        when (uiState) {
            HomeUiState.Loading -> HomeLoadingScreen()
            is HomeUiState.Data -> HomeScreen(
                uiState = uiState,
                onChallengeClick = onChallengeClick,
                onAddFriendClick = onAddFriendClick,
                onCreateChallengeClick = onCreateChallengeClick,
                onAcceptClick = onAcceptClick,
                onRejectClick = onRejectClick,
            )
        }
    }
}
