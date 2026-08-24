package com.lwg.challenge.remote.model.verification

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * POST /api/v1/challenges/{id}/verification 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": { "challengeId": 7, "status": "VERIFIED", "verifiedAt": "2026-08-14 15:32:10" } }
 * ```
 */
@Serializable
data class SubmitVerificationResponse(
    val data: SubmitVerificationData = SubmitVerificationData(),
) : BaseResponse()

@Serializable
data class SubmitVerificationData(
    val challengeId: Long = 0L,
    /** `PENDING` / `VERIFIED` / `FAILED`. */
    val status: String = "",
    /**
     * "yyyy-MM-dd HH:mm:ss" (KST). 상류 도메인 타입이 nullable 이라 명시적 null 이 올 수 있다 —
     * 기본값은 키 누락만 덮으므로 타입 자체가 nullable 이어야 한다.
     */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val verifiedAt: LocalDateTime? = null,
)
