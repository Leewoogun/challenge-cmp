package com.lwg.challenge.feature.main.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.navigation.Route

@Stable
sealed interface MainUiEffect {

    /** 세션 만료 · 사용자 로그아웃 · 회원탈퇴가 모두 여기로 모인다. */
    @Immutable
    data object NavigateToLogin : MainUiEffect

    /** 푸시 딥링크 목적지. 도착 판단은 ViewModel, 백스택 처리는 화면 몫이다. */
    @Immutable
    data class NavigateToPush(val route: Route) : MainUiEffect
}
