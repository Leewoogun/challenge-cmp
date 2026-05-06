package com.lwg.challenge.feature.login.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface LoginUiEffect {

    /** 비즈니스/네트워크 에러를 스낵바로 알림. */
    @Immutable
    data class ShowMessage(val message: String) : LoginUiEffect

    /** 로그인 성공 → 홈 화면으로 이동. */
    @Immutable
    data class NavigateToHome(val isNewUser: Boolean) : LoginUiEffect
}
