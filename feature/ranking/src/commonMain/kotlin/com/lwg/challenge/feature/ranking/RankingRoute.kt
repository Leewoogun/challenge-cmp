package com.lwg.challenge.feature.ranking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RankingRoute(
    viewModel: RankingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RankingContent(
        uiState = uiState,
    )
}

@Composable
private fun RankingContent(
    uiState: RankingUiState,
) {
    when (uiState) {
        is RankingUiState.Loading,
        is RankingUiState.Data -> {
            RankingScreen()
        }
    }
}
