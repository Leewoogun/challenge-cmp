package com.lwg.challenge.remote.model.challenge

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges 요청 body.
 */
@Serializable
data class ChallengeCreateBody(
    val opponentId: Long,
    val myMission: String,
    val betContent: String,
    val deadlineType: DeadlineTypeDto,
    /** 챌린저 서명. `SignatureCodec.encode` 출력 문자열 그대로다. */
    val signature: String,
)

/** 마감 기준. 실제 마감 시각 환산은 서버(KST 고정)가 한다. */
@Serializable
enum class DeadlineTypeDto {
    TODAY,
    TOMORROW,
}
