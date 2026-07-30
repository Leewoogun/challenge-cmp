package com.lwg.challenge.remote.model.challenge

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges 요청 body.
 *
 * ```
 * { "opponentId": 42,
 *   "myMission": "오늘 운동 1시간 하기",
 *   "betContent": "커피 사기",
 *   "deadlineType": "TODAY" }
 * ```
 */
@Serializable
data class ChallengeCreateBody(
    val opponentId: Long,
    val myMission: String,
    val betContent: String,
    val deadlineType: DeadlineTypeDto,
)

/** 마감 기준. 실제 마감 시각 환산은 서버(KST 고정)가 한다. */
@Serializable
enum class DeadlineTypeDto {
    TODAY,
    TOMORROW,
}
