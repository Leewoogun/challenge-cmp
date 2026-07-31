package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChallengeDateTimeFormatsTest {

    @Test
    fun `wire 포맷 문자열을 LocalDateTime 으로 파싱한다`() {
        val parsed = ChallengeDateTimeFormats.parseWireDateTime("2026-07-28 15:04:05")

        assertEquals(LocalDateTime(2026, 7, 28, 15, 4, 5), parsed)
    }

    @Test
    fun `파싱 후 포맷하면 원문과 왕복 일치한다`() {
        val input = "2026-07-28 15:04:05"
        val parsed = ChallengeDateTimeFormats.parseWireDateTime(input)

        assertNotNull(parsed)
        assertEquals(input, parsed.format(ChallengeDateTimeFormats.WIRE_DATE_TIME))
    }

    @Test
    fun `날짜 전용 문자열을 LocalDate 로 파싱한다`() {
        assertEquals(LocalDate(2026, 7, 28), ChallengeDateTimeFormats.parseWireDate("2026-07-28"))
    }

    @Test
    fun `T 구분자 ISO 문자열은 null 을 반환한다`() {
        assertNull(ChallengeDateTimeFormats.parseWireDateTime("2026-07-28T15:04:05"))
    }

    @Test
    fun `빈 문자열은 null 을 반환한다`() {
        assertNull(ChallengeDateTimeFormats.parseWireDateTime(""))
        assertNull(ChallengeDateTimeFormats.parseWireDate(""))
    }

    @Test
    fun `쓰레기 문자열은 null 을 반환한다`() {
        assertNull(ChallengeDateTimeFormats.parseWireDateTime("not-a-date"))
        assertNull(ChallengeDateTimeFormats.parseWireDate("not-a-date"))
    }
}
