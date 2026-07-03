package com.lwg.challenge.core.invite

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class IosKakaoInviter : KakaoInviter {

    override suspend fun sendInvite(inviterNickname: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val handler = KakaoInviteBridge.inviteHandler
            if (handler == null) {
                cont.resume(Result.failure(IllegalStateException("iOS 카카오 공유가 초기화되지 않았습니다.")))
                return@suspendCancellableCoroutine
            }
            handler(
                inviterNickname,
                { if (cont.isActive) cont.resume(Result.success(Unit)) },
                { message -> if (cont.isActive) cont.resume(Result.failure(IllegalStateException(message))) },
            )
        }
}
