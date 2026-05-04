package com.lwg.challenge.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.home.component.HomeTopBar
import com.lwg.challenge.feature.home.contract.HomeUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.primary,
        topBar = {
            HomeTopBar()
        },
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "영화 목록이 비어있습니다.",
                        style = ChallengeTheme.typography.medium16,
                    )
                }
            }

            is HomeUiState.Data -> {
                HomeScreen(uiState = uiState)
            }
        }
    }
}
