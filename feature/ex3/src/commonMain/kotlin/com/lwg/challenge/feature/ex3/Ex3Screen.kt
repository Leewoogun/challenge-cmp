package com.lwg.challenge.feature.ex3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lwg.challenge.feature.ex3.contract.Ex3UiState

@Composable
internal fun Ex3Screen(
    uiState: Ex3UiState.Data,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // TODO: UI를 구현하세요.
        Text(
            text = "Ex3",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
