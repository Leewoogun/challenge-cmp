package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateSerializer
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges 성공 응답.
 */
@Serializable
data class ChallengeCreateResponse(
    val data: ChallengeCreateData = ChallengeCreateData(),
) : BaseResponse()

@Serializable
data class ChallengeCreateData(
    val challengeId: Long = 0L,
    val status: String = "",
    /** "yyyy-MM-dd" (예: "2026-07-28"). */
    @Serializable(with = WireLocalDateSerializer::class)
    val challengeDate: LocalDate? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
)
