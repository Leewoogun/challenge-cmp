package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 아이콘 tint 색. 기준은 [TextColor] 와 같다.
 *
 * @property primary 기본 아이콘
 * @property secondary 보조 아이콘
 * @property point 브랜드 오렌지 강조 아이콘
 * @property onPoint 오렌지 배경 위 아이콘
 */
@Immutable
data class IconColor(
    val primary: Color,
    val secondary: Color,
    val point: Color,
    val onPoint: Color,
)
