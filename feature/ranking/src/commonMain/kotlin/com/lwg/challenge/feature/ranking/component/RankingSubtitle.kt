package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/** 승자가 아니라 패자 랭킹임을 진입하자마자 알리는 부제. */
@Composable
internal fun RankingSubtitle(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "패배의 왕좌 — 많이 진 놈이 대장",
        style = ChallengeTheme.typography.medium12,
        color = ChallengeTheme.colorScheme.text.secondary,
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
private fun RankingSubtitlePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            RankingSubtitle()
        }
    }
}
