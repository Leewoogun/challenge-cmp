package com.lwg.challenge.feature.ex2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.ex2.contract.Ex2UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Ex2Route(
    viewModel: Ex2ViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Ex2Content(
        uiState = uiState,
    )
}

@Composable
private fun Ex2Content(
    uiState: Ex2UiState,
) {
    when (uiState) {
        is Ex2UiState.Loading -> {
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

        is Ex2UiState.Data -> {
            Ex2Screen(uiState = uiState)
        }
    }
}
