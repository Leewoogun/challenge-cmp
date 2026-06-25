package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/users/search 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": {
 *     "users": [
 *       { "id": 42, "nickname": "민수", "profileImageUrl": "...", "relation": "REQUEST_SENT", "pendingRequestId": 901 },
 *       ...
 *     ]
 *   }
 * }
 * ```
 *
 * 닉네임 contains 매칭, 본인 제외, ACTIVE 유저만, LIMIT 20.
 * `pendingRequestId` 는 relation 이 `REQUEST_SENT` / `REQUEST_RECEIVED` 일 때만 non-null.
 */
@Serializable
data class UserSearchResponse(
    val data: UserSearchResponseData = UserSearchResponseData(),
) : BaseResponse()

@Serializable
data class UserSearchResponseData(
    val users: List<UserSearchDto> = emptyList(),
)

@Serializable
data class UserSearchDto(
    val id: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    /**
     * 서버 `Relation` enum: `NONE` / `REQUEST_SENT` / `REQUEST_RECEIVED` / `FRIEND` / `REJECTED`.
     * 알 수 없는 값은 매퍼에서 `Relation.NONE` 로 fallback.
     */
    val relation: String = "NONE",
    val pendingRequestId: Long? = null,
)
