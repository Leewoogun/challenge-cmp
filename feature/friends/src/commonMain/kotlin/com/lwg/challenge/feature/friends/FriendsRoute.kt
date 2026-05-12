package com.lwg.challenge.feature.friends

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FriendsRoute(
    viewModel: FriendsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FriendsContent(
        uiState = uiState,
    )
}

@Composable
private fun FriendsContent(
    uiState: FriendsUiState,
) {
    when (uiState) {
        is FriendsUiState.Loading,
        is FriendsUiState.Data -> {
            FriendsScreen()
        }
    }
}
