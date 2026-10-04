package com.lwg.challenge.domain.model

/**
 * 홈 목록([ActiveChallenge])에 실릴 수 있는 상태.
 * @property IN_PROGRESS: 진행 중
 * @property COMPLETED: 완료
 */
enum class ActiveChallengeStatus {
    IN_PROGRESS,
    COMPLETED,
}
