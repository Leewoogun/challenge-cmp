package com.lwg.challenge.push

import com.google.firebase.messaging.FirebaseMessaging
import com.lwg.challenge.utils.Logger
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class FcmTokenProviderAndroid : FcmTokenProvider {

    // FirebaseMessaging.token 은 Play Services Task 다. kotlinx-coroutines-play-services 를 새로
    // 들이지 않으려고 KakaoAuthClientAndroid 와 같은 방식으로 콜백을 직접 감싼다.
    override suspend fun getToken(): String? =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener

                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    // Play Services 미탑재 기기 등에서 실패할 수 있다. 던지면 앱 흐름을 막으므로 삼킨다.
                    Logger.w("FCM 토큰 획득 실패: ${task.exception?.message}")
                    continuation.resume(null)
                }
            }
        }
}
