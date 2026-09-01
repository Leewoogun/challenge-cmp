package com.lwg.challenge.feature.mypage.contract

import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ArchiveMonthCursorTest {

    // 시계를 읽으면 이번 달이 바뀔 때마다 결과가 흔들린다 — 상한을 고정값으로 못박는다.
    private fun cursor(
        selected: YearMonth,
        latest: YearMonth = YearMonth(2026, 8),
    ) = ArchiveMonthCursor(selected = selected, latest = latest)

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

    // 미래 달에는 종료된 챌린지가 있을 수 없어 이번 달이 상한이다. 2차 개정 이후 이 화면에 남은
    // 유일한 비활성이다.
    @Test
    fun `이번 달에서는 다음으로 갈 수 없다`() {
        val current = cursor(selected = YearMonth(2026, 8))

        assertFalse(current.canGoNext)
        assertSame(current, current.next())
    }

    // canGoNext 의 부정과 지금은 같은 값이지만 묻는 질문이 다르다 — 이쪽은 CTA 발동 조건이고
    // 저쪽은 이동 경계다. 상한 규칙이 바뀌면 둘이 갈라져야 하므로 각자 단언한다.
    @Test
    fun `이번 달에서만 isLatest 가 참이다`() {
        assertTrue(cursor(selected = YearMonth(2026, 8)).isLatest)
        assertFalse(cursor(selected = YearMonth(2026, 5)).isLatest)
    }

    // 2차 개정: 과거 하한을 폐기했다. 하한이 '최초 기록월' 이던 시절, 기록이 전부 이번 달인 계정에서
    // 하한과 상한이 같은 달로 겹쳐 양쪽 화살표가 동시에 죽었고 사용자가 화면을 고장으로 읽었다.
    // 이제 몇 번을 눌러도 계속 과거로 가고, 기록이 없는 달은 빈 달 화면으로 보인다.
    @Test
    fun `기록이 없어도 이전 달로 계속 갈 수 있다`() {
        val moved = cursor(selected = YearMonth(2026, 8))
            .previous()
            .previous()
            .previous()

        assertEquals(YearMonth(2026, 5), moved.selected)
    }

    // 빈 달을 건너뛰지 않는 것은 사용자 확정 사항이다. 이 타입은 커서 2개만 받을 뿐 '그 달에
    // 기록이 있는지' 를 알 방법 자체가 없고, 상한 안이기만 하면 이동이 열린다는 사실을 고정한다.
    @Test
    fun `데이터가 없는 달도 그대로 지난다`() {
        val middle = cursor(selected = YearMonth(2026, 5))

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

    // copy 로 selected 만 바꾸므로 상한은 이동 여부와 무관하게 보존돼야 한다.
    @Test
    fun `이동해도 상한은 그대로다`() {
        val moved = cursor(selected = YearMonth(2026, 5)).next().previous()

        assertEquals(YearMonth(2026, 8), moved.latest)
    }

    @Test
    fun `상한에서 이동을 시도해도 상한은 그대로다`() {
        val atLatest = cursor(selected = YearMonth(2026, 8)).next()

        assertEquals(YearMonth(2026, 8), atLatest.latest)
        assertEquals(YearMonth(2026, 8), atLatest.selected)
    }
}
