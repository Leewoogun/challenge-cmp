package com.lwg.challenge

import com.lwg.challenge.core.invite.KakaoInviteBridge

interface KakaoInviteHandler {
    fun invite(
        inviterNickname: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}

fun registerKakaoInviteHandler(handler: KakaoInviteHandler) {
    KakaoInviteBridge.inviteHandler = { inviterNickname, onSuccess, onError ->
        handler.invite(inviterNickname, onSuccess, onError)
    }
}
