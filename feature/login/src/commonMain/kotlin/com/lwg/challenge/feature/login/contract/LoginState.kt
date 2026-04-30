package com.lwg.challenge.feature.login.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 로그인 화면의 UI 상태.
 *
 * - [Idle] : 버튼 노출, 유저 액션 대기.
 * - [Loading] : 서버 통신 중. 버튼 비활성화 + 로딩 표시.
 * - [Error] : 직전 시도 실패. 메시지와 함께 Idle 로 돌아감 (UiEffect/Dialog 로도 알림).
 */
@Stable
sealed interface LoginUiState {

    @Immutable
    data object Idle : LoginUiState

    @Immutable
    data object Loading : LoginUiState

    @Immutable
    data class Error(val message: String) : LoginUiState
}
