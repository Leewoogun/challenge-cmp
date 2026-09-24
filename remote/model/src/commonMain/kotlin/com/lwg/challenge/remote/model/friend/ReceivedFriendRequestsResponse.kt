package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/friends/requests/received
 *
 * 정렬된 채로 도착하므로 앱이 재정렬하지 않는다.
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
    /** requestId — 수락·거절 호출의 `{id}` 다. 보낸 사람의 userId 가 아니다. */
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
