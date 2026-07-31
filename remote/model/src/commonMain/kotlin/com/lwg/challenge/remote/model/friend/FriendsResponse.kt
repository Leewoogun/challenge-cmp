package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/friends 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": {
 *     "friends": [
 *       { "id": 42, "nickname": "민수", "profileImageUrl": "...", "since": "2026-06-20 12:34:56" }
 *     ]
 *   } }
 * ```
 *
 * `id` 는 친구(상대) user id. `since` 는 "yyyy-MM-dd HH:mm:ss" (KST) (friendships.accepted_at).
 * `accepted_at DESC` 정렬. 친구 없으면 빈 배열.
 */
@Serializable
data class FriendsResponse(
    val data: FriendListData = FriendListData(),
) : BaseResponse()

@Serializable
data class FriendListData(
    val friends: List<FriendDto> = emptyList(),
)

@Serializable
data class FriendDto(
    val id: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST) (예: "2026-06-20 12:34:56"). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val since: LocalDateTime? = null,
)
