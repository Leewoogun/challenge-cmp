package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryDto
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryResponse

/**
 * 보관함 DTO → Domain 매핑.
 *
 * 서버가 준 순서(`challengeDate DESC`)를 그대로 보존한다 — 정렬은 서버 책임이다.
 */
fun ChallengeHistoryResponse.toChallengeHistories(): MappedList<ChallengeHistory> {
    val mapped = data.histories.map(ChallengeHistoryDto::toChallengeHistory)
    return MappedList(mapped.filterNotNull(), mapped.count { it == null })
}

/**
 * `challengeDate` 나 `myResult` 를 못 읽으면 항목을 통째로 드롭한다.
 *
 * 홈 목록이 모르는 `myResult` 를 null 로 흡수하고 카드는 살리는 것과 반대 판단이다. 거기서는 결과를
 * 몰라도 진행 중 카드가 성립하지만, 보관함은 **끝난 챌린지만** 담아 결과 pill 이 카드의 요지이고
 * 날짜는 월 그룹 키라 둘 중 하나가 없으면 그릴 자리 자체가 정해지지 않는다.
 */
private fun ChallengeHistoryDto.toChallengeHistory(): ChallengeHistory? {
    val date = challengeDate ?: return null
    val outcome = myResult.toChallengeOutcomeOrNull() ?: return null
    return ChallengeHistory(
        challengeId = challengeId,
        challengeDate = date,
        opponentNickname = opponentNickname,
        myMission = myMission,
        opponentMission = opponentMission,
        bet = bet,
        myResult = outcome,
    )
}
