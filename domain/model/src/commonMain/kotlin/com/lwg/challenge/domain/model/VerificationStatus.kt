package com.lwg.challenge.domain.model

/**
 * 챌린지 인증 상태.
 *
 * - [PENDING] : 미인증 (기본값)
 * - [VERIFIED] : 인증 완료
 * - [FAILED] : 인증 실패
 */
enum class VerificationStatus {
    PENDING,
    VERIFIED,
    FAILED,
}
