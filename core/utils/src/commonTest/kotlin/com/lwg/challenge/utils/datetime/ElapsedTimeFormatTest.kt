package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ElapsedTimeFormatTest {

    private val now = LocalDateTime(2026, 8, 5, 12, 0, 0)

    @Test
    fun `경과가 0초면 방금 전 을 표시한다`() {
        assertEquals("방금 전", now.toElapsedKoreanString(now))
    }

    @Test
    fun `경과가 30초면 방금 전 을 표시한다`() {
        val createdAt = LocalDateTime(2026, 8, 5, 11, 59, 30)

        assertEquals("방금 전", createdAt.toElapsedKoreanString(now))
    }

    @Test
    fun `기기 시계보다 미래 시각이어도 방금 전 을 표시한다`() {
        val createdAt = LocalDateTime(2026, 8, 5, 12, 3, 0)

        assertEquals("방금 전", createdAt.toElapsedKoreanString(now))
    }

    @Test
    fun `정확히 1분 지났으면 1분 전 을 표시한다`() {
        val createdAt = LocalDateTime(2026, 8, 5, 11, 59, 0)

        assertEquals("1분 전", createdAt.toElapsedKoreanString(now))
    }

    @Test
    fun `59분 지났으면 59분 전 을 표시한다`() {
        val createdAt = LocalDateTime(2026, 8, 5, 11, 1, 0)

        assertEquals("59분 전", createdAt.toElapsedKoreanString(now))
    }

    @Test
    fun `정확히 60분 지났으면 1시간 전 을 표시한다`() {
        val createdAt = LocalDateTime(2026, 8, 5, 11, 0, 0)

        assertEquals("1시간 전", createdAt.toElapsedKoreanString(now))
    }

    @Test
    fun `23시간 지났으면 23시간 전 을 표시한다`() {
        val createdAt = LocalDateTime(2026, 8, 4, 13, 0, 0)

        assertEquals("23시간 전", createdAt.toElapsedKoreanString(now))
    }

    @Test
    fun `어제 밤 시각을 오늘 새벽에 보면 어제 대신 시간 전 을 표시한다`() {
        val dawnNow = LocalDateTime(2026, 8, 5, 1, 0, 0)
        val createdAt = LocalDateTime(2026, 8, 4, 23, 0, 0)

        assertEquals("2시간 전", createdAt.toElapsedKoreanString(dawnNow))
    }

    @Test
    fun `24시간을 넘겼고 달력상 하루 전이면 어제 를 표시한다`() {
        val eveningNow = LocalDateTime(2026, 8, 5, 20, 0, 0)
        val createdAt = LocalDateTime(2026, 8, 4, 10, 0, 0)

        assertEquals("어제", createdAt.toElapsedKoreanString(eveningNow))
    }

    @Test
    fun `26시간 전이어도 달력상 이틀 전이면 어제 대신 날짜를 표시한다`() {
        val dawnNow = LocalDateTime(2026, 8, 5, 1, 0, 0)
        val createdAt = LocalDateTime(2026, 8, 3, 23, 0, 0)

        assertEquals("8월 3일", createdAt.toElapsedKoreanString(dawnNow))
    }

    @Test
    fun `날짜 표기에 0 패딩을 붙이지 않는다`() {
        val laterNow = LocalDateTime(2026, 9, 20, 12, 0, 0)
        val createdAt = LocalDateTime(2026, 8, 3, 9, 0, 0)

        assertEquals("8월 3일", createdAt.toElapsedKoreanString(laterNow))
    }

    @Test
    fun `월이 바뀌어도 달력상 하루 전이면 어제 를 표시한다`() {
        val septemberNow = LocalDateTime(2026, 9, 1, 20, 0, 0)
        val createdAt = LocalDateTime(2026, 8, 31, 9, 0, 0)

        assertEquals("어제", createdAt.toElapsedKoreanString(septemberNow))
    }

    @Test
    fun `월 경계를 넘긴 날짜는 생성 시점의 월과 일로 표시한다`() {
        val septemberNow = LocalDateTime(2026, 9, 2, 9, 0, 0)
        val createdAt = LocalDateTime(2026, 8, 31, 23, 0, 0)

        assertEquals("8월 31일", createdAt.toElapsedKoreanString(septemberNow))
    }

    @Test
    fun `연이 바뀌어도 달력상 하루 전이면 어제 를 표시한다`() {
        val newYearNow = LocalDateTime(2026, 1, 1, 22, 0, 0)
        val createdAt = LocalDateTime(2025, 12, 31, 9, 0, 0)

        assertEquals("어제", createdAt.toElapsedKoreanString(newYearNow))
    }

    @Test
    fun `연 경계를 넘긴 날짜는 생성 시점의 월과 일로 표시한다`() {
        val newYearNow = LocalDateTime(2026, 1, 3, 10, 0, 0)
        val createdAt = LocalDateTime(2025, 12, 30, 22, 0, 0)

        assertEquals("12월 30일", createdAt.toElapsedKoreanString(newYearNow))
    }
}
