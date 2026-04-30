package com.lwg.challenge.domain.model

/**
 * 로그인한 사용자의 프로필 (최소 필드).
 *
 * 현재 서버 응답은 userId/isNewUser 만 제공한다.
 * 닉네임/프로필 이미지 등은 추후 /users/me 엔드포인트에서 조회 (별도 feature).
 */
data class UserProfile(
    val userId: Long,
    val isNewUser: Boolean,
)
