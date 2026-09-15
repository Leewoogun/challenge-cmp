package com.lwg.challenge.push

import com.lwg.challenge.navigation.Route

/**
 * 이벤트 → 목적지. `else ->` 를 쓰면 새 타입에서 목적지 지정을 강제하지 못한다.
 *
 * 푸시 수신과 알림 목록이 이 한 벌을 공유한다 — 같은 알림을 어디서 탭하든 같은 화면에 닿아야 한다.
 */
fun PushEvent.toRoute(): Route = when (this) {
    // 수락/거절 액션이 홈 '받은 도전장' 섹션에만 있다.
    is PushEvent.ChallengeRequest -> Route.Home
    is PushEvent.ChallengeAccepted -> Route.Challenge.Detail(challengeId)
    // 거절된 챌린지는 계약서가 없어 상세에 볼 것이 없다.
    is PushEvent.ChallengeRejected -> Route.Home
    // 거절과 반대로 볼 것(상대 인증 사진)이 생겼고, 그 사진은 상세에서만 보인다. 홈 카드는 상태 뱃지뿐.
    is PushEvent.OpponentVerified -> Route.Challenge.Detail(challengeId)
    // 트레이 탭은 상세로 보내지만 **알림 목록에서는 이동하지 않는다** — 두 경로가 다른 것이 의도다.
    // 목록 게이트는 NotificationState 의 isTappableInList 가 따로 판정한다.
    is PushEvent.ChallengeForfeited -> Route.Challenge.Detail(challengeId)
}
