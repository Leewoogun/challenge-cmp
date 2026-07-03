package com.lwg.challenge.feature.friends

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.feature.friends.component.FriendsSearchTopBar
import com.lwg.challenge.feature.friends.contract.FriendSearchItemState
import com.lwg.challenge.feature.friends.contract.FriendsSearchUiState
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
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
                is FriendsUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    FriendsSearchContent(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::search,
        onBack = { navigator.popBackStack() },
        onClickAction = { item ->
            when (item.relation) {
                Relation.NONE, Relation.REJECTED -> viewModel.sendRequest(item)
                Relation.REQUEST_RECEIVED -> viewModel.acceptRequest(item)
                else -> {}
            }
        },
        onClickCancel = viewModel::cancelRequest,
    )
}

@Composable
private fun FriendsSearchContent(
    query: String,
    uiState: FriendsSearchUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClickAction: (FriendSearchItemState) -> Unit,
    onClickCancel: (FriendSearchItemState) -> Unit,
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
            FriendsSearchUiState.Idle ->
                FriendsSearchHintScreen("닉네임을 2자 이상 입력해주세요")

            FriendsSearchUiState.Searching -> FriendsSearchLoadingScreen()

            FriendsSearchUiState.Empty ->
                FriendsSearchHintScreen("검색 결과가 없어요. 닉네임을 더 정확히 입력해보세요.")

            is FriendsSearchUiState.Result -> FriendsSearchScreen(
                uiState = uiState,
                onClickAction = onClickAction,
                onClickCancel = onClickCancel,
            )
        }
    }
}
