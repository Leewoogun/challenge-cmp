package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme

@Composable
internal fun HomeTopBar(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Cooking",
        style = ChallengeTheme.typography.bold20,
        color = ChallengeTheme.colorScheme.onPrimary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
