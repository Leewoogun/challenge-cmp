package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DeadlineAbsoluteTextTest {

    @Test
    fun `challengeDate 가 있으면 익일 0시 deadline 이 함께 와도 challengeDate 날짜로 그린다`() {
        val text = deadlineAbsoluteText(
            challengeDate = LocalDate(2026, 8, 3),
            deadline = LocalDateTime.parse("2026-08-04T00:00:00"),
        )

        assertEquals("8/3 24:00", text)
    }

    @Test
    fun `challengeDate 가 null 이고 deadline 이 0시면 전날 24시로 그린다`() {
        val text = deadlineAbsoluteText(
            challengeDate = null,
            deadline = LocalDateTime.parse("2026-08-04T00:00:00"),
        )

        assertEquals("8/3 24:00", text)
    }

    @Test
    fun `월 경계에서 0시 deadline 폴백은 전달 마지막 날 24시가 된다`() {
        val text = deadlineAbsoluteText(
            challengeDate = null,
            deadline = LocalDateTime.parse("2026-09-01T00:00:00"),
        )

        assertEquals("8/31 24:00", text)
    }

    @Test
    fun `연 경계에서 0시 deadline 폴백은 전년 12월 31일 24시가 된다`() {
        val text = deadlineAbsoluteText(
            challengeDate = null,
            deadline = LocalDateTime.parse("2027-01-01T00:00:00"),
        )

        assertEquals("12/31 24:00", text)
    }

    @Test
    fun `0시가 아닌 deadline 폴백은 그 시각을 그대로 그린다`() {
        val text = deadlineAbsoluteText(
            challengeDate = null,
            deadline = LocalDateTime.parse("2026-08-04T18:30:00"),
        )

        assertEquals("8/4 18:30", text)
    }

    @Test
    fun `0시가 아닌 deadline 폴백의 시각은 두 자리로 채운다`() {
        val text = deadlineAbsoluteText(
            challengeDate = null,
            deadline = LocalDateTime.parse("2026-09-01T09:05:00"),
        )

        assertEquals("9/1 09:05", text)
    }

    @Test
    fun `월과 일은 0 패딩 없이 그린다`() {
        val text = deadlineAbsoluteText(
            challengeDate = LocalDate(2026, 1, 5),
            deadline = LocalDateTime.parse("2026-01-06T00:00:00"),
        )

        assertEquals("1/5 24:00", text)
    }
}
