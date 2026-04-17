package com.lwg.challenge.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.home.contract.HomeUiState

@Composable
internal fun HomeScreen(
    uiState: HomeUiState.Data,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(uiState.movies) { movie ->
            Text(
                text = movie.title,
                style = ChallengeTheme.typography.medium16,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
            HorizontalDivider()
        }
    }
}
