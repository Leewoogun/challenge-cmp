package com.lwg.challenge.feature.ranking

import androidx.compose.runtime.Composable
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.PlaceholderScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun RankingScreen() {
    PlaceholderScreen(title = "랭킹")
}

@Preview
@Composable
private fun RankingScreenPreview() {
    ChallengeTheme {
        RankingScreen()
    }
}
