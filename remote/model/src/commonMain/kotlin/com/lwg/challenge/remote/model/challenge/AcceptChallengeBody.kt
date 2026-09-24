package com.lwg.challenge.remote.model.challenge

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges/{id}/accept 요청 body.
 */
@Serializable
data class AcceptChallengeBody(
    val myMission: String,
    /** 수락하는 상대의 서명. `SignatureCodec.encode` 출력 문자열 그대로다. */
    val signature: String,
)
