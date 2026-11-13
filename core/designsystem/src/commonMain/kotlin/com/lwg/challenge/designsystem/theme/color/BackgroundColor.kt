package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 배경 색.
 *
 * @property screen 화면 전체 배경
 * @property card 카드 · 상단바 · 시트 · 상태바
 * @property cardSub 카드 안의 보조 영역
 * @property field 입력창 · 아이콘 박스 · 칩 · 서명란 등 카드 위에 채워진 요소
 */
@Immutable
data class BackgroundColor(
    val screen: Color,
    val card: Color,
    val cardSub: Color,
    val field: Color,
)
