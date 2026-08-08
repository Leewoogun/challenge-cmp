package com.lwg.challenge.push

/**
 * FCM 등록 토큰 획득 wrapper.
 *
 * 공식 Firebase KMP SDK 가 없어 플랫폼별 네이티브 SDK 를 감싼다 (`KakaoAuthClient` 와 같은 구조).
 */
interface FcmTokenProvider {
    suspend fun getToken(): String?
}
