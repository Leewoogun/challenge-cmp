package com.lwg.challenge.remote.model.friend

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/friends/requests 요청 body.
 */
@Serializable
data class SendFriendRequestBody(
    val receiverId: Long,
)
