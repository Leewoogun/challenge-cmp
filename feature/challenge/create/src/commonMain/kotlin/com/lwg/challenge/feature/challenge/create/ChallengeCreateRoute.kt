package com.lwg.challenge.feature.challenge.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChallengeCreateRoute(
    viewModel: ChallengeCreateViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChallengeCreateContent(
        uiState = uiState,
    )
}

@Composable
private fun ChallengeCreateContent(
    uiState: ChallengeCreateUiState,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
    ) {
        when (uiState) {
            ChallengeCreateUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
            }

            ChallengeCreateUiState.Data -> ChallengeCreateScreen()
        }
    }
}
