package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/active 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": { "activeChallenges": [
 *     { "challengeId": 1001, "status": "IN_PROGRESS", "myResult": null, ... },
 *     { "challengeId": 1002, "status": "COMPLETED",   "myResult": "WIN",  ... }
 *   ] }
 * }
 * ```
 *
 * 진행 중 챌린지가 없으면 `activeChallenges` 는 빈 배열.
 *
 * 진행 중 + 최근 판정된 건이 한 배열로 온다. 정렬도 서버가 한다
 * (IN_PROGRESS deadline 오름차순 → COMPLETED 판정 시각 내림차순) — 앱은 재정렬하지 않는다.
 */
@Serializable
data class ActiveChallengeResponse(
    val data: ActiveChallengeListData = ActiveChallengeListData(),
) : BaseResponse()

@Serializable
data class ActiveChallengeListData(
    val activeChallenges: List<ActiveChallengeDto> = emptyList(),
)

@Serializable
data class ActiveChallengeDto(
    val challengeId: Long = 0L,
    val myMission: String = "",
    val opponentNickname: String = "",
    val opponentMission: String = "",
    /** "yyyy-MM-dd HH:mm:ss" (KST) (예: "2026-05-26 00:00:00"). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
    val myVerificationStatus: VerificationStatusDto = VerificationStatusDto.PENDING,
    val opponentVerificationStatus: VerificationStatusDto = VerificationStatusDto.PENDING,
    val bet: String = "",
    /**
     * 이 목록에는 `IN_PROGRESS` / `COMPLETED` 만 온다 (`EXPIRED` 는 계약상 제외).
     *
     * enum 이 아니라 String 인 이유: 챌린지 상태는 7종이고 앞으로 늘어날 여지가 커서, typed 로
     * 받으면 서버가 값 하나 추가하는 순간 홈 목록 **전체**의 역직렬화가 깨진다. 모르는 값의
     * 처리는 매퍼가 한다. `PartyVerificationDto.status` 가 같은 판단의 선례다 —
     * 바로 아래 [VerificationStatusDto] 는 그 반대편 사례라 대비해서 보면 된다.
     */
    val status: String = "",
    /**
     * `WIN` / `LOSE` / `DRAW` / `BOTH_LOSE`. **이미 내 시점**으로 뒤집혀 온다 — 역할
     * (challenger/opponent) 기준이 아니고, 같은 DTO 의 `myMission` / `myVerificationStatus` 와
     * 같은 좌표계다. `IN_PROGRESS` 면 명시적 null 로 온다 (키는 존재) — 기본값은 키 누락만
     * 덮으므로 타입 자체가 nullable 이어야 한다.
     *
     * [status] 와 같은 이유로 enum 이 아니라 String 이다.
     */
    val myResult: String? = null,
)

/**
 * `myVerificationStatus` / `opponentVerificationStatus` 전용.
 *
 * 새로 붙은 [ActiveChallengeDto.status] / [ActiveChallengeDto.myResult] 가 String 인 것과 대비된다.
 * typed enum 은 서버가 값을 하나 추가하면 응답 전체의 역직렬화를 깨뜨리므로, 신규 필드는 같은
 * 실수를 반복하지 않으려 String 으로 받고 매퍼가 흡수한다. 이쪽은 backend 에 값 추가 계획을
 * 확인 중이라 아직 그대로 둔다.
 */
@Serializable
enum class VerificationStatusDto {
    PENDING,
    VERIFIED,
    FAILED,
}
