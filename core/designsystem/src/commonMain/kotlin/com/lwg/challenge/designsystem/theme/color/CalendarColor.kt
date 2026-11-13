package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 캘린더 색. 선택된 날짜 원은 `ChallengeTheme.brushes.fire` 를 쓴다.
 *
 * @property rangeBackground 기간 선택 시 시작~끝 사이를 잇는 띠
 * @property sundayText 일요일 날짜 · 요일 글씨
 * @property saturdayText 토요일 날짜 · 요일 글씨
 * @property disabledText 선택 불가(범위 밖 · 비활성) 날짜와 이웃 달 날짜 글씨
 */
@Immutable
data class CalendarColor(
    val rangeBackground: Color,
    val sundayText: Color,
    val saturdayText: Color,
    val disabledText: Color,
)
