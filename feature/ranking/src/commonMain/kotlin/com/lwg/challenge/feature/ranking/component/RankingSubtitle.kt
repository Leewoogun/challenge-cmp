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

/**
 * "이 탭은 승자 랭킹이 아니라 패자 랭킹" 임을 진입 즉시 못박는 부제.
 *
 * 상단바가 아니라 본문 최상단에 있는 이유는 [RankingTopBar] KDoc 참조.
 */
@Composable
internal fun RankingSubtitle(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "패배의 왕좌 — 많이 진 놈이 대장",
        style = ChallengeTheme.typography.medium12,
        color = ChallengeTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun RankingSubtitlePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            RankingSubtitle()
        }
    }
}
