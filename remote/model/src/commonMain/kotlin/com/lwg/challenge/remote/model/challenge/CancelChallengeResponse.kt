package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * DELETE /api/v1/challenges/{id} 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": { "challengeId": 7 } }
 * ```
 *
 * PENDING row 물리 삭제.
 */
@Serializable
data class CancelChallengeResponse(
    val data: CancelChallengeData = CancelChallengeData(),
) : BaseResponse()

@Serializable
data class CancelChallengeData(
    val challengeId: Long = 0L,
)
