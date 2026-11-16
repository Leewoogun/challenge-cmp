package com.lwg.challenge.designsystem.components.calendar

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalendarPagingTest {

    private val boundary = LocalDate(2026, 11, 13)..LocalDate(2027, 2, 3)

    @Test
    fun `월 표시는 boundary 가 걸친 달 수만큼 페이지를 만든다`() {
        val paging = CalendarPaging(CalendarConfig(boundary = boundary))

        assertEquals(4, paging.pageCount)
        assertEquals(LocalDate(2027, 1, 1), paging.pageStartDate(2))
        assertEquals(2, paging.pageOf(LocalDate(2027, 1, 31)))
    }

    @Test
    fun `월 표시 그리드는 6주이고 첫 칸은 주 시작 요일이다`() {
        val weeks = CalendarPaging(CalendarConfig(boundary = boundary)).weeksOf(0)

        assertEquals(MONTH_GRID_WEEKS, weeks.size)
        assertEquals(LocalDate(2026, 11, 1), weeks.first().first())
        assertTrue(weeks.all { week -> week.first().dayOfWeek == DayOfWeek.SUNDAY })
    }

    @Test
    fun `월요일 시작이면 그리드가 앞 달 월요일부터 채워진다`() {
        val config = CalendarConfig(boundary = boundary, firstDayOfWeek = DayOfWeek.MONDAY)

        assertEquals(LocalDate(2026, 10, 26), CalendarPaging(config).weeksOf(0).first().first())
    }

    @Test
    fun `주 표시는 boundary 가 걸친 주 수만큼 페이지를 만든다`() {
        val paging = CalendarPaging(CalendarConfig(boundary = boundary, style = CalendarStyle.Week))

        assertEquals(LocalDate(2026, 11, 8), paging.pageStartDate(0))
        assertEquals(paging.pageOf(LocalDate(2027, 2, 3)) + 1, paging.pageCount)
        assertEquals(1, paging.weeksOf(0).size)
    }

    @Test
    fun `주 표시 헤더 달은 주 가운데 날 기준이다`() {
        val paging = CalendarPaging(CalendarConfig(boundary = boundary, style = CalendarStyle.Week))
        val page = paging.pageOf(LocalDate(2026, 11, 29))

        assertEquals(LocalDate(2026, 12, 1), paging.displayMonthOf(page))
    }
}
