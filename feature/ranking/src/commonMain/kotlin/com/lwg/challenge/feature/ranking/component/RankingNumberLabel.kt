package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 명단 순위 숫자. 1·3위만 강조하고 2위는 4위 이하와 같다 — 보더(top3)와 축이 다른 건 디자인 그대로다. */
@Composable
internal fun RankingNumberLabel(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = rank.toString(),
        style = ChallengeTheme.typography.bold14,
        color = when (rank) {
            1 -> ChallengeTheme.colorScheme.error
            3 -> ChallengeTheme.colorScheme.warning
            else -> ChallengeTheme.colorScheme.onSurfaceVariant
        },
        textAlign = TextAlign.Center,
        modifier = modifier.width(28.dp),
    )
}

@Preview
@Composable
private fun RankingNumberLabelPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            (1..5).forEach { rank -> RankingNumberLabel(rank = rank) }
        }
    }
}
