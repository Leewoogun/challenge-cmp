@file:OptIn(ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class KstDeadlineTest {

    @Test
    fun `daysAhead 0 이면 KST 기준 오늘 날짜의 24시를 표시한다`() {
        val now = Instant.parse("2026-07-28T03:00:00Z") // KST 7/28 12:00

        assertEquals("7/28 24:00", kstDeadlineHintText(daysAhead = 0, now = now))
    }

    @Test
    fun `UTC 15시 이후는 KST 로 다음 날이므로 하루 뒤 날짜를 표시한다`() {
        val now = Instant.parse("2026-07-28T15:00:00Z") // KST 7/29 00:00

        assertEquals("7/29 24:00", kstDeadlineHintText(daysAhead = 0, now = now))
    }

    @Test
    fun `daysAhead 1 이면 KST 기준 하루 뒤 날짜를 표시한다`() {
        val now = Instant.parse("2026-07-28T03:00:00Z") // KST 7/28 12:00

        assertEquals("7/29 24:00", kstDeadlineHintText(daysAhead = 1, now = now))
    }

    @Test
    fun `월말에서 하루 뒤는 다음 달 1일이 된다`() {
        val now = Instant.parse("2026-07-31T03:00:00Z") // KST 7/31 12:00

        assertEquals("8/1 24:00", kstDeadlineHintText(daysAhead = 1, now = now))
    }

    @Test
    fun `연말에서 하루 뒤는 다음 해 1월 1일이 된다`() {
        val now = Instant.parse("2026-12-31T03:00:00Z") // KST 12/31 12:00

        assertEquals("1/1 24:00", kstDeadlineHintText(daysAhead = 1, now = now))
    }

    @Test
    fun `UTC 14시 59분 59초는 아직 KST 같은 날이라 날짜가 넘어가지 않는다`() {
        val now = Instant.parse("2026-07-28T14:59:59Z") // KST 7/28 23:59:59

        assertEquals("7/28 24:00", kstDeadlineHintText(daysAhead = 0, now = now))
    }

    @Test
    fun `윤년 2월 28일의 하루 뒤는 2월 29일이다`() {
        val now = Instant.parse("2028-02-28T05:00:00Z") // KST 2/28 14:00

        assertEquals("2/29 24:00", kstDeadlineHintText(daysAhead = 1, now = now))
    }

    @Test
    fun `평년 2월 28일의 하루 뒤는 3월 1일이다`() {
        val now = Instant.parse("2027-02-28T05:00:00Z") // KST 2/28 14:00

        assertEquals("3/1 24:00", kstDeadlineHintText(daysAhead = 1, now = now))
    }

    @Test
    fun `1970 이전 시각도 하루 밀리지 않는다`() {
        val now = Instant.parse("1969-12-31T14:59:59Z") // KST 12/31 23:59:59, epochSeconds 음수

        assertEquals("12/31 24:00", kstDeadlineHintText(daysAhead = 0, now = now))
    }
}
