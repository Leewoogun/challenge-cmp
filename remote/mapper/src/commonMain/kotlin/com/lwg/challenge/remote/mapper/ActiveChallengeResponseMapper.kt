package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.remote.model.challenge.ActiveChallengeDto
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import com.lwg.challenge.remote.model.challenge.VerificationStatusDto

/**
 * Active challenge DTO → Domain 매핑.
 *
 * 시간 파싱은 DTO 직렬화기(WireLocalDateTimeSerializer)가 하고, 실패는 null 로 도착한다.
 * `deadline` 이 null 이면 해당 항목 제외([MappedList.droppedCount]에 집계) — 카드 전체가 deadline 에 걸려 있다.
 */
fun ActiveChallengeResponse.toActiveChallenges(): MappedList<ActiveChallenge> {
    val mapped = data.activeChallenges.map(ActiveChallengeDto::toActiveChallenge)
    return MappedList(mapped.filterNotNull(), mapped.count { it == null })
}

private fun ActiveChallengeDto.toActiveChallenge(): ActiveChallenge? {
    val deadline = deadline ?: return null
    return ActiveChallenge(
        challengeId = challengeId,
        myMission = myMission,
        opponentNickname = opponentNickname,
        opponentMission = opponentMission,
        deadline = deadline,
        myVerificationStatus = myVerificationStatus.toDomain(),
        opponentVerificationStatus = opponentVerificationStatus.toDomain(),
        bet = bet,
    )
}

private fun VerificationStatusDto.toDomain(): VerificationStatus = when (this) {
    VerificationStatusDto.PENDING -> VerificationStatus.PENDING
    VerificationStatusDto.VERIFIED -> VerificationStatus.VERIFIED
    VerificationStatusDto.FAILED -> VerificationStatus.FAILED
}
