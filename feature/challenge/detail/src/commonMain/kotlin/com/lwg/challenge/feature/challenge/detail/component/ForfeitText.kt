package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_ENTRY_TEXT

@Composable
internal fun ForfeitText(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = FORFEIT_ENTRY_TEXT,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.error,
        )
    }
}

@ThemePreviews
@Composable
private fun ForfeitTextPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            ForfeitText(onClick = {})
        }
    }
}

@ThemePreviews
@Composable
private fun ForfeitTextDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            ForfeitText(onClick = {}, enabled = false)
        }
    }
}
