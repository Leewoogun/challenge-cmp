package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": {
 *     "challengeId": 7,
 *     "status": "PENDING",
 *     "challengeDate": "2026-07-28",
 *     "deadline": "2026-07-28T15:00:00Z"
 *   } }
 * ```
 */
@Serializable
data class ChallengeCreateResponse(
    val data: ChallengeCreateData = ChallengeCreateData(),
) : BaseResponse()

@Serializable
data class ChallengeCreateData(
    val challengeId: Long = 0L,
    val status: String = "",
    /** 평문 ISO date (예: "2026-07-28"). */
    val challengeDate: String = "",
    /** ISO-8601 UTC. */
    val deadline: String = "",
)
