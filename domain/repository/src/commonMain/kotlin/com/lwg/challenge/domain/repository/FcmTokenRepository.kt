package com.lwg.challenge.domain.repository

interface FcmTokenRepository {

    /**
     * 멱등하다. 로그인 직후와 `onNewToken` 양쪽에서 부르므로 같은 토큰 중복 호출이 정상 경로다.
     */
    suspend fun updateFcmToken(
        fcmToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}
