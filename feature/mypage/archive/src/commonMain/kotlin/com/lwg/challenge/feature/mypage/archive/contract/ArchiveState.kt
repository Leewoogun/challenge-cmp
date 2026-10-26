package com.lwg.challenge.feature.mypage.archive.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

@Stable
sealed interface ArchiveUiState {

    /** 첫 로딩. 월 전환은 응답이 올 때까지 이전 [Data] 를 그대로 둔다. */
    @Immutable
    data object Loading : ArchiveUiState

    /** 첫 로딩 실패. 월 전환 실패는 이전 [Data] 를 덮지 않고 스낵바로만 알린다. */
    @Immutable
    data object Error : ArchiveUiState

    /**
     * 한 달치 조회 결과. 월 이동 바와 목록을 한 값에 담아 "8월 바 + 7월 카드" 같은 화면이 나올 수 없게 한다.
     *
     * @property challenges 빈 목록도 실패가 아니라 성공한 0건이다.
     * @property firstArchivedMonth 가장 오래된 종료 기록이 있는 달. 기록이 없으면 null.
     *   문구용이지 `<` 를 막는 하한이 아니다 — 하한이 있던 시절 양쪽 화살표가 함께 죽어 고장으로 읽혔다.
     * @property canGoNext 미래 달에는 종료된 챌린지가 있을 수 없어 이번 달이 상한이다.
     * @property isThisMonth 지금은 `!canGoNext` 와 같지만, 상한 규칙이 바뀌어도 CTA 조건이 딸려가지 않게 따로 둔다.
     */
    @Immutable
    data class Data(
        val selectedMonth: YearMonth,
        val challenges: List<ChallengeHistory>,
        val firstArchivedMonth: YearMonth?,
        val canGoNext: Boolean,
        val isThisMonth: Boolean,
    ) : ArchiveUiState {

        val monthLabel: String get() = "${selectedMonth.year}년 ${selectedMonth.month.number}월"

        /** 빈 달을 연속으로 지날 때 탭이 먹었는지 보이도록 제목에도 월을 넣는다. */
        val emptyMonthTitle: String get() = "${selectedMonth.month.number}월엔 끝낸 챌린지가 없어요"

        val isEmpty: Boolean get() = challenges.isEmpty()

        val countText: String get() = "${challenges.size}건"

        /** "챌린지 만들기" CTA 화면인지. 기록이 0건이어도 이번 달일 때만 — 과거 달을 보는 사용자는 회고 중이다. */
        val showsNoRecordsEver: Boolean
            get() = firstArchivedMonth == null && isThisMonth

        /** 첫 기록보다 이전 달이면 그 월을 알려 "더 가도 없다" 를 대신 말한다. */
        val emptyMonthSubtitle: String
            get() = if (firstArchivedMonth != null && selectedMonth < firstArchivedMonth) {
                "첫 기록은 ${firstArchivedMonth.year}년 ${firstArchivedMonth.month.number}월이에요"
            } else {
                "화살표로 다른 달을 볼 수 있어요"
            }
    }
}
