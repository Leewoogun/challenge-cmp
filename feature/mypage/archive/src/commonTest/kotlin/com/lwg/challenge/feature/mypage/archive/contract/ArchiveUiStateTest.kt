package com.lwg.challenge.feature.mypage.archive.contract

import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class ArchiveUiStateTest {

    private fun data(selectedMonth: YearMonth) = ArchiveUiState.Data(
        selectedMonth = selectedMonth,
        challenges = emptyList(),
        firstArchivedMonth = null,
        canGoNext = false,
        isThisMonth = true,
    )

    // 한 자리 월에 0 을 채우지 않는다 — "2026년 03월" 은 이 화면의 표기가 아니다.
    @Test
    fun `라벨이 연과 월 형식이다`() {
        assertEquals("2026년 8월", data(YearMonth(2026, 8)).monthLabel)
        assertEquals("2026년 3월", data(YearMonth(2026, 3)).monthLabel)
    }
}
