package com.lwg.challenge.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lwg.challenge.designsystem.theme.color.GradientColor

/**
 * 프로젝트 그라데이션 브러시.
 */
@Immutable
data class ChallengeBrushes(
    /** fireStart → fireEnd (135deg). 메인 CTA / 히어로. */
    val fire: Brush,
    /** cardStart → cardEnd (135deg). 카드 표면 미묘한 depth. */
    val card: Brush,
    /** glow → transparent (radial). 화면 상단 후광. */
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

internal fun buildChallengeBrushes(gradient: GradientColor): ChallengeBrushes =
    ChallengeBrushes(
        fire = linearGradient135(start = gradient.fireStart, end = gradient.fireEnd),
        card = linearGradient135(start = gradient.cardStart, end = gradient.cardEnd),
        glow = radialGlow(center = gradient.glow),
    )

val LocalChallengeBrushes = staticCompositionLocalOf<ChallengeBrushes> {
    error("ChallengeBrushes not provided. Wrap your composable in ChallengeTheme {}.")
}
