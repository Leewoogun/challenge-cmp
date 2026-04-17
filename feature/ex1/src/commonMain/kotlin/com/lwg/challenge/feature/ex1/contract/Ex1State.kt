package com.lwg.challenge.feature.ex1.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface Ex1UiState {

    @Immutable
    data object Loading : Ex1UiState

    @Immutable
    data class Data(
        val placeholder: Unit = Unit, // TODO: 실제 데이터 필드로 교체하세요.
    ) : Ex1UiState
}
