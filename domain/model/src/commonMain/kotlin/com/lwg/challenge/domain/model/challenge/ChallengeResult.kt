package com.lwg.challenge.domain.model.challenge

/**
 * 챌린지 판정 결과.
 *
 * 서버가 준 **역할 그대로**다 — '내가 이겼다 / 졌다' 로 뒤집혀 오지 않는다.
 * 내가 challenger 인지 opponent 인지를 아는 화면 계층이 [outcomeFor] 로 관점을 바꾼다.
 * [ChallengeDetail] 의 `challenger` / `opponent` 와 같은 정책이다.
 */
enum class ChallengeResult {
    CHALLENGER_WIN,
    OPPONENT_WIN,
    DRAW,
    BOTH_LOSE,
}

/**
 * **내 시점**으로 접힌 판정 결과.
 */
enum class ChallengeOutcome {
    WIN,
    LOSE,
    DRAW,
    BOTH_LOSE,
}

/**
 * 역할 기준 결과를 내 시점으로 뒤집는다.
 *
 * @param isMeOpponent 내가 opponent 쪽이면 true. challenger 면 false
 */
fun ChallengeResult.outcomeFor(isMeOpponent: Boolean): ChallengeOutcome = when (this) {
    ChallengeResult.CHALLENGER_WIN -> if (isMeOpponent) ChallengeOutcome.LOSE else ChallengeOutcome.WIN
    ChallengeResult.OPPONENT_WIN -> if (isMeOpponent) ChallengeOutcome.WIN else ChallengeOutcome.LOSE
    ChallengeResult.DRAW -> ChallengeOutcome.DRAW
    ChallengeResult.BOTH_LOSE -> ChallengeOutcome.BOTH_LOSE
}
