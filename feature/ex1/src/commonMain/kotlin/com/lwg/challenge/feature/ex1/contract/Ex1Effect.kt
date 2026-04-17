package com.lwg.challenge.feature.ex1.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface Ex1ModalEffect {

    @Immutable
    data object Hidden : Ex1ModalEffect
}

@Stable
sealed interface Ex1UiEffect {

    @Immutable
    data class ShowMessage(val message: String) : Ex1UiEffect
}
