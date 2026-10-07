package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/active
 *
 * 이름과 달리 `COMPLETED` 항목도 섞여 온다 — 홈의 "최근 결과" 카드가 이 목록을 같이 쓰기
 * 때문이다. 정렬도 서버가 하므로 앱은 재정렬하지 않는다.
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
    /** "yyyy-MM-dd HH:mm:ss" (KST) (예: "2026-05-26 00:00:00"). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
    val bet: String = "",
    /** 이 목록에는 `IN_PROGRESS` / `COMPLETED` 만 온다. */
    val status: ActiveChallengeStatusDto,
    /** 내 시점 판정 결과. `IN_PROGRESS` 면 null. */
    val myResult: ChallengeOutcomeDto? = null,
    val me: ActiveChallengeMeDto,
    val opponent: ActiveChallengeOpponentDto,
)

@Serializable
data class ActiveChallengeMeDto(
    val mission: String = "",
    val verificationStatus: VerificationStatusDto = VerificationStatusDto.PENDING,
)

@Serializable
data class ActiveChallengeOpponentDto(
    val nickname: String = "",
    val mission: String = "",
    val verificationStatus: VerificationStatusDto = VerificationStatusDto.PENDING,
)

@Serializable
enum class VerificationStatusDto {
    PENDING,
    VERIFIED,
    FAILED,
}

@Serializable
enum class ActiveChallengeStatusDto {
    IN_PROGRESS,
    COMPLETED,
}

@Serializable
enum class ChallengeOutcomeDto {
    WIN,
    LOSE,
    DRAW,
    BOTH_LOSE,
}
