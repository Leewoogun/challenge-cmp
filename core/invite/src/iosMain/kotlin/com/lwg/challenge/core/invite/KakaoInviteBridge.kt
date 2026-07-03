package com.lwg.challenge.core.invite

/**
 * iOS 카카오톡 공유(KakaoSDKShare) 연동 bridge.
 *
 * Swift 측에서 KakaoSDKShare 통합 시 [inviteHandler] 를 주입한다.
 * 미설정이면 [IosKakaoInviter] 가 실패로 처리한다 (feature:login 의 KakaoLoginBridge 미러링).
 */
object KakaoInviteBridge {
    var inviteHandler: ((
        inviterNickname: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) -> Unit)? = null
}
