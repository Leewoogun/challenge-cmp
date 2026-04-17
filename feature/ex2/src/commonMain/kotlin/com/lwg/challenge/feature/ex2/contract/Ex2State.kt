package com.lwg.challenge.feature.ex2.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface Ex2UiState {

    @Immutable
    data object Loading : Ex2UiState

    @Immutable
    data class Data(
        val placeholder: Unit = Unit, // TODO: 실제 데이터 필드로 교체하세요.
    ) : Ex2UiState
}
