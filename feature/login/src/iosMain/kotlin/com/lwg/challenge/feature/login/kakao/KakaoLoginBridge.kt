package com.lwg.challenge.feature.login.kakao

object KakaoLoginBridge {
    var loginHandler: ((
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit,
    ) -> Unit)? = null
}
