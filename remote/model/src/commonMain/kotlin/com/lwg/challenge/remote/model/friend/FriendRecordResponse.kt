package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/** GET /api/v1/friends/{friendId}/record */
@Serializable
data class FriendRecordResponse(
    val data: FriendRecordData = FriendRecordData(),
) : BaseResponse()

@Serializable
data class FriendRecordData(
    val friend: FriendRecordFriendDto = FriendRecordFriendDto(),
    val score: FriendScoreDto = FriendScoreDto(),
    val ongoing: List<FriendOngoingChallengeDto> = emptyList(),
)

/** [UserBriefDto] 와 필드는 같지만 id 키 이름이 `userId` 다. */
@Serializable
data class FriendRecordFriendDto(
    val userId: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
)

@Serializable
data class FriendScoreDto(
    val myWins: Int = 0,
    val friendWins: Int = 0,
    val draws: Int = 0,
    val bothLose: Int = 0,
)

@Serializable
data class FriendOngoingChallengeDto(
    val challengeId: Long = 0L,
    val status: FriendChallengeStatusDto,
    // 키가 빠졌을 때 false 로 흡수되면 받은 도전장의 탭 목적지가 바뀌므로 기본값 없이 역직렬화 실패로 드러낸다
    val sentByMe: Boolean,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
    val myMission: String? = null,
    val friendMission: String? = null,
    val bet: String = "",
)

/** 서버가 이 두 상태만 싣는 닫힌 집합이다. */
@Serializable
enum class FriendChallengeStatusDto {
    PENDING,
    IN_PROGRESS,
}
