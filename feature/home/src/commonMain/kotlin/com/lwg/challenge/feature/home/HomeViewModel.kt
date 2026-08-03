package com.lwg.challenge.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.feature.home.contract.ReceivedChallengeItemState
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class HomeViewModel(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _inFlightIds = MutableStateFlow<Set<Long>>(emptySet())

    private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<HomeUiEffect> = _uiEffect.receiveAsFlow()

    private val homeDataFlow: Flow<HomeUiState.Data> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getHomeDataUseCase(::showMessage).map { data ->
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
        }

    // WhileSubscribed(0) 는 성능 최적화가 아니라 수락 후 홈 갱신의 유일한 근거다. 맹세 화면이 push 되면
    // 홈 컴포지션이 dispose 돼 구독자가 0이 되고, 돌아와 재구독하는 순간 upstream 이 재시작되며
    // refreshTrigger.onStart 가 다시 발화해 목록을 새로 읽는다. 홈에는 수락 완료를 알 다른 경로가 없다.
    val uiState: StateFlow<HomeUiState> = combine(homeDataFlow, _inFlightIds) { data, inFlightIds ->
        data.copy(inFlightIds = inFlightIds)
    }.stateIn(
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
                onSuccess = ::reload,
                onError = { message ->
                    showMessage(message)
                    reload()
                },
            )
            _inFlightIds.update { it - challengeId }
        }
    }

    private fun reload() {
        refreshTrigger.tryEmit(Unit)
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.send(HomeUiEffect.ShowMessage(message))
        }
    }
}
