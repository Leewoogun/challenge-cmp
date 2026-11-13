package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 텍스트 색.
 *
 * @property primary 본문
 * @property secondary 보조 · 캡션
 * @property point 브랜드 오렌지 강조 글씨
 * @property onPoint 오렌지 배경 위 글씨 (라벨 등)
 */
@Immutable
data class TextColor(
    val primary: Color,
    val secondary: Color,
    val point: Color,
    val onPoint: Color,
)
