package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/active 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": { "activeChallenges": [ { "challengeId": 1001, ... }, ... ] }
 * }
 * ```
 *
 * 진행 중 챌린지가 없으면 `activeChallenges` 는 빈 배열.
 */
@Serializable
data class ActiveChallengeResponse(
    val data: ActiveChallengeListData = ActiveChallengeListData(),
) : BaseResponse()

@Serializable
data class ActiveChallengeListData(
    val activeChallenges: List<ActiveChallengeDto> = emptyList(),
)

@Serializable
data class ActiveChallengeDto(
    val challengeId: Long = 0L,
    val myMission: String = "",
    val opponentNickname: String = "",
    val opponentMission: String = "",
    /** ISO-8601 UTC (예: "2026-05-26T00:00:00Z"). 매퍼에서 [kotlinx.datetime.Instant] 로 파싱. */
    val deadline: String = "",
    val myVerificationStatus: VerificationStatusDto = VerificationStatusDto.PENDING,
    val opponentVerificationStatus: VerificationStatusDto = VerificationStatusDto.PENDING,
    val bet: String = "",
)

@Serializable
enum class VerificationStatusDto {
    PENDING,
    VERIFIED,
    FAILED,
}
