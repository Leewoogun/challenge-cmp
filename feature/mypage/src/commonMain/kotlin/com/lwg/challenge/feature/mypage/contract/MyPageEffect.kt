package com.lwg.challenge.feature.mypage.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface MyPageModalEffect {

    @Immutable
    data object Hidden : MyPageModalEffect
}

@Stable
sealed interface MyPageUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : MyPageUiEffect
}
