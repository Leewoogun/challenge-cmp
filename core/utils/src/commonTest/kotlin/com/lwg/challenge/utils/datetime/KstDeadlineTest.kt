package com.lwg.challenge.utils.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals

class KstDeadlineTest {

    @Test
    fun `날짜의 월 일 요일 24시를 표시한다`() {
        assertEquals("11/20(금) 24:00", kstDeadlineHintText(LocalDate(2026, 11, 20)))
    }

    @Test
    fun `요일은 날짜마다 한글 한 글자로 바뀐다`() {
        assertEquals("11/16(월) 24:00", kstDeadlineHintText(LocalDate(2026, 11, 16)))
        assertEquals("11/22(일) 24:00", kstDeadlineHintText(LocalDate(2026, 11, 22)))
    }

    @Test
    fun `월말 다음 날은 다음 달 1일로 표시한다`() {
        assertEquals("8/1(토) 24:00", kstDeadlineHintText(LocalDate(2026, 8, 1)))
    }

    @Test
    fun `연말 12월 31일을 표시한다`() {
        assertEquals("12/31(목) 24:00", kstDeadlineHintText(LocalDate(2026, 12, 31)))
    }

    @Test
    fun `다음 해 1월 1일을 표시한다`() {
        assertEquals("1/1(금) 24:00", kstDeadlineHintText(LocalDate(2027, 1, 1)))
    }

    @Test
    fun `윤년 2월 29일을 표시한다`() {
        assertEquals("2/29(화) 24:00", kstDeadlineHintText(LocalDate(2028, 2, 29)))
    }

    @Test
    fun `평년 2월 28일 다음 날은 3월 1일이다`() {
        assertEquals("3/1(월) 24:00", kstDeadlineHintText(LocalDate(2027, 2, 28).plus(1, DateTimeUnit.DAY)))
    }
}
