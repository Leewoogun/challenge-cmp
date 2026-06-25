package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/friends/requests/received 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": {
 *     "requests": [
 *       { "id": 901,
 *         "fromUser": { "id": 42, "nickname": "민수", "profileImageUrl": "..." },
 *         "requestedAt": "2026-06-24T18:00:00Z" }
 *     ]
 *   } }
 * ```
 *
 * `id` 는 `friendships.id` (PENDING). `requestedAt` 은 ISO-8601 UTC (friendships.created_at).
 * `created_at DESC` 정렬. 받은 요청 없으면 빈 배열.
 */
@Serializable
data class ReceivedFriendRequestsResponse(
    val data: ReceivedRequestListData = ReceivedRequestListData(),
) : BaseResponse()

@Serializable
data class ReceivedRequestListData(
    val requests: List<FriendRequestDto> = emptyList(),
)

@Serializable
data class FriendRequestDto(
    val id: Long = 0L,
    val fromUser: UserBriefDto = UserBriefDto(),
    /** ISO-8601 UTC. 매퍼에서 [kotlin.time.Instant] 로 파싱. */
    val requestedAt: String = "",
)

@Serializable
data class UserBriefDto(
    val id: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
)
