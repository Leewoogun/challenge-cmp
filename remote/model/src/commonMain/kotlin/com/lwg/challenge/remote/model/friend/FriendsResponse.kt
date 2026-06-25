package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/friends 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": {
 *     "friends": [
 *       { "id": 42, "nickname": "민수", "profileImageUrl": "...", "since": "2026-06-20T12:34:56Z" }
 *     ]
 *   } }
 * ```
 *
 * `id` 는 친구(상대) user id. `since` 는 ISO-8601 UTC (friendships.accepted_at).
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
    /** ISO-8601 UTC (예: "2026-06-20T12:34:56Z"). 매퍼에서 [kotlin.time.Instant] 로 파싱. */
    val since: String = "",
)
