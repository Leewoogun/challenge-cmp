@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.remote.model.challenge.ActiveChallengeDto
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import com.lwg.challenge.remote.model.challenge.VerificationStatusDto
import kotlin.time.Instant

/**
 * Active challenge DTO → Domain 매핑.
 *
 * 시간 변환: DTO 의 `deadline: String` (ISO-8601 UTC) → Domain 의 `Instant`.
 * 파싱 실패 시 [Instant.DISTANT_PAST] (백엔드 계약상 발생하면 안 되지만 방어적 처리).
 */
fun ActiveChallengeResponse.toActiveChallenges(): List<ActiveChallenge> =
    data.activeChallenges.map(ActiveChallengeDto::toActiveChallenge)

private fun ActiveChallengeDto.toActiveChallenge(): ActiveChallenge = ActiveChallenge(
    challengeId = challengeId,
    myMission = myMission,
    opponentNickname = opponentNickname,
    opponentMission = opponentMission,
    deadline = parseDeadline(deadline),
    myVerificationStatus = myVerificationStatus.toDomain(),
    opponentVerificationStatus = opponentVerificationStatus.toDomain(),
    bet = bet,
)

private fun parseDeadline(value: String): Instant = runCatching {
    Instant.parse(value)
}.getOrDefault(Instant.DISTANT_PAST)

private fun VerificationStatusDto.toDomain(): VerificationStatus = when (this) {
    VerificationStatusDto.PENDING -> VerificationStatus.PENDING
    VerificationStatusDto.VERIFIED -> VerificationStatus.VERIFIED
    VerificationStatusDto.FAILED -> VerificationStatus.FAILED
}
