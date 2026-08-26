package com.lwg.challenge.feature.ranking.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 포디움을 세우는 최소 참가자 수.
 *
 * 자리가 비면 빈 칸이 생겨 고장난 화면으로 보이고, 참가자 1명짜리 "개돼지왕" 은 자기 자신을
 * 왕으로 세우는 화면이라 농담이 성립하지 않는다.
 */
private const val PODIUM_MIN_PARTICIPANTS = 3

/** 포디움 3열. 표시 순서(2·1·3위)는 컴포넌트가 정하고, 여기서는 자리만 고정한다. */
@Immutable
data class PodiumState(
    val first: LoserRankingItemState,
    val second: LoserRankingItemState,
    val third: LoserRankingItemState,
)

@Stable
sealed interface RankingUiState {

    @Immutable
    data object Loading : RankingUiState

    @Immutable
    data class Data(
        val entries: List<LoserRankingItemState>,
    ) : RankingUiState {

        /**
         * null 이면 포디움을 그리지 않는다(참가자 3명 미만 — 신규 사용자의 기본 상태).
         *
         * get() 이 아니라 생성 시점 val 인 이유: 매 호출마다 새 인스턴스가 나오면 이 값을 받는
         * 하위 Composable 의 skipping 이 깨진다 (CLAUDE.md "UI State 파싱 규칙").
         */
        val podium: PodiumState? = if (entries.size >= PODIUM_MIN_PARTICIPANTS) {
            PodiumState(first = entries[0], second = entries[1], third = entries[2])
        } else {
            null
        }

        /** 명단은 1위부터 전원이다 — 포디움과 중복되는 것이 정본이다. */
        val isRankListVisible: Boolean get() = entries.isNotEmpty()

        val isEmptyStateVisible: Boolean get() = entries.size < PODIUM_MIN_PARTICIPANTS
    }
}
