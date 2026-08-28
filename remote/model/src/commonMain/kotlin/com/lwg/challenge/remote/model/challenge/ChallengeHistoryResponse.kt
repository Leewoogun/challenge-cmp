package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/history 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": { "histories": [
 *     { "challengeId": 42, "challengeDate": "2026-08-14", "opponentNickname": "준혁",
 *       "myMission": "헬스장 가기", "opponentMission": "금주", "bet": "치킨 한 마리",
 *       "myResult": "WIN" }
 *   ] }
 * }
 * ```
 *
 * 종료된 챌린지가 없으면 `histories` 는 빈 배열. 페이지네이션이 없어 전체가 한 번에 온다.
 */
@Serializable
data class ChallengeHistoryResponse(
    val data: ChallengeHistoryListData = ChallengeHistoryListData(),
) : BaseResponse()

@Serializable
data class ChallengeHistoryListData(
    val histories: List<ChallengeHistoryDto> = emptyList(),
)

@Serializable
data class ChallengeHistoryDto(
    val challengeId: Long = 0L,
    /** "yyyy-MM-dd" (KST) (예: "2026-08-14"). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateSerializer::class)
    val challengeDate: LocalDate? = null,
    /** 탈퇴한 상대는 `"탈퇴한 사용자"` 로 온다 — non-null 이 유지되므로 앱은 분기하지 않는다. */
    val opponentNickname: String = "",
    val myMission: String = "",
    val opponentMission: String = "",
    val bet: String = "",
    /**
     * `WIN` / `LOSE` / `DRAW` / `BOTH_LOSE`. **이미 내 시점**으로 뒤집혀 온다 —
     * 역할(challenger/opponent) 기준 원본은 이 응답에 없다.
     *
     * enum 이 아니라 String 인 이유는 [ActiveChallengeDto.myResult] 와 같다 — 서버가 값을 하나
     * 추가하면 typed enum 은 목록 **전체**의 역직렬화를 깨뜨린다. 모르는 값은 매퍼가 흡수한다.
     */
    val myResult: String = "",
)
