package com.lwg.challenge.feature.ranking.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry

// 자리가 비면 고장난 화면으로 보이고, 1명짜리 "개돼지왕" 은 자기 자신을 왕으로 세워 농담이 성립하지 않는다.
private const val TOP_THREE_MIN_PARTICIPANTS = 3

private const val MY_LABEL = "나"
private const val NO_RECORD_TEXT = "아직 기록 없음"
private const val FIRST_RANK = 1
private const val THIRD_RANK = 3

/** Top3 카드의 3자리. 표시 순서(2·1·3위)는 컴포넌트가 정한다. */
@Immutable
data class RankingTopThree(
    val first: LoserRankingEntry,
    val second: LoserRankingEntry,
    val third: LoserRankingEntry,
)

@Stable
sealed interface RankingUiState {

    @Immutable
    data object Loading : RankingUiState

    @Immutable
    data object Error : RankingUiState

    @Immutable
    data class Data(
        val entries: List<LoserRankingEntry>,
    ) : RankingUiState {

        val topThree: RankingTopThree? = if (entries.size >= TOP_THREE_MIN_PARTICIPANTS) {
            RankingTopThree(first = entries[0], second = entries[1], third = entries[2])
        } else {
            null
        }

        /** 명단은 1위부터 전원이라 Top3 카드와 겹친다. */
        val isRankListVisible: Boolean get() = entries.isNotEmpty()

        val isEmptyStateVisible: Boolean get() = entries.size < TOP_THREE_MIN_PARTICIPANTS
    }
}

/** 내 행은 닉네임 대신 "나". 아바타 이니셜도 이 값을 쓴다. */
internal val LoserRankingEntry.displayName: String get() = if (isMe) MY_LABEL else nickname

/** 카드 보더를 error 계열로 올릴지. 2위도 포함돼 순위 숫자 색([RankingNumberLabel])과 축이 다르다. */
internal val LoserRankingEntry.isTopThree: Boolean get() = rank <= THIRD_RANK

/** 기록 없는 사람과 전승자는 둘 다 `0패 · 패배율 0%` 라 기록 없음을 따로 적는다. */
internal val LoserRankingEntry.listCaption: String
    get() = if (totalChallenges == 0) NO_RECORD_TEXT else "${losses}패 · 패배율 ${lossRate}%"

/** 연패 절은 1위·연패 중일 때만. 정렬 1차 키가 패 수라 연패 0 인 1위가 실제로 나와 `🐷0연패` 를 막는다. */
internal val LoserRankingEntry.cardCaption: String
    get() = if (rank == FIRST_RANK && currentLossStreak > 0) {
        "🐷${currentLossStreak}연패 · ${losses}패"
    } else {
        "${losses}패"
    }

/** 연패 중일 때만 붙는 뱃지. null 이면 그리지 않는다. */
internal val LoserRankingEntry.streakBadge: String?
    get() = if (currentLossStreak > 0) "🐷$currentLossStreak" else null
