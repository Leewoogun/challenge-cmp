package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** GET /api/v1/friends/{friendId}/challenges — `COMPLETED` 만, `id DESC` 키 기반 페이지네이션. */
@Serializable
data class FriendChallengesResponse(
    val data: FriendChallengesData = FriendChallengesData(),
) : BaseResponse()

/**
 * @property nextChallengeId **다음 쪽 첫 항목**의 id(이번 쪽 마지막 항목 id 가 아니다). 다음 요청에 그대로 싣는다
 * @property isPageEnd 기본값이 `true` 인 이유: 키가 빠진 응답에서 `false` 면 null 키로 첫 쪽을 반복 요청한다
 */
@Serializable
data class FriendChallengesData(
    val challenges: List<FriendPastChallengeDto> = emptyList(),
    val nextChallengeId: Long? = null,
    val isPageEnd: Boolean = true,
)

@Serializable
data class FriendPastChallengeDto(
    val challengeId: Long = 0L,
    /** "yyyy-MM-dd" (KST). */
    val challengeDate: LocalDate,
    val myMission: String = "",
    val friendMission: String = "",
    val bet: String = "",
    val myResult: ChallengeOutcomeDto,
)
