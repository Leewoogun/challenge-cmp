package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryDto
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryResponse

/** 서버가 준 순서(`challengeDate DESC`)를 그대로 보존한다 — 정렬은 서버 책임이다. */
fun ChallengeHistoryResponse.toChallengeArchive(): ChallengeArchive = ChallengeArchive(
    firstArchivedMonth = data.firstArchivedMonth,
    histories = data.histories.map(ChallengeHistoryDto::toChallengeHistory),
)

private fun ChallengeHistoryDto.toChallengeHistory(): ChallengeHistory = ChallengeHistory(
    challengeId = challengeId,
    challengeDate = challengeDate,
    opponentNickname = opponentNickname,
    myMission = myMission,
    opponentMission = opponentMission,
    bet = bet,
    myResult = myResult.toDomain(),
)
