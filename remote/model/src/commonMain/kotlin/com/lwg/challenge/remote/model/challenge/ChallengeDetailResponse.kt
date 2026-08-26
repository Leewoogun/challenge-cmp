package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateSerializer
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/{id} 성공 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "",
 *   "data": {
 *     "challengeId": 7,
 *     "status": "IN_PROGRESS",
 *     "result": null,
 *     "challengeDate": "2026-08-03",
 *     "deadline": "2026-08-04 00:00:00",
 *     "betContent": "커피 사기",
 *     "challenger": {
 *       "userId": 1,
 *       "nickname": "이우건",
 *       "profileImageUrl": null,
 *       "mission": "운동 1시간" },
 *     "opponent": {
 *       "userId": 14,
 *       "nickname": "테스터1",
 *       "profileImageUrl": null,
 *       "mission": "책 30페이지" },
 *     "contract": {
 *       "content": "...",
 *       "isFinalized": true,
 *       "challengerSignature": "{\"v\":1,\"g\":1000,\"s\":[[123,456]]}",
 *       "challengerSignedAt": "2026-08-03 11:20:00",
 *       "opponentSignature": null,
 *       "opponentSignedAt": null } } }
 * ```
 *
 * `status` 가 `COMPLETED` 면 `result` 에 `"CHALLENGER_WIN"` 같은 판정 결과가 들어온다.
 *
 * `challenger` / `opponent` 는 역할 그대로다 — '나 / 상대' 로 뒤집혀 오지 않는다.
 * `result` 도 같다.
 */
@Serializable
data class ChallengeDetailResponse(
    val data: ChallengeDetailData = ChallengeDetailData(),
) : BaseResponse()

@Serializable
data class ChallengeDetailData(
    val challengeId: Long = 0L,
    val status: String = "",
    /**
     * `CHALLENGER_WIN` / `OPPONENT_WIN` / `DRAW` / `BOTH_LOSE`. `COMPLETED` 전에는 명시적 null 로
     * 온다 (키는 존재) — 기본값은 키 누락만 덮으므로 타입 자체가 nullable 이어야 한다.
     *
     * enum 이 아니라 String 인 이유: `@Serializable` enum 으로 받으면 서버가 결과 종류를 하나
     * 추가하는 순간 응답 **전체**의 역직렬화가 깨져 상세 화면이 통째로 못 뜬다. 모르는 값의
     * 흡수는 매퍼가 한다. `PartyVerificationDto.status` 가 같은 판단의 선례다.
     */
    val result: String? = null,
    /** "yyyy-MM-dd" (예: "2026-08-03"). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateSerializer::class)
    val challengeDate: LocalDate? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
    val betContent: String = "",
    val challenger: ContractPartyDto = ContractPartyDto(),
    val opponent: ContractPartyDto = ContractPartyDto(),
    /**
     * soul-oath 도입 이전에 만들어진 챌린지는 계약서가 없다. 정상 상태이며 계약 §3.1 nullable
     * 표에 등재돼 있다.
     */
    val contract: ContractDto? = null,
)

@Serializable
data class ContractPartyDto(
    val userId: Long = 0L,
    val nickname: String = "",
    /** 카카오 프로필 미설정 시 null. */
    val profileImageUrl: String? = null,
    /**
     * `PENDING` 구간에는 상대 미션이 아직 없어 서버가 명시적 null 을 보낸다.
     * 기본값은 키 누락만 덮으므로 타입 자체가 nullable 이어야 한다.
     */
    val mission: String? = null,
)

@Serializable
data class ContractDto(
    val content: String = "",
    val isFinalized: Boolean = false,
    /** `SignatureCodec` 저장 포맷 문자열. 미서명이면 null. */
    val challengerSignature: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 미서명이면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val challengerSignedAt: LocalDateTime? = null,
    /** `SignatureCodec` 저장 포맷 문자열. 미서명이면 null. */
    val opponentSignature: String? = null,
    /** "yyyy-MM-dd HH:mm:ss" (KST). 미서명이면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val opponentSignedAt: LocalDateTime? = null,
)
