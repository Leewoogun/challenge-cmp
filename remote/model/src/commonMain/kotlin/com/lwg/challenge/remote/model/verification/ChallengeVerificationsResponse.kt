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
 *       "verifiedAt": "2026-08-14 15:32:10", "photoDeleted": false, "aiReason": null },
 *     "opponent": {
 *       "userId": 14, "status": "PENDING",
 *       "photoUrl": null, "verifiedAt": null, "photoDeleted": false, "aiReason": null } } }
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
     * 제출한 적이 없으면 명시적 null 로 온다 (키는 존재). 기본값은 키 누락만 덮으므로
     * 타입 자체가 nullable 이어야 한다. 아래 시각 필드도 같다.
     *
     * 미인증과 같은 말이 아니다 — AI 가 반려한 칸은 [status] 가 `FAILED` 인데도 사용자가 올린
     * 사진이 그대로 실려 온다(계약 §4 필드 표).
     */
    val photoUrl: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). **제출** 확정 시각이라 제출한 적이 없어야 null 이다(계약 §4 Q&A 표). */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val verifiedAt: LocalDateTime? = null,
    /**
     * 사진이 지워졌고 되살아나지 않음. 기본값이 반드시 있어야 한다 — 이 키를 아직 주지 않는
     * 서버에서는 키 누락이 곧 `MissingFieldException` 이라 응답 전체가 통째로 실패한다.
     */
    val photoDeleted: Boolean = false,
    /**
     * AI 가 인증 사진을 판정하며 남긴 사유. 판정이 없거나 사유를 싣지 않으면 명시적 null 로 온다
     * (키는 존재). 기본값은 [photoDeleted] 와 같은 이유로 둔다 — 이 키를 아직 주지 않는 서버에서
     * 키 누락이 `MissingFieldException` 이 되면 인증 현황 조회가 통째로 죽는다.
     *
     * 여기에 값이 실려 왔다는 것 자체가 **그 인증이 AI 판별로 미인증 처리됐다**는 뜻이다(API 계약 §4.1
     * 보장). 통과·검증 불가·호출 실패 건의 사유는 서버가 컬럼에만 두고 이 자리에는 싣지 않는다.
     */
    val aiReason: String? = null,
)
