package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider as Material3HorizontalDivider
import androidx.compose.material3.VerticalDivider as Material3VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 디자인 시스템 표준 구분선.
 */
@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    lineColor: Color = ChallengeTheme.colorScheme.line.divider,
    thickness: Dp = 1.dp,
) {
    Material3HorizontalDivider(
        modifier = modifier,
        color = lineColor,
        thickness = thickness,
    )
}

@Composable
fun HorizontalDivider10(
    modifier: Modifier = Modifier,
    lineColor: Color = ChallengeTheme.colorScheme.line.dividerSub,
) {
    Material3HorizontalDivider(
        modifier = modifier,
        color = lineColor,
        thickness = 10.dp,
    )
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    lineColor: Color = ChallengeTheme.colorScheme.line.divider,
) {
    Material3VerticalDivider(
        modifier = modifier,
        color = lineColor,
        thickness = 1.dp,
    )
}

@Composable
fun VerticalDivider10(
    modifier: Modifier = Modifier,
    lineColor: Color = ChallengeTheme.colorScheme.line.dividerSub,
) {
    Material3VerticalDivider(
        modifier = modifier,
        color = lineColor,
        thickness = 10.dp,
    )
}

@ThemePreviews
@Composable
private fun DividerPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider10(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
