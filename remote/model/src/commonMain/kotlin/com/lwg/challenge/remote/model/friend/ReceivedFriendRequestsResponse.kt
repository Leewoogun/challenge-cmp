package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
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
 *         "requestedAt": "2026-06-24 18:00:00" }
 *     ]
 *   } }
 * ```
 *
 * `id` 는 `friendships.id` (PENDING). `requestedAt` 은 "yyyy-MM-dd HH:mm:ss" (KST) (friendships.created_at).
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
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val requestedAt: LocalDateTime? = null,
)

@Serializable
data class UserBriefDto(
    val id: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
)
