package com.lwg.challenge.push

import com.google.firebase.messaging.FirebaseMessaging
import com.lwg.challenge.utils.Logger
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class FcmTokenProviderAndroid : FcmTokenProvider {
    override suspend fun getToken(): String? =
        suspendCancellableCoroutine { continuation ->
            // getToken 은 deprecated 지만 대체품 register() 는 토큰이 아니라 FID 를 쓰므로 서버 발송까지 바뀐다.
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener

                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    Logger.w("FCM 토큰 획득 실패: ${task.exception?.message}")
                    continuation.resume(null)
                }
            }
        }
}
