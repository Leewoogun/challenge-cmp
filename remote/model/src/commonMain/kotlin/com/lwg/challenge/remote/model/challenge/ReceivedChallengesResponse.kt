package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateSerializer
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
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
 *         "deadline": "2026-07-28 15:00:00",
 *         "createdAt": "2026-07-28 02:11:00" }
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
    /** "yyyy-MM-dd" (예: "2026-07-28"). 매핑하지 않고 버린다. */
    @Serializable(with = WireLocalDateSerializer::class)
    val challengeDate: LocalDate? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,
)
