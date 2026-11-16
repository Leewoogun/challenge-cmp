package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class RelativeTimeFormatTest {

    private val now = LocalDateTime(2026, 7, 28, 12, 0, 0)

    @Test
    fun `1시간 이상 남고 분이 있으면 X시간 Y분 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 28, 17, 32, 0)

        assertEquals("5시간 32분", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `정각이면 분 없이 X시간 만 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 28, 14, 0, 0)

        assertEquals("2시간", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `정확히 1시간 남으면 1시간 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 28, 13, 0, 0)

        assertEquals("1시간", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `1분 이상 1시간 미만이면 X분 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 28, 12, 59, 0)

        assertEquals("59분", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `정확히 1분 남으면 1분 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 28, 12, 1, 0)

        assertEquals("1분", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `1분 미만 양수면 곧 마감 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 28, 12, 0, 59)

        assertEquals("곧 마감", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `정확히 0초 남으면 마감 을 표시한다`() {
        assertEquals("마감", now.toRelativeKoreanString(now))
    }

    @Test
    fun `이미 지난 시각이면 마감 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 28, 11, 0, 0)

        assertEquals("마감", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `자정을 넘어가는 마감도 정확히 계산한다`() {
        val lateNow = LocalDateTime(2026, 7, 28, 23, 0, 0)
        val deadline = LocalDateTime(2026, 7, 29, 1, 30, 0)

        assertEquals("2시간 30분", deadline.toRelativeKoreanString(lateNow))
    }

    @Test
    fun `23시간 59분 남으면 일 단위 없이 시간 분 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 29, 11, 59, 0)

        assertEquals("23시간 59분", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `정확히 24시간 남으면 1일 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 29, 12, 0, 0)

        assertEquals("1일", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `24시간 이상이면 분은 생략한다`() {
        val deadline = LocalDateTime(2026, 7, 29, 12, 1, 0)

        assertEquals("1일", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `24시간 이상이고 남는 시간이 있으면 N일 X시간 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 29, 13, 30, 0)

        assertEquals("1일 1시간", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `48시간 남으면 2일 을 표시한다`() {
        val deadline = LocalDateTime(2026, 7, 30, 12, 0, 0)

        assertEquals("2일", deadline.toRelativeKoreanString(now))
    }

    @Test
    fun `2184시간 남으면 91일 을 표시한다`() {
        val deadline = LocalDateTime(2026, 10, 27, 12, 0, 0)

        assertEquals("91일", deadline.toRelativeKoreanString(now))
    }
}
