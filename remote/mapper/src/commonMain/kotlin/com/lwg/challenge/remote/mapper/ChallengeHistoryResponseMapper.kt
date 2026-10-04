package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryDto
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryResponse

/**
 * 보관함 DTO → Domain 매핑.
 *
 * 서버가 준 순서(`challengeDate DESC`)를 그대로 보존한다 — 정렬은 서버 책임이다.
 *
 * `month` 를 못 읽으면 항목을 거르는 게 아니라 **응답 전체를 버리고 null 을 반환**한다. 그 값이 없으면
 * 이게 어느 달의 응답인지 알 수 없다 — 화면 상단에 표시할 달도, stale 응답인지 판별할 기준도, ±1 을 셀
 * 원점도 전부 사라진다. 항목 몇 개를 버리는 것과 급이 다르다.
 */
fun ChallengeHistoryResponse.toChallengeArchive(): MappedArchive? {
    val month = data.month ?: return null
    val mapped = data.histories.map(ChallengeHistoryDto::toChallengeHistory)
    return MappedArchive(
        archive = ChallengeArchive(
            month = month,
            firstArchivedMonth = data.firstArchivedMonth,
            histories = mapped.filterNotNull(),
        ),
        droppedCount = mapped.count { it == null },
    )
}

/**
 * `challengeDate` 나 `myResult` 를 못 읽으면 항목을 통째로 드롭한다.
 *
 * 홈 목록이 모르는 `myResult` 를 null 로 흡수하고 카드는 살리는 것과 반대 판단이다. 거기서는 결과를
 * 몰라도 진행 중 카드가 성립하지만, 보관함은 **끝난 챌린지만** 담아 결과 pill 이 카드의 요지이고
 * 날짜는 월 그룹 키라 둘 중 하나가 없으면 그릴 자리 자체가 정해지지 않는다.
 */
private fun ChallengeHistoryDto.toChallengeHistory(): ChallengeHistory? {
    val date = challengeDate ?: return null
    val outcome = myResult.toChallengeOutcomeOrNull() ?: return null
    return ChallengeHistory(
        challengeId = challengeId,
        challengeDate = date,
        opponentNickname = opponentNickname,
        myMission = myMission,
        opponentMission = opponentMission,
        bet = bet,
        myResult = outcome,
    )
}

private fun String?.toChallengeOutcomeOrNull(): ChallengeOutcome? =
    this?.let { raw -> ChallengeOutcome.entries.find { it.name == raw } }
