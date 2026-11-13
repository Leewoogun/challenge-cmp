package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import com.lwg.challenge.designsystem.components.ChallengeBaseTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 랭킹 탭 상단바. 부제를 같이 두면 폰트 1.3배에서 56dp 고정 높이가 잘려 부제는 본문([RankingSubtitle])에 둔다. */
@Composable
internal fun RankingTopBar(
    modifier: Modifier = Modifier,
) {
    ChallengeBaseTopBar(modifier = modifier) {
        Text(
            text = "개돼지 랭킹 🐷",
            style = ChallengeTheme.typography.bold20,
            color = ChallengeTheme.colorScheme.text.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun RankingTopBarPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            RankingTopBar()
        }
    }
}

/** 56dp 고정 높이에서 제목 한 줄이 큰 폰트 스케일을 견디는지 확인용. */
@Preview
@Composable
private fun RankingTopBarLargeFontScalePreview() {
    ChallengeTheme {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density = density.density, fontScale = 1.3f),
        ) {
            Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
                RankingTopBar()
            }
        }
    }
}
