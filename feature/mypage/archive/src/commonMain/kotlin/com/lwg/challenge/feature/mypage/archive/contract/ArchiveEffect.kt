package com.lwg.challenge.feature.mypage.archive.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface ArchiveUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : ArchiveUiEffect
}
