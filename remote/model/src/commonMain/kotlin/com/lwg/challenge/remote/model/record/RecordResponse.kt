package com.lwg.challenge.remote.model.record

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/record
 *
 * 신규 사용자도 같은 shape 이라 빈 상태를 따로 분기할 필요가 없다 — 모든 값이 0 으로 온다.
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
