package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 명단 섹션 제목. */
@Composable
internal fun RankingSectionTitle(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "수치의 명단",
        style = ChallengeTheme.typography.bold14,
        color = ChallengeTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun RankingSectionTitlePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            RankingSectionTitle()
        }
    }
}
