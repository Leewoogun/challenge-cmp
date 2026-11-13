package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 버튼 색.
 *
 * @property background 메인 버튼 배경 (브랜드 오렌지)
 * @property text 메인 버튼 글씨
 * @property subBackground 보조 버튼 배경
 * @property subText 보조 버튼 글씨
 * @property border 외곽선 버튼 테두리
 */
@Immutable
data class ButtonColor(
    val background: Color,
    val text: Color,
    val subBackground: Color,
    val subText: Color,
    val border: Color,
)
