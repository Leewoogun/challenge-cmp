package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 후속 feature 개발 전 임시 화면용 공통 placeholder. */
@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "준비 중",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChallengeTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = ChallengeTheme.typography.bold18,
            color = ChallengeTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun PlaceholderScreenPreview() {
    ChallengeTheme {
        PlaceholderScreen(title = "랭킹")
    }
}
