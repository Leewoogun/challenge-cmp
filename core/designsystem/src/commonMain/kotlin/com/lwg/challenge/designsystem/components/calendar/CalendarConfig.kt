package com.lwg.challenge.designsystem.components.calendar

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * 캘린더의 선택 가능 범위와 표시 방식.
 *
 * @property boundary 선택·이동 가능한 날짜 범위. 범위 밖 달(주)로는 넘어가지 않는다.
 * @property disabledDates 범위 안이지만 선택할 수 없는 날짜
 * @property style 한 달 단위([CalendarStyle.Month]) 또는 한 주 단위([CalendarStyle.Week]) 표시
 * @property firstDayOfWeek 한 주의 시작 요일
 * @property yearMonthSelection 헤더의 연·월을 눌러 연·월 선택 화면으로 바로 이동할 수 있는지
 * @property showAdjacentMonthDays 월 표시에서 앞뒤 달 날짜를 흐리게 채워 보여줄지
 */
@Immutable
data class CalendarConfig(
    val boundary: ClosedRange<LocalDate> = DefaultBoundary,
    val disabledDates: Set<LocalDate> = emptySet(),
    val style: CalendarStyle = CalendarStyle.Month,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    val yearMonthSelection: Boolean = true,
    val showAdjacentMonthDays: Boolean = true,
) {
    init {
        require(!boundary.isEmpty()) { "boundary 가 비어 있습니다: $boundary" }
    }

    fun isSelectable(date: LocalDate): Boolean = date in boundary && date !in disabledDates

    companion object {
        val DefaultBoundary: ClosedRange<LocalDate> = LocalDate(1900, 1, 1)..LocalDate(2100, 12, 31)
    }
}

enum class CalendarStyle {
    Month,
    Week,
}
