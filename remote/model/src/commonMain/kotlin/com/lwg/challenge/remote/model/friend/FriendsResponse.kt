package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/friends
 *
 * 정렬된 채로 도착하므로 앱이 재정렬하지 않는다.
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
    /** 친구(상대)의 user id. 챌린지 신청 시 `opponentId` 로 그대로 넘긴다. */
    val id: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST) (예: "2026-06-20 12:34:56"). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val since: LocalDateTime? = null,
)
