package com.lwg.challenge.feature.friends.record.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface FriendRecordUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : FriendRecordUiEffect
}
