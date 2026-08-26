package com.lwg.challenge.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.RankingRepository
import com.lwg.challenge.feature.ranking.contract.LoserRankingItemState
import com.lwg.challenge.feature.ranking.contract.RankingUiEffect
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RankingViewModel(
    rankingRepository: RankingRepository,
) : ViewModel() {

    private val _uiEffect = MutableSharedFlow<RankingUiEffect>()
    val uiEffect: SharedFlow<RankingUiEffect> = _uiEffect.asSharedFlow()

    val uiState: TriggerStateFlow<RankingUiState> =
        rankingRepository.getLoserRanking(::showMessage)
            .map { entries -> RankingUiState.Data(entries.map(::LoserRankingItemState)) }
            .triggerStateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(0),
                initialValue = RankingUiState.Loading,
            )

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(RankingUiEffect.ShowMessage(message))
        }
    }
}
