package com.lwg.challenge.feature.ex3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.ex3.contract.Ex3UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Ex3Route(
    viewModel: Ex3ViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Ex3Content(
        uiState = uiState,
    )
}

@Composable
private fun Ex3Content(
    uiState: Ex3UiState,
) {
    when (uiState) {
        is Ex3UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        is Ex3UiState.Data -> {
            Ex3Screen(uiState = uiState)
        }
    }
}
