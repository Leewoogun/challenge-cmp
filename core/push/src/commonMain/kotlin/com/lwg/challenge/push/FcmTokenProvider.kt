package com.lwg.challenge.push

/**
 * FCM 등록 토큰 획득 wrapper.
 *
 * 공식 Firebase KMP SDK 가 없어 플랫폼별 네이티브 SDK 를 감싼다 (`KakaoAuthClient` 와 같은 구조).
 * 획득 실패·미지원 플랫폼은 null 을 돌려준다 — 푸시는 부가 기능이라 앱 흐름을 막으면 안 된다.
 */
interface FcmTokenProvider {
    suspend fun getToken(): String?
}
