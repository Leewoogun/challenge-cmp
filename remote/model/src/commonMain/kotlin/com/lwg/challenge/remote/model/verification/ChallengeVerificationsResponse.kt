package com.lwg.challenge.remote.model.verification

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/{id}/verifications 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": {
 *     "challengeId": 7,
 *     "challenger": {
 *       "userId": 1, "status": "VERIFIED",
 *       "photoUrl": "/api/v1/challenges/7/photos/challenger",
 *       "verifiedAt": "2026-08-14 15:32:10" },
 *     "opponent": {
 *       "userId": 14, "status": "PENDING",
 *       "photoUrl": null, "verifiedAt": null } } }
 * ```
 *
 * `challenger` / `opponent` 는 역할 그대로다 — '나 / 상대' 로 뒤집혀 오지 않는다.
 */
@Serializable
data class ChallengeVerificationsResponse(
    val data: ChallengeVerificationsData = ChallengeVerificationsData(),
) : BaseResponse()

@Serializable
data class ChallengeVerificationsData(
    val challengeId: Long = 0L,
    val challenger: PartyVerificationDto = PartyVerificationDto(),
    val opponent: PartyVerificationDto = PartyVerificationDto(),
)

@Serializable
data class PartyVerificationDto(
    val userId: Long = 0L,
    /** `PENDING` / `VERIFIED` / `FAILED`. */
    val status: String = "",
    /**
     * 미인증이면 명시적 null 로 온다 (키는 존재). 기본값은 키 누락만 덮으므로
     * 타입 자체가 nullable 이어야 한다. 아래 시각 필드도 같다.
     */
    val photoUrl: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 미인증이면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val verifiedAt: LocalDateTime? = null,
)
