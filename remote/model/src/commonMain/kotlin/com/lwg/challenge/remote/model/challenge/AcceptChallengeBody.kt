package com.lwg.challenge.remote.model.challenge

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges/{id}/accept 요청 body.
 *
 * ```
 * { "myMission": "책 30페이지 읽기" }
 * ```
 */
@Serializable
data class AcceptChallengeBody(
    val myMission: String,
)
