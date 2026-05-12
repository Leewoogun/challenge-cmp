package com.lwg.challenge.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.feature.ranking.contract.RankingModalEffect
import com.lwg.challenge.feature.ranking.contract.RankingUiEffect
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RankingViewModel(
    // TODO: 의존성을 주입하세요.
) : ViewModel() {

    private val _uiState: MutableStateFlow<RankingUiState> = MutableStateFlow(RankingUiState.Data())
    val uiState: StateFlow<RankingUiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<RankingModalEffect>(RankingModalEffect.Hidden)
    val modalEffect: StateFlow<RankingModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<RankingUiEffect>()
    val uiEffect: SharedFlow<RankingUiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { RankingModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(RankingUiEffect.ShowMessage(message))
        }
    }
}
