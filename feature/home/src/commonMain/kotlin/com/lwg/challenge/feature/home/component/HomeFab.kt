package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HomeFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        containerColor = ChallengeTheme.colorScheme.primary,
        contentColor = ChallengeTheme.colorScheme.onPrimary,
        modifier = Modifier.size(56.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "챌린지 만들기",
            modifier = Modifier.size(28.dp),
        )
    }
}

@Preview
@Composable
private fun HomeFabPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            HomeFab(onClick = {})
        }
    }
}
