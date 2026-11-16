package com.lwg.challenge.designsystem.components.calendar

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

/**
 * 캘린더 선택 방식과 현재 선택값.
 *
 * [select] 가 다음 선택값을 돌려주므로 호출부는 결과를 그대로 상태에 저장하면 된다.
 */
@Immutable
sealed interface CalendarSelection {

    /** 처음 열 때 보여줄 기준 날짜. 선택이 없으면 null. */
    val anchorDate: LocalDate?

    fun isSelected(date: LocalDate): Boolean

    fun select(date: LocalDate, config: CalendarConfig = CalendarConfig()): CalendarSelection

    /** 날짜 하나. */
    data class Single(val date: LocalDate? = null) : CalendarSelection {
        override val anchorDate: LocalDate? get() = date

        override fun isSelected(date: LocalDate): Boolean = this.date == date

        override fun select(date: LocalDate, config: CalendarConfig): CalendarSelection = copy(date = date)
    }

    /** 여러 날짜. 이미 선택된 날짜를 다시 누르면 해제된다. */
    data class Multiple(val dates: Set<LocalDate> = emptySet()) : CalendarSelection {
        override val anchorDate: LocalDate? get() = dates.minOrNull()

        override fun isSelected(date: LocalDate): Boolean = date in dates

        override fun select(date: LocalDate, config: CalendarConfig): CalendarSelection =
            copy(dates = if (date in dates) dates - date else dates + date)
    }

    /**
     * 시작~끝 기간. 시작만 고른 상태면 [end] 가 null 이다.
     *
     * 시작보다 앞선 날짜나 사이에 비활성 날짜가 끼는 날짜를 누르면 그 날짜부터 다시 시작한다.
     */
    data class Range(
        val start: LocalDate? = null,
        val end: LocalDate? = null,
    ) : CalendarSelection {
        override val anchorDate: LocalDate? get() = start

        val isComplete: Boolean get() = start != null && end != null

        override fun isSelected(date: LocalDate): Boolean = date == start || date == end

        fun isStart(date: LocalDate): Boolean = date == start

        fun isEnd(date: LocalDate): Boolean = date == end

        fun isBetween(date: LocalDate): Boolean =
            start != null && end != null && date > start && date < end

        override fun select(date: LocalDate, config: CalendarConfig): CalendarSelection {
            val start = start
            if (start == null || end != null || date <= start) return Range(start = date)

            val crossesDisabled = config.disabledDates.any { it > start && it < date }
            return if (crossesDisabled) Range(start = date) else copy(end = date)
        }
    }
}
