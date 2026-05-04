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
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Label with solid color background
 */
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

/**
 * Label with gradient brush background
 */
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
@Preview
private fun ChallengeLabelPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Solid color label — primary
            ChallengeLabel(
                backgroundColor = ChallengeTheme.colorScheme.primary
            ) {
                Text(
                    text = "Primary Label",
                    color = ChallengeTheme.colorScheme.onPrimary
                )
            }

            // Gradient label — fire
            ChallengeLabel(
                backgroundBrush = ChallengeTheme.brushes.fire
            ) {
                Text(
                    text = "Gradient Label",
                    color = ChallengeTheme.colorScheme.onPrimary
                )
            }

            // Surface variant example
            ChallengeLabel(
                backgroundColor = ChallengeTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Secondary Label",
                    color = ChallengeTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
