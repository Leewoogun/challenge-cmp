package com.lwg.challenge.feature.ranking.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface RankingUiState {

    @Immutable
    data object Loading : RankingUiState

    @Immutable
    data class Data(
        val placeholder: Unit = Unit, // TODO: 실제 데이터 필드로 교체하세요.
    ) : RankingUiState
}
