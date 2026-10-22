package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.MyChallengeInfo
import com.lwg.challenge.domain.model.OpponentChallengeInfo
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.remote.model.challenge.ActiveChallengeDto
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import com.lwg.challenge.remote.model.challenge.ActiveChallengeStatusDto
import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import com.lwg.challenge.remote.model.challenge.VerificationStatusDto

/**
 * Active challenge DTO → Domain 매핑. 서버가 준 순서를 그대로 보존한다.
 *
 * `deadline` 을 못 읽은 항목은 카드를 그릴 수 없어 제외한다.
 */
fun ActiveChallengeResponse.toActiveChallenges(): List<ActiveChallenge> =
    data.activeChallenges.mapNotNull(ActiveChallengeDto::toActiveChallenge)

private fun ActiveChallengeDto.toActiveChallenge(): ActiveChallenge? {
    val deadline = deadline ?: return null
    return ActiveChallenge(
        challengeId = challengeId,
        me = MyChallengeInfo(
            mission = me.mission,
            verificationStatus = me.verificationStatus.toDomain(),
        ),
        opponent = OpponentChallengeInfo(
            nickname = opponent.nickname,
            mission = opponent.mission,
            verificationStatus = opponent.verificationStatus.toDomain(),
        ),
        deadline = deadline,
        penalty = bet,
        status = status.toDomain(),
        outcome = myResult?.toDomain(),
    )
}

private fun ActiveChallengeStatusDto.toDomain(): ActiveChallengeStatus = when (this) {
    ActiveChallengeStatusDto.IN_PROGRESS -> ActiveChallengeStatus.IN_PROGRESS
    ActiveChallengeStatusDto.COMPLETED -> ActiveChallengeStatus.COMPLETED
}

internal fun ChallengeOutcomeDto.toDomain(): ChallengeOutcome = when (this) {
    ChallengeOutcomeDto.WIN -> ChallengeOutcome.WIN
    ChallengeOutcomeDto.LOSE -> ChallengeOutcome.LOSE
    ChallengeOutcomeDto.DRAW -> ChallengeOutcome.DRAW
    ChallengeOutcomeDto.BOTH_LOSE -> ChallengeOutcome.BOTH_LOSE
}

private fun VerificationStatusDto.toDomain(): VerificationStatus = when (this) {
    VerificationStatusDto.PENDING -> VerificationStatus.PENDING
    VerificationStatusDto.VERIFIED -> VerificationStatus.VERIFIED
    VerificationStatusDto.FAILED -> VerificationStatus.FAILED
}
