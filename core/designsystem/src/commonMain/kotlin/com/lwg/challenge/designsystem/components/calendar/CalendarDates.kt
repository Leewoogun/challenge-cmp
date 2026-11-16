package com.lwg.challenge.designsystem.components.calendar

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

internal const val DAYS_IN_WEEK = 7
internal const val MONTH_GRID_WEEKS = 6

internal val LocalDate.firstOfMonth: LocalDate get() = LocalDate(year, month, 1)

internal fun LocalDate.startOfWeek(firstDayOfWeek: DayOfWeek): LocalDate {
    val offset = (dayOfWeek.isoDayNumber - firstDayOfWeek.isoDayNumber + DAYS_IN_WEEK) % DAYS_IN_WEEK
    return minus(offset, DateTimeUnit.DAY)
}

internal fun LocalDate.coerceIn(range: ClosedRange<LocalDate>): LocalDate = when {
    this < range.start -> range.start
    this > range.endInclusive -> range.endInclusive
    else -> this
}

/** 첫 요일부터 시작하는 요일 7개. */
internal fun weekDays(firstDayOfWeek: DayOfWeek): List<DayOfWeek> =
    List(DAYS_IN_WEEK) { DayOfWeek((firstDayOfWeek.isoDayNumber - 1 + it) % DAYS_IN_WEEK + 1) }

/**
 * 페이저 페이지 ↔ 날짜 변환. 월 표시는 한 페이지가 한 달(1일), 주 표시는 한 주(첫 요일)다.
 */
internal class CalendarPaging(private val config: CalendarConfig) {

    private val firstPageDate: LocalDate = pageStartOf(config.boundary.start)

    val pageCount: Int = pageOf(config.boundary.endInclusive) + 1

    fun pageOf(date: LocalDate): Int = when (config.style) {
        CalendarStyle.Month -> firstPageDate.monthsUntil(date.firstOfMonth)
        CalendarStyle.Week -> firstPageDate.daysUntil(date.startOfWeek(config.firstDayOfWeek)) / DAYS_IN_WEEK
    }

    fun pageStartDate(page: Int): LocalDate = when (config.style) {
        CalendarStyle.Month -> firstPageDate.plus(page, DateTimeUnit.MONTH)
        CalendarStyle.Week -> firstPageDate.plus(page * DAYS_IN_WEEK, DateTimeUnit.DAY)
    }

    /** 헤더에 표시할 달. 주 표시는 주의 가운데 날이 속한 달로 본다. */
    fun displayMonthOf(page: Int): LocalDate = when (config.style) {
        CalendarStyle.Month -> pageStartDate(page)
        CalendarStyle.Week -> pageStartDate(page).plus(DAYS_IN_WEEK / 2, DateTimeUnit.DAY).firstOfMonth
    }

    /** 페이지에 그릴 날짜를 주 단위로. 월 표시는 높이가 바뀌지 않도록 항상 6주를 채운다. */
    fun weeksOf(page: Int): List<List<LocalDate>> {
        val gridStart = pageStartDate(page).startOfWeek(config.firstDayOfWeek)
        val weekCount = when (config.style) {
            CalendarStyle.Month -> MONTH_GRID_WEEKS
            CalendarStyle.Week -> 1
        }
        return List(weekCount) { week ->
            List(DAYS_IN_WEEK) { day -> gridStart.plus(week * DAYS_IN_WEEK + day, DateTimeUnit.DAY) }
        }
    }

    private fun pageStartOf(date: LocalDate): LocalDate = when (config.style) {
        CalendarStyle.Month -> date.firstOfMonth
        CalendarStyle.Week -> date.startOfWeek(config.firstDayOfWeek)
    }
}
