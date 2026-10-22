package com.lwg.challenge.domain.model.verification

import com.lwg.challenge.domain.model.VerificationStatus
import kotlinx.datetime.LocalDateTime

/** 챌린지 양측 인증 현황. [myVerification] / [opponentVerification] 는 요청한 사람 시점이다. */
data class ChallengeVerifications(
    val challengeId: Long,
    val myVerification: VerificationResult,
    val opponentVerification: VerificationResult,
)

data class VerificationResult(
    val status: VerificationStatus,
    val photoUrl: String?,
    val verifiedAt: LocalDateTime?,
    val photoDeleted: Boolean,
    val aiReason: String?,
)
