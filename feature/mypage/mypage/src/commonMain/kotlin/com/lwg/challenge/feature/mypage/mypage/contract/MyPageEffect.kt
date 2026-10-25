package com.lwg.challenge.feature.mypage.mypage.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface MyPageModalEffect {

    @Immutable
    data object Hidden : MyPageModalEffect

    @Immutable
    data object ConfirmWithdraw : MyPageModalEffect
}

@Stable
sealed interface MyPageUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : MyPageUiEffect

    /** 탈퇴가 성공하고 로컬 정리까지 끝났다. 화면이 로그인으로 보내야 한다. */
    @Immutable
    data object Withdrawn : MyPageUiEffect
}
