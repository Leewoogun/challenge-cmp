package com.lwg.challenge.feature.friends.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface FriendsModalEffect {

    @Immutable
    data object Hidden : FriendsModalEffect
}

@Stable
sealed interface FriendsUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : FriendsUiEffect
}
