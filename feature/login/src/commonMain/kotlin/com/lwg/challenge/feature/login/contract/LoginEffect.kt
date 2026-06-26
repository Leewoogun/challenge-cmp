package com.lwg.challenge.feature.login.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface LoginUiEffect {
    @Immutable
    data class ShowMessage(val message: String) : LoginUiEffect

    @Immutable
    data class NavigateToHome(val isNewUser: Boolean) : LoginUiEffect
}

