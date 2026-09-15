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
 * challenge-forfeit(2026-09-05) 이후 이 경고는 더 강해졌다 — 역산은 규칙 사본을 만들 뿐 아니라
 * **틀린 답을 낸다.** 내가 challenger 인데 기권하면 상대가 인증하지 않았어도 서버는
 * [ChallengeResult.OPPONENT_WIN] 을 내려준다: 양측 다 미인증인데 [BOTH_LOSE] 가 아니다.
 * *"사본이긴 해도 값은 맞으니 이번만"* 으로 빠져나갈 여지가 사라졌다.
 *
 * 홈 목록도 서버가 이 값을 그대로 내려준다 — `challenge-result` 가 `ActiveChallengeDto.myResult` 로
 * 개통시켰다. 상세와 홈 두 화면이 같은 표현 타입에 모인다.
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
