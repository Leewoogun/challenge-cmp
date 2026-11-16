package com.lwg.challenge.feature.challenge.create.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface ChallengeCreateModalEffect {

    @Immutable
    data object Hidden : ChallengeCreateModalEffect

    @Immutable
    data object DeadlineCalendar : ChallengeCreateModalEffect
}

@Stable
sealed interface ChallengeCreateUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : ChallengeCreateUiEffect

    @Immutable
    data object NavigateBack : ChallengeCreateUiEffect
}
