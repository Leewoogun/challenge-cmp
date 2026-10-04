package com.lwg.challenge.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
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
            HomeUiState.Data(
                record = data.record,
                challenges = data.challenges,
                now = nowKst(),
                userInfo = data.userInfo,
                receivedChallenges = data.receivedChallenges,
                unreadCount = data.unreadCount,
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

    fun rejectChallenge(challenge: ReceivedChallenge) {
        val challengeId = challenge.challengeId
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
