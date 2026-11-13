package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

@Composable
fun ChallengeLabel(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun ChallengeLabel(
    backgroundBrush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
@ThemePreviews
private fun ChallengeLabelPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChallengeLabel(
                backgroundColor = ChallengeTheme.colorScheme.point
            ) {
                Text(
                    text = "Primary Label",
                    color = ChallengeTheme.colorScheme.text.onPoint
                )
            }

            ChallengeLabel(
                backgroundBrush = ChallengeTheme.brushes.fire
            ) {
                Text(
                    text = "Gradient Label",
                    color = ChallengeTheme.colorScheme.text.onPoint
                )
            }

            ChallengeLabel(
                backgroundColor = ChallengeTheme.colorScheme.background.cardSub
            ) {
                Text(
                    text = "Secondary Label",
                    color = ChallengeTheme.colorScheme.text.secondary
                )
            }
        }
    }
}
