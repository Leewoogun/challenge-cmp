package com.lwg.challenge.feature.splash.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface SplashUiEffect {

    /** 저장된 토큰 없음 또는 refresh 만료 → 로그인 화면. */
    @Immutable
    data object NavigateToLogin : SplashUiEffect

    /** refresh 성공 → 홈 화면. */
    @Immutable
    data object NavigateToHome : SplashUiEffect
}
