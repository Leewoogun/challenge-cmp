@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.remote.model.challenge.ChallengeCreateBody
import com.lwg.challenge.remote.model.challenge.DeadlineTypeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse
import kotlin.time.Instant

/**
 * 챌린지 신청 / 응답 DTO ↔ Domain 매핑.
 *
 * 시간 변환: DTO 의 `String` (ISO-8601 UTC) → Domain 의 [Instant].
 * 파싱 실패 시 [Instant.DISTANT_PAST] (백엔드 계약상 발생하면 안 되지만 방어적 처리).
 *
 * `challengeDate` 는 도메인으로 넘기지 않는다 — 카운트다운이 `deadline` 기준이라 UI 가 쓰지 않는다.
 */

fun ReceivedChallengesResponse.toReceivedChallenges(): List<ReceivedChallenge> =
    data.challenges.map(ReceivedChallengeDto::toReceivedChallenge)

fun ReceivedChallengeDto.toReceivedChallenge(): ReceivedChallenge = ReceivedChallenge(
    challengeId = challengeId,
    challengerId = challengerId,
    challengerNickname = challengerNickname,
    challengerProfileImageUrl = challengerProfileImageUrl,
    challengerMission = challengerMission,
    betContent = betContent,
    deadline = parseInstant(deadline),
    createdAt = parseInstant(createdAt),
)

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

private fun parseInstant(value: String): Instant = runCatching {
    Instant.parse(value)
}.getOrDefault(Instant.DISTANT_PAST)
