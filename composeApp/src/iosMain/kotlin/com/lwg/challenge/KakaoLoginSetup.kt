package com.lwg.challenge

import com.lwg.challenge.feature.login.kakao.KakaoLoginBridge

interface KakaoLoginHandler {
    fun login(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit,
    )
}

fun registerKakaoLoginHandler(handler: KakaoLoginHandler) {
    KakaoLoginBridge.loginHandler = { onSuccess, onError, onCancelled ->
        handler.login(onSuccess, onError, onCancelled)
    }
}
