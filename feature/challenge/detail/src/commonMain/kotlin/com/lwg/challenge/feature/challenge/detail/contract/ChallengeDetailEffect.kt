package com.lwg.challenge.feature.challenge.detail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface ChallengeDetailModalEffect {

    @Immutable
    data object Hidden : ChallengeDetailModalEffect

    /** 기권 확인 다이얼로그. 여기서 확정하기 전까지 요청은 나가지 않는다. */
    @Immutable
    data object ConfirmForfeit : ChallengeDetailModalEffect
}

@Stable
sealed interface ChallengeDetailUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : ChallengeDetailUiEffect
}
