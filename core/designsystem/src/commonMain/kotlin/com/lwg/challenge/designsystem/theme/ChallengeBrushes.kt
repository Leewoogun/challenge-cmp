package com.lwg.challenge.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 프로젝트 그라데이션 브러시.
 *
 * 접근: `ChallengeTheme.brushes.<name>`.
 * 색상 stops는 [ChallengeColorScheme]의 gradientPrimaryStart/End, gradientCardStart/End,
 * glowPrimary 에서 가져온다.
 */
@Immutable
data class ChallengeBrushes(
    /** gradientPrimaryStart → gradientPrimaryEnd (135deg). 메인 CTA / 히어로. */
    val fire: Brush,
    /** gradientCardStart → gradientCardEnd (135deg). 카드 표면 미묘한 depth. */
    val card: Brush,
    /** glowPrimary → transparent (radial). 화면 상단 후광. */
    val glow: Brush,
)

private fun linearGradient135(start: Color, end: Color): Brush =
    Brush.linearGradient(
        colors = listOf(start, end),
        start = Offset.Zero,
        end = Offset.Infinite,
    )

private fun radialGlow(center: Color): Brush =
    Brush.radialGradient(
        colors = listOf(center, Color.Transparent),
    )

internal fun buildChallengeBrushes(colorScheme: ChallengeColorScheme): ChallengeBrushes =
    ChallengeBrushes(
        fire = linearGradient135(
            start = colorScheme.gradientPrimaryStart,
            end = colorScheme.gradientPrimaryEnd,
        ),
        card = linearGradient135(
            start = colorScheme.gradientCardStart,
            end = colorScheme.gradientCardEnd,
        ),
        glow = radialGlow(center = colorScheme.glowPrimary),
    )

val LocalChallengeBrushes = staticCompositionLocalOf<ChallengeBrushes> {
    error("ChallengeBrushes not provided. Wrap your composable in ChallengeTheme {}.")
}
