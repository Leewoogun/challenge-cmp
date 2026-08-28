package com.lwg.challenge.feature.mypage.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome

@Stable
sealed interface ArchiveUiState {

    @Immutable
    data object Loading : ArchiveUiState

    /**
     * 조회 실패. 보관함은 화면 전체가 목록 하나라 실패 영역도 화면 전체다 — 마이페이지가 화면 Error 를
     * 기각한 사유("성공한 데이터까지 못 쓰게 된다")가 여기서는 성립하지 않는다 (design.md §2.5.4.1).
     *
     * 서버 message 를 화면에 쓰지 않아 `data object` 로 충분하다.
     */
    @Immutable
    data object Failure : ArchiveUiState

    @Immutable
    data class Data(
        val months: List<ArchiveMonthState>,
    ) : ArchiveUiState {

        val isEmpty: Boolean get() = months.isEmpty()
    }
}

/**
 * 월 섹션 1개. 그룹핑은 ViewModel 매핑 시점에 [toArchiveMonths] 로 확정한다 —
 * `get()` 에서 만들면 매 호출마다 새 List 인스턴스가 나와 하위 Composable 의 skipping 이 깨진다.
 */
@Immutable
data class ArchiveMonthState(
    val key: String,
    val label: String,
    val challenges: List<ArchiveChallengeItemState>,
) {
    val countText: String get() = "${challenges.size}건"
}

@Immutable
data class ArchiveChallengeItemState(private val model: ChallengeHistory) {

    val challengeId: Long get() = model.challengeId
    val opponentNickname: String get() = model.opponentNickname
    val myMission: String get() = model.myMission
    val opponentMission: String get() = model.opponentMission
    val opponentMissionLabel: String get() = "${model.opponentNickname}의 미션"
    val betText: String get() = "내기: ${model.bet}"
    val outcome: ChallengeOutcome get() = model.myResult

    /** 월 헤더가 연·월을 이미 말하지만, 카드만 떼어 봐도 읽히도록 월을 반복한다. */
    val dateText: String get() = "${model.challengeDate.monthNumber}월 ${model.challengeDate.day}일"
}

/**
 * 종료 챌린지를 최신 월부터 묶는다.
 *
 * 그룹 키가 `challengeDate` 인 이유: 판정이 자정 직후 배치라 `completed_at` 으로 묶으면 말일
 * 챌린지가 다음 달 헤더 아래로 들어가고, 카드에 적힌 날짜와 헤더가 서로 다른 말을 한다.
 *
 * 서버가 이미 `challengeDate DESC` 로 주지만 여기서 한 번 더 정렬한다 — 순서가 어긋난 응답이 오면
 * 같은 월 헤더가 목록에 두 번 나오는 형태로 깨지기 때문이다.
 */
internal fun List<ChallengeHistory>.toArchiveMonths(): List<ArchiveMonthState> =
    sortedByDescending { it.challengeDate }
        .groupBy { "${it.challengeDate.year}-${it.challengeDate.monthNumber}" }
        .map { (key, histories) ->
            val date = histories.first().challengeDate
            ArchiveMonthState(
                key = key,
                label = "${date.year}년 ${date.monthNumber}월",
                challenges = histories.map(::ArchiveChallengeItemState),
            )
        }
