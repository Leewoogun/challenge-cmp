package com.lwg.challenge.remote.model.challenge

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges/{id}/accept 요청 body.
 *
 * ```
 * { "myMission": "책 30페이지 읽기",
 *   "signature": "{\"v\":1,\"g\":1000,\"s\":[[123,456,130,460]]}" }
 * ```
 */
@Serializable
data class AcceptChallengeBody(
    val myMission: String,
    /** 수락하는 상대의 서명. `SignatureCodec.encode` 출력 문자열 그대로다. */
    val signature: String,
)
