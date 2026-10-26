package com.lwg.challenge.feature.notification.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface NotificationUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : NotificationUiEffect
}
