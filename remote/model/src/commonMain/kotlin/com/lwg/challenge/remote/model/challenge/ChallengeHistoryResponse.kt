package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateSerializer
import com.lwg.challenge.remote.model.serializer.WireYearMonthSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/history 성공 응답.
 *
 * ```
 * {
 *   "error": false, "code": 200, "message": "",
 *   "data": {
 *     "month": "2026-08",
 *     "firstArchivedMonth": "2026-03",
 *     "histories": [
 *       { "challengeId": 42, "challengeDate": "2026-08-14", "opponentNickname": "준혁",
 *         "myMission": "헬스장 가기", "opponentMission": "금주", "bet": "치킨 한 마리",
 *         "myResult": "WIN" }
 *     ]
 *   }
 * }
 * ```
 *
 * 한 번에 한 달치만 온다 — `histories` 는 `month` 한 달의 종료된 챌린지 전체이고, 그 달에 기록이
 * 없으면 빈 배열이다. 다른 달은 `month` 파라미터를 붙여 다시 요청한다.
 */
@Serializable
data class ChallengeHistoryResponse(
    val data: ChallengeHistoryListData = ChallengeHistoryListData(),
) : BaseResponse()

@Serializable
data class ChallengeHistoryListData(
    /**
     * 서버가 실제로 조회한 달의 에코. 계약상 non-null 인데 여기서만 nullable 인 이유는, 파싱 실패나
     * 필드 누락을 **응답 전체를 깨뜨리지 않고** 이 필드 하나로 흡수하기 위해서다. 그래도 값을 못 읽으면
     * 매퍼가 응답 전체를 버린다 — 어느 달의 응답인지 모르는 채로는 아무것도 그릴 수 없다.
     */
    @Serializable(with = WireYearMonthSerializer::class)
    val month: YearMonth? = null,
    /**
     * 가장 오래된 종료 챌린지의 달. **`null` 은 결측이 아니라 "종료한 챌린지가 하나도 없다"** 는 뜻이다 —
     * 이 값 하나가 (a) 이전 달로 이동할 수 있는지와 (b) *"이 달만 비었다"* vs *"기록이 아예 없다"* 를
     * 둘 다 결정한다.
     *
     * 사용자 전역 값이라 요청한 달과 무관하게 매 응답 같은 값이 온다.
     */
    @Serializable(with = WireYearMonthSerializer::class)
    val firstArchivedMonth: YearMonth? = null,
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
