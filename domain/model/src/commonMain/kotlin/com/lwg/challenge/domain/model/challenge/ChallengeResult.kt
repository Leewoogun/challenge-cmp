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
 *
 * [BOTH_LOSE] 를 [LOSE] 로 접지 않는다: 둘 다 못 한 것과 내가 진 것은 사용자에게 다른 사실이다.
 * 접어 버리면 화면이 둘을 다시 갈라내려고 양측 인증 상태로 판정 규칙을 역산하게 되고,
 * 그 순간 서버가 이미 내린 판정이 앱에 두 번째로 심긴다.
 *
 * 홈 목록은 서버가 이 값을 그대로 내려줄 예정이라, 상세와 홈 두 화면이 같은 표현 타입에 모인다.
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
