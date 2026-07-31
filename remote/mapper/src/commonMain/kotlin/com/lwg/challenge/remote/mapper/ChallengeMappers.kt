package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.remote.model.challenge.ChallengeCreateBody
import com.lwg.challenge.remote.model.challenge.DeadlineTypeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse

/**
 * 챌린지 신청 / 응답 DTO ↔ Domain 매핑.
 *
 * 시간 파싱은 DTO 직렬화기(WireLocalDateTimeSerializer)가 하고, 실패는 null 로 도착한다.
 * `deadline` 이 null 이면 해당 항목 제외([MappedList.droppedCount]에 집계),
 * `createdAt` null 은 그대로 통과 — 카드 UI 가 deadline 하나에 걸려 있어 중요도가 갈린다.
 *
 * `challengeDate` 는 도메인으로 넘기지 않는다 — 카운트다운이 `deadline` 기준이라 UI 가 쓰지 않는다.
 */

fun ReceivedChallengesResponse.toReceivedChallenges(): MappedList<ReceivedChallenge> {
    val mapped = data.challenges.map(ReceivedChallengeDto::toReceivedChallenge)
    return MappedList(mapped.filterNotNull(), mapped.count { it == null })
}

fun ReceivedChallengeDto.toReceivedChallenge(): ReceivedChallenge? {
    val deadline = deadline ?: return null
    return ReceivedChallenge(
        challengeId = challengeId,
        challengerId = challengerId,
        challengerNickname = challengerNickname,
        challengerProfileImageUrl = challengerProfileImageUrl,
        challengerMission = challengerMission,
        betContent = betContent,
        deadline = deadline,
        createdAt = createdAt,
    )
}

fun ChallengeCreateInput.toBody(): ChallengeCreateBody = ChallengeCreateBody(
    opponentId = opponentId,
    myMission = myMission,
    betContent = betContent,
    deadlineType = deadlineType.toDto(),
)

fun DeadlineType.toDto(): DeadlineTypeDto = when (this) {
    DeadlineType.TODAY -> DeadlineTypeDto.TODAY
    DeadlineType.TOMORROW -> DeadlineTypeDto.TOMORROW
}
