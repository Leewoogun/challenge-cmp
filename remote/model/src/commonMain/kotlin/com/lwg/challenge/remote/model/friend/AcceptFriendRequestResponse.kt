package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/friends/requests/{id}/accept 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": { "friendshipId": 901, "status": "ACCEPTED" } }
 * ```
 */
@Serializable
data class AcceptFriendRequestResponse(
    val data: AcceptFriendRequestData = AcceptFriendRequestData(),
) : BaseResponse()

@Serializable
data class AcceptFriendRequestData(
    val friendshipId: Long = 0L,
    val status: String = "ACCEPTED",
)
