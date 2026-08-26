package com.lwg.challenge.feature.ranking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.ranking.component.RankingTopBar
import com.lwg.challenge.feature.ranking.contract.RankingUiEffect
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RankingRoute(
    viewModel: RankingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is RankingUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    RankingContent(
        uiState = uiState,
        onClickAddFriend = { navigator.navigateTo(Route.Friends.Search) },
    )
}

@Composable
private fun RankingContent(
    uiState: RankingUiState,
    onClickAddFriend: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = { RankingTopBar() },
    ) {
        when (uiState) {
            RankingUiState.Loading -> RankingLoadingScreen()

            is RankingUiState.Data -> RankingScreen(
                uiState = uiState,
                onClickAddFriend = onClickAddFriend,
            )
        }
    }
}
