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
}
