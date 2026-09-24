package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/** GET /api/v1/users/search */
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
    /** 수락·취소 호출의 `{id}`. [relation] 이 `REQUEST_SENT` / `REQUEST_RECEIVED` 일 때만 non-null. */
    val pendingRequestId: Long? = null,
)
