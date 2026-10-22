package com.lwg.challenge.remote.model.verification

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/{id}/verifications
 *
 * [ChallengeVerificationsData.me] / [ChallengeVerificationsData.opponent] 는 요청한 사람 시점이다.
 */
@Serializable
data class ChallengeVerificationsResponse(
    val data: ChallengeVerificationsData,
) : BaseResponse()

@Serializable
data class ChallengeVerificationsData(
    val challengeId: Long = 0L,
    val me: PartyVerificationDto,
    val opponent: PartyVerificationDto,
)

@Serializable
data class PartyVerificationDto(
    /** `PENDING` / `VERIFIED` / `FAILED`. */
    val status: String = "",
    val photoUrl: String? = null,
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val verifiedAt: LocalDateTime? = null,
    val photoDeleted: Boolean = false,
    val aiReason: String? = null,
)
