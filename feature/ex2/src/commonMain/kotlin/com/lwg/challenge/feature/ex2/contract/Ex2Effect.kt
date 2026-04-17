package com.lwg.challenge.feature.ex2.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface Ex2ModalEffect {

    @Immutable
    data object Hidden : Ex2ModalEffect
}

@Stable
sealed interface Ex2UiEffect {

    @Immutable
    data class ShowMessage(val message: String) : Ex2UiEffect
}
