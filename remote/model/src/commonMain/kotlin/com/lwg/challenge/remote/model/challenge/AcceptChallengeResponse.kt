package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges/{id}/accept 성공 응답.
 */
@Serializable
data class AcceptChallengeResponse(
    val data: AcceptChallengeData = AcceptChallengeData(),
) : BaseResponse()

@Serializable
data class AcceptChallengeData(
    val challengeId: Long = 0L,
    val status: String = "",
)
