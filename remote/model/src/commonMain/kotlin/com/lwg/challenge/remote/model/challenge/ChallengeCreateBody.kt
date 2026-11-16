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
    /** "yyyy-MM-dd" */
    val challengeDate: String,
    /** 챌린저 서명. `SignatureCodec.encode` 출력 문자열 그대로다. */
    val signature: String,
)

