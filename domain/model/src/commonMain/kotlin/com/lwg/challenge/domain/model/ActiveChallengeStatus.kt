package com.lwg.challenge.domain.model

/**
 * 홈 목록([ActiveChallenge])에 실릴 수 있는 상태.
 *
 * 챌린지 전체 상태 7종과는 **다른 집합**이다 — 부분집합이라기보다 목록 계약이 따로 좁혀 놓은
 * 축이다. 특히 `EXPIRED` 는 계약상 이 목록에 나오지 않고, 그 밖의 상태도 마찬가지다.
 * 이름이 `ChallengeStatus` 가 아닌 이유가 그것이다 — 전체 상태를 표현하는 타입으로 오해되면
 * 나중에 '왜 여기 만료가 없냐' 는 질문이 매퍼가 아니라 화면에서 터진다.
 */
enum class ActiveChallengeStatus {
    IN_PROGRESS,
    COMPLETED,
}
