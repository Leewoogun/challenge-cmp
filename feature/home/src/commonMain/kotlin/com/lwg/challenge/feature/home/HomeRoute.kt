package com.lwg.challenge.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.home.component.HomeTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    @Suppress("UNUSED_PARAMETER") viewModel: HomeViewModel = koinViewModel(),
) {
    HomeContent()
}

@Composable
private fun HomeContent() {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.primary,
        topBar = { HomeTopBar() },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "준비 중",
                style = ChallengeTheme.typography.medium16,
            )
        }
    }
}
