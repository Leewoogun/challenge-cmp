package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/friends/requests/{id}/reject 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": { "requestId": 901, "status": "REJECTED" } }
 * ```
 */
@Serializable
data class RejectFriendRequestResponse(
    val data: RejectFriendRequestData = RejectFriendRequestData(),
) : BaseResponse()

@Serializable
data class RejectFriendRequestData(
    val requestId: Long = 0L,
    val status: String = "REJECTED",
)
