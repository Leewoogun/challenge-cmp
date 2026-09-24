package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * DELETE /api/v1/friends/requests/{id} 성공 응답.
 *
 * PENDING row 물리 삭제. 재요청 가능.
 */
@Serializable
data class CancelFriendRequestResponse(
    val data: CancelFriendRequestData = CancelFriendRequestData(),
) : BaseResponse()

@Serializable
data class CancelFriendRequestData(
    val requestId: Long = 0L,
)
