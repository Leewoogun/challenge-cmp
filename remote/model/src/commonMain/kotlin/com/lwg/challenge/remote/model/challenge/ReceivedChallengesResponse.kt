package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateSerializer
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/received
 *
 * 정렬된 채로 도착하므로 앱이 재정렬하지 않는다.
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
    val opponent: ReceivedChallengeOpponentDto,
)

@Serializable
data class ReceivedChallengeOpponentDto(
    val id: Long = 0L,
    val nickname: String = "",
    /** 카카오 프로필 미설정 시 null. */
    val profileImageUrl: String? = null,
    val mission: String = "",
)
