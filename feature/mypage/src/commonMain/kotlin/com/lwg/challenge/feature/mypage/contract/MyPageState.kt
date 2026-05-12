package com.lwg.challenge.feature.mypage.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface MyPageUiState {

    @Immutable
    data object Loading : MyPageUiState

    @Immutable
    data class Data(
        val placeholder: Unit = Unit, // TODO: 실제 데이터 필드로 교체하세요.
    ) : MyPageUiState
}
