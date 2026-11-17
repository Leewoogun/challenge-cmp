package com.lwg.challenge.feature.friends.record

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiEffect
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import com.lwg.challenge.feature.friends.record.screen.FriendRecordEmptyScreen
import com.lwg.challenge.feature.friends.record.screen.FriendRecordErrorScreen
import com.lwg.challenge.feature.friends.record.screen.FriendRecordLoadingScreen
import com.lwg.challenge.feature.friends.record.screen.FriendRecordScreen
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.ui.components.PaginationEffect
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FriendRecordRoute(
    friendId: Long,
    viewModel: FriendRecordViewModel = koinViewModel { parametersOf(friendId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is FriendRecordUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    FriendRecordContent(
        uiState = uiState,
        listState = listState,
        onBack = navigator::popBackStack,
        onChallengeClick = navigator::navigateTo,
        onSendChallengeClick = { navigator.navigateTo(Route.Challenge.Create(opponentId = friendId)) },
        onRetryClick = viewModel::retry,
    )

    PaginationEffect(
        listState = listState,
        onLoadNextPage = viewModel::nextPage,
    )
}

@Composable
private fun FriendRecordContent(
    uiState: FriendRecordUiState,
    listState: LazyListState,
    onBack: () -> Unit,
    onChallengeClick: (Route) -> Unit,
    onSendChallengeClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.background.card,
        topBar = { ChallengeTopBar(title = "친구 전적", onBack = onBack) },
    ) {
        when (uiState) {
            FriendRecordUiState.Loading -> FriendRecordLoadingScreen()

            is FriendRecordUiState.Error -> FriendRecordErrorScreen(
                uiState = uiState,
                onRetryClick = onRetryClick,
            )

            is FriendRecordUiState.Empty -> FriendRecordEmptyScreen(
                uiState = uiState,
                onSendChallengeClick = onSendChallengeClick,
            )

            is FriendRecordUiState.Data -> FriendRecordScreen(
                uiState = uiState,
                listState = listState,
                onChallengeClick = onChallengeClick,
                onSendChallengeClick = onSendChallengeClick,
            )
        }
    }
}
