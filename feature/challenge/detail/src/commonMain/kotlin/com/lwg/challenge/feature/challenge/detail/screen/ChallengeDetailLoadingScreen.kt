package com.lwg.challenge.feature.challenge.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/** 계약서 응답 대기 화면. */
@Composable
internal fun ChallengeDetailLoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.point)
    }
}

@ThemePreviews
@Composable
private fun ChallengeDetailLoadingScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            ChallengeDetailLoadingScreen()
        }
    }
}
