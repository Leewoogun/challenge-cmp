package com.lwg.challenge.remote.model.auth

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/auth/kakao 요청 body.
 *
 * @param kakaoAccessToken Kakao SDK 로 획득한 access token. 공백/누락 시 서버 code=700.
 */
@Serializable
data class KakaoLoginRequest(
    val kakaoAccessToken: String,
)
