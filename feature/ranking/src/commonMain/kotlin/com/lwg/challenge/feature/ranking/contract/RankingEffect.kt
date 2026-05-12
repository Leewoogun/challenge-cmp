package com.lwg.challenge.feature.ranking.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface RankingModalEffect {

    @Immutable
    data object Hidden : RankingModalEffect
}

@Stable
sealed interface RankingUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : RankingUiEffect
}
