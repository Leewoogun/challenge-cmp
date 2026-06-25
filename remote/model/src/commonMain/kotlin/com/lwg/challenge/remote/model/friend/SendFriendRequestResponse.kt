package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/friends/requests 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": { "requestId": 901, "status": "PENDING" } }
 * ```
 *
 * `status` 는 항상 `"PENDING"`. REJECTED row UPDATE 케이스도 동일 응답.
 */
@Serializable
data class SendFriendRequestResponse(
    val data: SendFriendRequestData = SendFriendRequestData(),
) : BaseResponse()

@Serializable
data class SendFriendRequestData(
    val requestId: Long = 0L,
    val status: String = "PENDING",
)
