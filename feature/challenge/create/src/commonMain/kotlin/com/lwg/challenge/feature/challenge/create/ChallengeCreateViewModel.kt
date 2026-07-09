package com.lwg.challenge.feature.challenge.create

import androidx.lifecycle.ViewModel
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChallengeCreateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeCreateUiState>(ChallengeCreateUiState.Data)
    val uiState: StateFlow<ChallengeCreateUiState> = _uiState.asStateFlow()
}
