package com.lwg.challenge.feature.friends.list.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface FriendsUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : FriendsUiEffect
}
