package com.lwg.challenge.feature.ex3.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface Ex3ModalEffect {

    @Immutable
    data object Hidden : Ex3ModalEffect
}

@Stable
sealed interface Ex3UiEffect {

    @Immutable
    data class ShowMessage(val message: String) : Ex3UiEffect
}
