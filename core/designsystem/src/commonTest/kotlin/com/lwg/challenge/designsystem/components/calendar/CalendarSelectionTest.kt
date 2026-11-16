package com.lwg.challenge.designsystem.components.calendar

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarSelectionTest {

    private val d10 = LocalDate(2026, 11, 10)
    private val d13 = LocalDate(2026, 11, 13)
    private val d20 = LocalDate(2026, 11, 20)

    @Test
    fun `Single 은 누른 날짜로 바뀐다`() {
        assertEquals(CalendarSelection.Single(d20), CalendarSelection.Single(d13).select(d20))
    }

    @Test
    fun `Multiple 은 같은 날짜를 다시 누르면 해제된다`() {
        val selected = CalendarSelection.Multiple().select(d13).select(d20).select(d13)

        assertEquals(CalendarSelection.Multiple(setOf(d20)), selected)
    }

    @Test
    fun `Range 는 시작 다음 끝 순서로 채워진다`() {
        val selected = CalendarSelection.Range().select(d13).select(d20)

        assertEquals(CalendarSelection.Range(d13, d20), selected)
    }

    @Test
    fun `Range 시작보다 앞선 날짜를 누르면 그 날짜부터 다시 시작한다`() {
        assertEquals(CalendarSelection.Range(d10), CalendarSelection.Range(d13).select(d10))
    }

    @Test
    fun `Range 완성 후 다시 누르면 새 시작이 된다`() {
        assertEquals(CalendarSelection.Range(d10), CalendarSelection.Range(d13, d20).select(d10))
    }

    @Test
    fun `Range 사이에 비활성 날짜가 끼면 끝이 아니라 새 시작이 된다`() {
        val config = CalendarConfig(disabledDates = setOf(LocalDate(2026, 11, 15)))

        assertEquals(CalendarSelection.Range(d20), CalendarSelection.Range(d13).select(d20, config))
    }
}
