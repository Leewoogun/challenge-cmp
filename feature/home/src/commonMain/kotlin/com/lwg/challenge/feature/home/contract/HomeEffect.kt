package com.lwg.challenge.feature.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface HomeModalEffect {

    @Immutable
    data object Hidden : HomeModalEffect
}

@Stable
sealed interface HomeUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : HomeUiEffect
}
