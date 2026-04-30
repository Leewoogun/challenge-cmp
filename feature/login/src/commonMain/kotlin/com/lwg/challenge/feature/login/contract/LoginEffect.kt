package com.lwg.challenge.feature.login.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface LoginModalEffect {

    @Immutable
    data object Hidden : LoginModalEffect

    /** code=701 (카카오 토큰 만료) — 재로그인 유도 다이얼로그. */
    @Immutable
    data class ReLogin(val message: String) : LoginModalEffect

    /** code=703 (카카오 서버 장애) — 재시도 버튼이 있는 전체화면 에러 안내 다이얼로그. */
    @Immutable
    data class ServerError(val message: String) : LoginModalEffect
}

@Stable
sealed interface LoginUiEffect {

    /** code=700 등 스낵바 수준 알림. */
    @Immutable
    data class ShowMessage(val message: String) : LoginUiEffect

    /** 로그인 성공 → 홈 화면으로 이동. */
    @Immutable
    data class NavigateToHome(val isNewUser: Boolean) : LoginUiEffect
}
