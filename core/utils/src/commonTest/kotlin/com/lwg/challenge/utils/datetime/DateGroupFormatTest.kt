package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 날짜 그룹 헤더 문구와 행의 시각 표기.
 *
 * `ElapsedTimeFormatTest` 와 어휘가 겹치는 단언이 있는데 축이 다르다 — 여기서는 시각을 아예 보지
 * 않는다. 두 함수가 같은 입력에 다른 답을 내는 자리를 아래 "어제 밤" 케이스가 고정한다.
 */
class DateGroupFormatTest {

    private val today = LocalDate(2026, 8, 5)

    @Test
    fun `같은 날짜면 오늘 을 표시한다`() {
        assertEquals("오늘", today.toDateGroupLabel(today))
    }

    @Test
    fun `달력상 하루 전이면 어제 를 표시한다`() {
        assertEquals("어제", LocalDate(2026, 8, 4).toDateGroupLabel(today))
    }

    @Test
    fun `이틀 전이면 월과 일을 표시한다`() {
        assertEquals("8월 3일", LocalDate(2026, 8, 3).toDateGroupLabel(today))
    }

    /**
     * 같은 날짜를 `toElapsedKoreanString` 은 "2시간 전" 으로 읽는다. 그룹은 달력 축이라 시각을 보지
     * 않는 것이 정상이고, 이 차이가 없어지면 헤더가 시각에 따라 흔들린다.
     */
    @Test
    fun `어제 밤은 오늘 새벽에 봐도 어제 다`() {
        assertEquals("어제", LocalDate(2026, 8, 4).toDateGroupLabel(today))
    }

    @Test
    fun `날짜 표기에 0 패딩을 붙이지 않는다`() {
        assertEquals("8월 3일", LocalDate(2026, 8, 3).toDateGroupLabel(LocalDate(2026, 9, 20)))
    }

    @Test
    fun `월이 바뀌어도 달력상 하루 전이면 어제 다`() {
        assertEquals("어제", LocalDate(2026, 8, 31).toDateGroupLabel(LocalDate(2026, 9, 1)))
    }

    @Test
    fun `연이 바뀌어도 달력상 하루 전이면 어제 다`() {
        assertEquals("어제", LocalDate(2025, 12, 31).toDateGroupLabel(LocalDate(2026, 1, 1)))
    }

    @Test
    fun `연 경계를 넘긴 날짜는 그 날의 월과 일로 표시한다`() {
        assertEquals("12월 30일", LocalDate(2025, 12, 30).toDateGroupLabel(LocalDate(2026, 1, 3)))
    }

    /** 서버와 기기 시계가 어긋나 미래 날짜가 와도 "오늘" 로 흡수하지 않는다. */
    @Test
    fun `기준보다 미래인 날짜는 월과 일로 표시한다`() {
        assertEquals("8월 6일", LocalDate(2026, 8, 6).toDateGroupLabel(today))
    }

    @Test
    fun `시각은 24시간제 HH mm 로 표시한다`() {
        assertEquals("20:05", LocalDateTime(2026, 8, 5, 20, 5, 0).toClockTimeText())
    }

    @Test
    fun `한 자리 시와 분에 0 패딩을 붙인다`() {
        assertEquals("09:00", LocalDateTime(2026, 8, 5, 9, 0, 0).toClockTimeText())
    }

    @Test
    fun `자정은 00 00 이다`() {
        assertEquals("00:00", LocalDateTime(2026, 8, 5, 0, 0, 0).toClockTimeText())
    }

    /** 초는 표기에 들어가지 않는다 — 알림 목록에서 초 단위는 아무 행동도 바꾸지 않는다. */
    @Test
    fun `초는 표기에서 버린다`() {
        assertEquals("23:59", LocalDateTime(2026, 8, 5, 23, 59, 59).toClockTimeText())
    }
}
