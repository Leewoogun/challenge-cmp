package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 선 색.
 *
 * @property border 카드 · 박스 테두리
 * @property divider 구분선
 * @property dividerSub 옅은 구분선
 */
@Immutable
data class LineColor(
    val border: Color,
    val divider: Color,
    val dividerSub: Color,
)
