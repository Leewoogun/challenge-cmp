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

internal const val RANKING_TITLE = "개돼지 랭킹 🐷"

/**
 * 랭킹 탭 상단바.
 *
 * 부제("패배의 왕좌 — 많이 진 놈이 대장")는 여기가 아니라 본문 최상단([RankingSubtitle])에 있다.
 * [ChallengeBaseTopBar] 높이가 56dp 고정이라 제목(lineHeight 28sp) + 2dp + 부제(lineHeight 16sp)
 * 2줄 구성은 폰트 스케일 1.3x 에서 약 59dp 가 되어 잘린다. 공용 56dp 는 전 화면이 공유하므로
 * 랭킹 사정으로 바꾸지 않는다. (design.md §1.1)
 */
@Composable
internal fun RankingTopBar(
    modifier: Modifier = Modifier,
) {
    ChallengeBaseTopBar(modifier = modifier) {
        Text(
            text = RANKING_TITLE,
            style = ChallengeTheme.typography.bold20,
            color = ChallengeTheme.colorScheme.onBackground,
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
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
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
            Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
                RankingTopBar()
            }
        }
    }
}
