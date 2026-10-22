package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateSerializer
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/{id}
 *
 * [ChallengeDetailData.me] / [ChallengeDetailData.opponent] 는 요청한 사람 시점이다.
 */
@Serializable
data class ChallengeDetailResponse(
    val data: ChallengeDetailData,
) : BaseResponse()

@Serializable
data class ChallengeDetailData(
    val challengeId: Long = 0L,
    val status: String = "",
    /** `COMPLETED` 전에는 명시적 null 로 온다. */
    val myResult: ChallengeOutcomeDto? = null,
    /** 기권한 쪽. 기권 없이 끝났거나 진행 중이면 명시적 null 로 온다. */
    val forfeitedBy: ChallengeSideDto? = null,
    /** "yyyy-MM-dd" (예: "2026-08-03"). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateSerializer::class)
    val challengeDate: LocalDate? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
    val betContent: String = "",
    val me: ContractUserInfoDto,
    val opponent: ContractUserInfoDto,
    /** soul-oath 도입 이전에 만들어진 챌린지는 계약서가 없다. */
    val contract: ContractDto? = null,
)

@Serializable
enum class ChallengeSideDto {
    ME,
    OPPONENT,
}

@Serializable
data class ContractUserInfoDto(
    val nickname: String = "",
    /** 카카오 프로필 미설정 시 null. */
    val profileImageUrl: String? = null,
    /** 아직 수락 전이라 미션이 없으면 명시적 null 로 온다. */
    val mission: String? = null,
)

@Serializable
data class ContractDto(
    val content: String = "",
    val isFinalized: Boolean = false,
    /** `SignatureCodec` 저장 포맷 문자열. 미서명이면 null. */
    val mySignature: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 미서명이면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val mySignedAt: LocalDateTime? = null,
    /** `SignatureCodec` 저장 포맷 문자열. 미서명이면 null. */
    val opponentSignature: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 미서명이면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val opponentSignedAt: LocalDateTime? = null,
)
