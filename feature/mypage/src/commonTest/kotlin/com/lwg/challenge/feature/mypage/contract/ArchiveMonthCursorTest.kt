package com.lwg.challenge.feature.mypage.contract

import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ArchiveMonthCursorTest {

    // 시계를 읽으면 이번 달이 바뀔 때마다 결과가 흔들린다 — 상한·하한을 고정값으로 못박는다.
    private fun cursor(
        selected: YearMonth,
        latest: YearMonth = YearMonth(2026, 8),
        earliest: YearMonth? = YearMonth(2025, 1),
    ) = ArchiveMonthCursor(selected = selected, latest = latest, earliest = earliest)

    @Test
    fun `다음 달로 이동하면 선택 월이 한 칸 올라간다`() {
        val moved = cursor(selected = YearMonth(2026, 5)).next()

        assertEquals(YearMonth(2026, 6), moved.selected)
    }

    @Test
    fun `이전 달로 이동하면 선택 월이 한 칸 내려간다`() {
        val moved = cursor(selected = YearMonth(2026, 5)).previous()

        assertEquals(YearMonth(2026, 4), moved.selected)
    }

    // 미래 달에는 종료된 챌린지가 있을 수 없어 이번 달이 상한이다.
    @Test
    fun `이번 달에서는 다음으로 갈 수 없다`() {
        val current = cursor(selected = YearMonth(2026, 8))

        assertFalse(current.canGoNext)
        assertSame(current, current.next())
    }

    @Test
    fun `하한 달에서는 이전으로 갈 수 없다`() {
        val oldest = cursor(selected = YearMonth(2025, 1))

        assertFalse(oldest.canGoPrevious)
        assertSame(oldest, oldest.previous())
    }

    // earliest 가 null 이면 종료한 챌린지가 하나도 없는 사용자다. 하한이 없다고 무한히 거슬러
    // 올라가게 두면 뒤에 아무것도 없는 달을 끝없이 넘기게 된다.
    @Test
    fun `기록이 하나도 없으면 이전으로 갈 수 없다`() {
        val empty = cursor(selected = YearMonth(2026, 8), earliest = null)

        assertFalse(empty.canGoPrevious)
        assertSame(empty, empty.previous())
    }

    // 빈 달을 건너뛰지 않는 것은 사용자 확정 사항이다. 이 타입은 커서 3개만 받을 뿐 '그 달에
    // 기록이 있는지' 를 알 방법 자체가 없고, 경계 안이기만 하면 이동이 열린다는 사실을 고정한다.
    @Test
    fun `데이터가 없는 달도 그대로 지난다`() {
        val middle = cursor(selected = YearMonth(2026, 5))

        assertTrue(middle.canGoPrevious)
        assertTrue(middle.canGoNext)
        assertEquals(YearMonth(2026, 4), middle.previous().selected)
    }

    // 월 숫자만 더하고 빼면 12월 다음이 13월이 된다 — 연 경계를 넘는지 확인한다.
    @Test
    fun `연초에서 이전으로 가면 작년 십이월이 된다`() {
        val moved = cursor(selected = YearMonth(2026, 1)).previous()

        assertEquals(YearMonth(2025, 12), moved.selected)
        assertEquals("2025년 12월", moved.label)
    }

    @Test
    fun `연말에서 다음으로 가면 내년 일월이 된다`() {
        val moved = cursor(selected = YearMonth(2025, 12)).next()

        assertEquals(YearMonth(2026, 1), moved.selected)
        assertEquals("2026년 1월", moved.label)
    }

    // 한 자리 월에 0 을 채우지 않는다 — "2026년 03월" 은 이 화면의 표기가 아니다.
    @Test
    fun `라벨이 연과 월 형식이다`() {
        assertEquals("2026년 8월", cursor(selected = YearMonth(2026, 8)).label)
        assertEquals("2026년 3월", cursor(selected = YearMonth(2026, 3)).label)
    }

    // copy 로 selected 만 바꾸므로 상한·하한은 이동 여부와 무관하게 보존돼야 한다.
    @Test
    fun `이동해도 상한과 하한은 그대로다`() {
        val moved = cursor(selected = YearMonth(2026, 5)).next().previous()

        assertEquals(YearMonth(2026, 8), moved.latest)
        assertEquals(YearMonth(2025, 1), moved.earliest)
    }

    @Test
    fun `경계에서 이동을 시도해도 상한과 하한은 그대로다`() {
        val atLatest = cursor(selected = YearMonth(2026, 8)).next()
        val atEarliest = cursor(selected = YearMonth(2025, 1)).previous()

        assertEquals(YearMonth(2026, 8), atLatest.latest)
        assertEquals(YearMonth(2025, 1), atLatest.earliest)
        assertEquals(YearMonth(2026, 8), atEarliest.latest)
        assertEquals(YearMonth(2025, 1), atEarliest.earliest)
    }
}
