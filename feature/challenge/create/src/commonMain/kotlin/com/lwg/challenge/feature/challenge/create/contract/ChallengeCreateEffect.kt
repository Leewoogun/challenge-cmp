package com.lwg.challenge.feature.challenge.create.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface ChallengeCreateUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : ChallengeCreateUiEffect
}
