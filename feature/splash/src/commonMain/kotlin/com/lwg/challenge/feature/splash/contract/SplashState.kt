package com.lwg.challenge.feature.splash.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface SplashUiState {

    @Immutable
    data object Loading : SplashUiState
}
