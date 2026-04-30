package com.lwg.challenge.domain.model

/**
 * 카카오 로그인 성공 결과.
 *
 * [UserProfile] 의 isNewUser 값에 따라 온보딩 또는 홈으로 분기.
 */
data class LoginResult(
    val tokens: AuthTokens,
    val userProfile: UserProfile,
)
