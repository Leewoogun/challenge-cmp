package com.lwg.challenge.feature.login.kakao

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class KakaoAuthClientIos : KakaoAuthClient {

    override suspend fun requestAccessToken(): KakaoAuthResult =
        suspendCancellableCoroutine { continuation ->
            val handler = KakaoLoginBridge.loginHandler
            if (handler == null) {
                continuation.resume(
                    KakaoAuthResult.Failure("iOS 카카오 로그인이 초기화되지 않았습니다."),
                )
                return@suspendCancellableCoroutine
            }

            handler(
                { accessToken ->
                    if (continuation.isActive) {
                        continuation.resume(KakaoAuthResult.Success(accessToken))
                    }
                },
                { message ->
                    if (continuation.isActive) {
                        continuation.resume(KakaoAuthResult.Failure(message))
                    }
                },
                {
                    if (continuation.isActive) {
                        continuation.resume(KakaoAuthResult.Cancelled)
                    }
                },
            )
        }
}
