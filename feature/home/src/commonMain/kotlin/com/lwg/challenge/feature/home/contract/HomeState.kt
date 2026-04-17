package com.lwg.challenge.feature.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.Movie

@Stable
sealed interface HomeUiState {

    @Immutable
    data object Loading : HomeUiState

    @Immutable
    data object Empty : HomeUiState

    @Immutable
    data class Data(
        val movies: List<Movie>,
    ) : HomeUiState {
        val isEmpty: Boolean get() = movies.isEmpty()
    }
}
