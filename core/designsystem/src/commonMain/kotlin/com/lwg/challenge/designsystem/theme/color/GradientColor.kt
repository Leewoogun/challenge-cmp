package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 그라데이션 브러시(`ChallengeTheme.brushes`)의 원색.
 *
 * @property fireStart fire 브러시 시작 (메인 CTA · 히어로)
 * @property fireEnd fire 브러시 끝
 * @property cardStart card 브러시 시작 (카드 표면 depth)
 * @property cardEnd card 브러시 끝
 * @property glow 화면 상단 후광 중심색
 */
@Immutable
data class GradientColor(
    val fireStart: Color,
    val fireEnd: Color,
    val cardStart: Color,
    val cardEnd: Color,
    val glow: Color,
)
