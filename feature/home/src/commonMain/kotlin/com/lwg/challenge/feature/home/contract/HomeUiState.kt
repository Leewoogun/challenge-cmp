package com.lwg.challenge.feature.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.UserRecord

@Stable
sealed interface HomeUiState {

    @Immutable
    data object Loading : HomeUiState

    @Immutable
    data class Data(
        val record: UserRecord,
        val challenges: List<ActiveChallenge>,
    ) : HomeUiState {
        val emptyType: HomeEmptyType
            get() = when {
                record.isEmpty && challenges.isEmpty() -> HomeEmptyType.FIRST_USER
                challenges.isEmpty() -> HomeEmptyType.NO_ACTIVE_CHALLENGE
                else -> HomeEmptyType.NONE
            }
    }
}

enum class HomeEmptyType {
    NONE,
    FIRST_USER,
    NO_ACTIVE_CHALLENGE,
}
