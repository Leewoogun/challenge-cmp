package com.lwg.challenge.remote.model.record

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/record 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": { "win": 7, "lose": 3, "draw": 2, "currentStreak": 3 }
 * }
 * ```
 *
 * 신규 사용자도 동일 shape — 모든 값이 0 으로 채워져 응답.
 */
@Serializable
data class RecordResponse(
    val data: RecordData = RecordData(),
) : BaseResponse()

@Serializable
data class RecordData(
    val win: Int = 0,
    val lose: Int = 0,
    val draw: Int = 0,
    val currentStreak: Int = 0,
)
