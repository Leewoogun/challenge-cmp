package com.lwg.challenge.feature.friends

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import com.lwg.challenge.navigation.LocalMainAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FriendsRoute(
    viewModel: FriendsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is FriendsUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    FriendsContent(
        uiState = uiState,
        onClickAddFriend = { viewModel.showMessage("준비 중입니다") },
    )
}

@Composable
private fun FriendsContent(
    uiState: FriendsUiState,
    onClickAddFriend: () -> Unit,
) {
    FriendsScreen(
        uiState = uiState,
        onClickAddFriend = onClickAddFriend,
    )
}
