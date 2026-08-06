package com.lwg.challenge.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.feature.home.contract.ReceivedChallengeItemState
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    getHomeDataUseCase: GetHomeDataUseCase,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val _inFlightIds = MutableStateFlow<Set<Long>>(emptySet())

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect: SharedFlow<HomeUiEffect> = _uiEffect.asSharedFlow()

    private val homeDataFlow: Flow<HomeUiState.Data> = getHomeDataUseCase(::showMessage)
        .map { data ->
            // now 는 매핑 시점에 1회만 측정한다 — 항목마다 재측정하면 임박 판정이 서로 어긋난다.
            val now = nowKst()
            HomeUiState.Data(
                record = data.record,
                challenges = data.challenges,
                now = now,
                userInfo = data.userInfo,
                receivedChallenges = data.receivedChallenges.map {
                    ReceivedChallengeItemState(it, now)
                },
            )
        }

    val uiState: TriggerStateFlow<HomeUiState> =
        combine(homeDataFlow, _inFlightIds) { data, inFlightIds ->
            data.copy(inFlightIds = inFlightIds)
        }.triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = HomeUiState.Loading,
        )

    fun rejectChallenge(item: ReceivedChallengeItemState) {
        val challengeId = item.challengeId
        if (challengeId in _inFlightIds.value) return

        _inFlightIds.update { it + challengeId }

        viewModelScope.launch {
            challengeRepository.rejectChallenge(
                challengeId = challengeId,
                onSuccess = uiState::restart,
                onError = { message ->
                    showMessage(message)
                    uiState.restart()
                },
            )
            _inFlightIds.update { it - challengeId }
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(HomeUiEffect.ShowMessage(message))
        }
    }
}
