package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.usecase.GetChallengeDetailDataUseCase
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailModalEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.HeadlineDisplay
import com.lwg.challenge.feature.challenge.detail.contract.headlineDisplay
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.Duration.Companion.minutes

/** 남은 시간 표기가 분 단위다. */
private val HEADLINE_TICK_INTERVAL = 1.minutes

@KoinViewModel
class ChallengeDetailViewModel(
    @InjectedParam private val challengeId: Long,
    private val getChallengeDetailDataUseCase: GetChallengeDetailDataUseCase,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val _isForfeiting = MutableStateFlow(false)

    private val _modalEffect = MutableStateFlow<ChallengeDetailModalEffect>(ChallengeDetailModalEffect.Hidden)
    val modalEffect: StateFlow<ChallengeDetailModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<ChallengeDetailUiEffect>()
    val uiEffect: SharedFlow<ChallengeDetailUiEffect> = _uiEffect.asSharedFlow()

    val uiState: TriggerStateFlow<ChallengeDetailUiState> = detailFlow()
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = ChallengeDetailUiState.Loading,
        )

    fun retry() {
        uiState.restart()
    }

    fun requestForfeit() {
        _modalEffect.update { ChallengeDetailModalEffect.ConfirmForfeit }
    }

    fun forfeit() {
        if (_isForfeiting.value) return
        _isForfeiting.update { true }
        dismiss()

        viewModelScope.launch {
            challengeRepository.forfeitChallenge(
                challengeId = challengeId,
                onSuccess = uiState::restart,
                onError = { message ->
                    showMessage(message)
                    uiState.restart()
                },
            )
        }
    }

    fun dismiss() {
        _modalEffect.update { ChallengeDetailModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(ChallengeDetailUiEffect.ShowMessage(message))
        }
    }

    private fun detailFlow(): Flow<ChallengeDetailUiState> = channelFlow {
        // 재조회 중엔 직전 값이 유지되므로, 보던 Data 는 두고 Error 에서 재시도할 때만 스피너를 띄운다.
        uiState.withData<ChallengeDetailUiState.Error> { send(ChallengeDetailUiState.Loading) }

        getChallengeDetailDataUseCase(
            challengeId = challengeId,
            onError = { message -> trySend(ChallengeDetailUiState.Error(message)) },
        ).collectLatest { data ->
            _isForfeiting.update { false }
            combine(headlineFlow(data.detail.deadline, data.detail.myResult), _isForfeiting) { headline, isForfeiting ->
                ChallengeDetailUiState.Data(
                    detail = data.detail,
                    verifications = data.verifications,
                    headline = headline,
                    isForfeiting = isForfeiting,
                )
            }.collect(::send)
        }
    }

    /** 마감 이후엔 시계로 바뀔 표기가 없어 마감을 넘긴 회차까지만 돈다. */
    private fun headlineFlow(deadline: LocalDateTime, result: ChallengeOutcome?): Flow<HeadlineDisplay> = flow {
        emit(headlineDisplay(result, deadline, nowKst()))
        while (nowKst() < deadline) {
            delay(HEADLINE_TICK_INTERVAL)
            emit(headlineDisplay(result, deadline, nowKst()))
        }
    }.distinctUntilChanged()
}
