package com.lwg.challenge.feature.challenge.create.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 챌린지 생성 화면 UI 상태 — 아직 스펙 미확정 스텁.
 */
@Stable
sealed interface ChallengeCreateUiState {

    @Immutable
    data object Loading : ChallengeCreateUiState

    @Immutable
    data object Data : ChallengeCreateUiState
}
