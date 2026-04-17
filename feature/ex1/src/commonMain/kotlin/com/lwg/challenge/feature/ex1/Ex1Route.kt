package com.lwg.challenge.feature.ex1

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.ex1.contract.Ex1UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Ex1Route(
    viewModel: Ex1ViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Ex1Content(
        uiState = uiState,
    )
}

@Composable
private fun Ex1Content(
    uiState: Ex1UiState,
) {
    when (uiState) {
        is Ex1UiState.Loading -> {
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

        is Ex1UiState.Data -> {
            Ex1Screen(uiState = uiState)
        }
    }
}
