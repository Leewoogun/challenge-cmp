package com.lwg.challenge.push

/**
 * iOS no-op 스텁.
 *
 * FCM iOS 토큰 발급은 APNs 인증키가 선행돼야 하고 그 발급이 유료 Apple Developer Program 전용이라
 * 이번 범위 밖이다. null 을 돌려주면 등록 단계가 건너뛰어져 앱 흐름에는 영향이 없다.
 */
internal class FcmTokenProviderIos : FcmTokenProvider {

    override suspend fun getToken(): String? = null
}
