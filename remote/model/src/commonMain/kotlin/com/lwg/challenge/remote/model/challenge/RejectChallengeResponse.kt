package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges/{id}/reject 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": { "challengeId": 7, "status": "REJECTED" } }
 * ```
 */
@Serializable
data class RejectChallengeResponse(
    val data: RejectChallengeData = RejectChallengeData(),
) : BaseResponse()

@Serializable
data class RejectChallengeData(
    val challengeId: Long = 0L,
    val status: String = "",
)
