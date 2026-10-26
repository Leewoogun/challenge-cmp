package com.lwg.challenge.remote.model.challenge

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireYearMonthSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/challenges/history
 *
 * 한 번에 한 달치만 온다 — 다른 달은 `month` 파라미터를 붙여 다시 요청한다.
 */
@Serializable
data class ChallengeHistoryResponse(
    val data: ChallengeHistoryListData = ChallengeHistoryListData(),
) : BaseResponse()

@Serializable
data class ChallengeHistoryListData(
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
    /** "yyyy-MM-dd" (KST) (예: "2026-08-14"). */
    val challengeDate: LocalDate,
    /** 탈퇴한 상대는 `"탈퇴한 사용자"` 로 온다 — non-null 이 유지되므로 앱은 분기하지 않는다. */
    val opponentNickname: String = "",
    val myMission: String = "",
    val opponentMission: String = "",
    val bet: String = "",
    /** **이미 내 시점**으로 뒤집혀 온다 — 역할(challenger/opponent) 기준 원본은 이 응답에 없다. */
    val myResult: ChallengeOutcomeDto,
)
