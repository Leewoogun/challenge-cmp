package com.lwg.challenge.feature.challenge.oath.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface OathModalEffect {

    @Immutable
    data object Hidden : OathModalEffect

    /** 작성 중 이탈 확인. 미션·서명이 하나라도 있을 때만 띄운다. */
    @Immutable
    data object ExitConfirm : OathModalEffect
}

@Stable
sealed interface OathUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : OathUiEffect

    @Immutable
    data object NavigateBack : OathUiEffect
}
