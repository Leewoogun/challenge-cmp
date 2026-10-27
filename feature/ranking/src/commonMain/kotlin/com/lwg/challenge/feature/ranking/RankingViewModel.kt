package com.lwg.challenge.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.RankingRepository
import com.lwg.challenge.feature.ranking.contract.RankingUiEffect
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import com.lwg.challenge.utils.flow.valueAs
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RankingViewModel(
    private val rankingRepository: RankingRepository,
) : ViewModel() {

    private val _uiEffect = MutableSharedFlow<RankingUiEffect>()
    val uiEffect: SharedFlow<RankingUiEffect> = _uiEffect.asSharedFlow()

    val uiState: TriggerStateFlow<RankingUiState> = rankingFlow()
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = RankingUiState.Loading,
        )

    fun retry() {
        uiState.restart()
    }

    private fun rankingFlow(): Flow<RankingUiState> = channelFlow {
        uiState.withData<RankingUiState.Error> { send(RankingUiState.Loading) }

        rankingRepository.getLoserRanking(
            onError = { message ->
                if (uiState.valueAs<RankingUiState.Data>() != null) {
                    showMessage(message)
                } else {
                    trySend(RankingUiState.Error)
                }
            },
        ).collect { entries -> send(RankingUiState.Data(entries)) }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(RankingUiEffect.ShowMessage(message))
        }
    }
}
