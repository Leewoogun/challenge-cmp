package com.lwg.challenge.designsystem.components.challenge

/**
 * [ChallengeCard] status pill 의 표시 상태.
 *
 * 디자인 시스템 모듈은 :domain:model 에 의존하지 않으므로(Clean Arch 단방향),
 * 도메인의 VerificationStatus 와는 독립된 표시 전용 enum 으로 둔다.
 * 호출부(feature)에서 1:1 매핑 함수로 변환.
 */
enum class ChallengeVerificationStatus {
    PENDING,
    VERIFIED,
    FAILED,
}
