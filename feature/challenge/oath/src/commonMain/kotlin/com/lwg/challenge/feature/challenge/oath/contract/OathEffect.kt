package com.lwg.challenge.feature.challenge.oath.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface OathUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : OathUiEffect

    @Immutable
    data object NavigateBack : OathUiEffect
}
