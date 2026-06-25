package com.lwg.challenge.remote.model.friend

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/friends/requests 요청 body.
 *
 * ```
 * { "receiverId": 42 }
 * ```
 */
@Serializable
data class SendFriendRequestBody(
    val receiverId: Long,
)
