package com.lwg.challenge.domain.model

/**
 * 로그인한 사용자의 프로필 (최소 필드).
 */
data class UserProfile(
    val userId: Long,
    val isNewUser: Boolean,
)
