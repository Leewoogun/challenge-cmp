package com.lwg.challenge.feature.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun BoxScope.BackgroundDecor() {
    val glowBrush = ChallengeTheme.brushes.glow
    val fireBrush = ChallengeTheme.brushes.fire

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .size(500.dp)
            .alpha(0.30f)
            .clip(CircleShape)
            .background(brush = glowBrush),
    )

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .size(400.dp)
            .alpha(0.20f)
            .clip(CircleShape)
            .background(brush = fireBrush),
    )
}

@Preview
@Composable
private fun BackgroundDecorPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChallengeTheme.colorScheme.background),
        ) {
            BackgroundDecor()
        }
    }
}
