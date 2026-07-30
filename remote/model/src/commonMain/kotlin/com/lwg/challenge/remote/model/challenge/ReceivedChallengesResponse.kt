package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/received 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": {
 *     "challenges": [
 *       { "challengeId": 7,
 *         "challengerId": 42,
 *         "challengerNickname": "민수",
 *         "challengerProfileImageUrl": "https://.../profile.jpg",
 *         "challengerMission": "오늘 운동 1시간 하기",
 *         "betContent": "커피 사기",
 *         "challengeDate": "2026-07-28",
 *         "deadline": "2026-07-28T15:00:00Z",
 *         "createdAt": "2026-07-28T02:11:00Z" }
 *     ]
 *   } }
 * ```
 *
 * `created_at DESC` 정렬. 받은 도전장이 없으면 빈 배열.
 */
@Serializable
data class ReceivedChallengesResponse(
    val data: ReceivedChallengesData = ReceivedChallengesData(),
) : BaseResponse()

@Serializable
data class ReceivedChallengesData(
    val challenges: List<ReceivedChallengeDto> = emptyList(),
)

@Serializable
data class ReceivedChallengeDto(
    val challengeId: Long = 0L,
    val challengerId: Long = 0L,
    val challengerNickname: String = "",
    /** 카카오 프로필 미설정 시 null. */
    val challengerProfileImageUrl: String? = null,
    val challengerMission: String = "",
    val betContent: String = "",
    /** 평문 ISO date (예: "2026-07-28"). 매핑하지 않고 버린다. */
    val challengeDate: String = "",
    /** ISO-8601 UTC. 매퍼에서 [kotlin.time.Instant] 로 파싱. */
    val deadline: String = "",
    /** ISO-8601 UTC. 매퍼에서 [kotlin.time.Instant] 로 파싱. */
    val createdAt: String = "",
)
