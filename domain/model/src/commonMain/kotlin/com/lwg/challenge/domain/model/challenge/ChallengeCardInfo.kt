package com.lwg.challenge.domain.model.challenge

import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.VerificationStatus
import kotlinx.datetime.LocalDate

/**
 * 홈·보관함이 함께 그리는 챌린지 카드 1장.
 *
 * @property penalty 진 쪽이 해야 하는 벌칙 (예: "커피 사기 ☕")
 * @property outcome 판정 결과. 진행 중이면 null.
 * @property challengeDate 챌린지 날짜. 보관함 카드에만 있다.
 * @property myVerification 내 인증 상태. 홈 카드에만 있다.
 * @property opponentVerification 상대 인증 상태. 홈 카드에만 있다.
 */
data class ChallengeCardInfo(
    val opponentNickname: String,
    val myMission: String,
    val opponentMission: String,
    val penalty: String,
    val outcome: ChallengeOutcome?,
    val challengeDate: LocalDate?,
    val myVerification: VerificationStatus?,
    val opponentVerification: VerificationStatus?,
) {

    constructor(challenge: ActiveChallenge) : this(
        opponentNickname = challenge.opponent.nickname,
        myMission = challenge.me.mission,
        opponentMission = challenge.opponent.mission,
        penalty = challenge.penalty,
        outcome = challenge.outcome,
        challengeDate = null,
        myVerification = challenge.me.verificationStatus,
        opponentVerification = challenge.opponent.verificationStatus,
    )

    constructor(history: ChallengeHistory) : this(
        opponentNickname = history.opponentNickname,
        myMission = history.myMission,
        opponentMission = history.opponentMission,
        penalty = history.bet,
        outcome = history.myResult,
        challengeDate = history.challengeDate,
        myVerification = null,
        opponentVerification = null,
    )
}
