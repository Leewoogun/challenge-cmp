package com.lwg.challenge.feature.ex2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lwg.challenge.feature.ex2.contract.Ex2UiState

@Composable
internal fun Ex2Screen(
    uiState: Ex2UiState.Data,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // TODO: UI를 구현하세요.
        Text(
            text = "Ex2",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
