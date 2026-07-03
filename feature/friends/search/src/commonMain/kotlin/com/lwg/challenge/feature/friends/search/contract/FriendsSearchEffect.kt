package com.lwg.challenge.feature.friends.search.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface FriendsSearchUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : FriendsSearchUiEffect
}
