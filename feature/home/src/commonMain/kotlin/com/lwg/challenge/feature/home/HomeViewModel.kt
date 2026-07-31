package com.lwg.challenge.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.feature.home.contract.MAX_ACCEPT_MISSION_LENGTH
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
    private val _acceptForm = MutableStateFlow(AcceptFormState())

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

    val uiState: StateFlow<HomeUiState> = combine(homeDataFlow, _acceptForm) { data, form ->
        data.copy(
            acceptTarget = form.target,
            acceptMission = form.mission,
            isSubmittingAccept = form.isSubmitting,
            inFlightIds = form.inFlightIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(0),
        initialValue = HomeUiState.Loading,
    )

    fun openAcceptDialog(item: ReceivedChallengeItemState) {
        _acceptForm.update { it.copy(target = item, mission = "") }
    }

    fun dismissAcceptDialog() {
        if (_acceptForm.value.isSubmitting) return
        _acceptForm.update { it.copy(target = null, mission = "") }
    }

    fun updateAcceptMission(value: String) {
        _acceptForm.update { it.copy(mission = value.take(MAX_ACCEPT_MISSION_LENGTH)) }
    }

    fun submitAccept() {
        val form = _acceptForm.value
        val challengeId = form.target?.challengeId ?: return
        if (form.isSubmitting) return

        _acceptForm.update {
            it.copy(isSubmitting = true, inFlightIds = it.inFlightIds + challengeId)
        }

        viewModelScope.launch {
            challengeRepository.acceptChallenge(
                challengeId = challengeId,
                myMission = form.mission.trim(),
                onSuccess = {
                    closeAcceptDialog(challengeId)
                    // 받은 도전장과 진행 중 목록을 GetHomeDataUseCase 가 함께 물고 있어 reload 1회로 둘 다 갱신된다.
                    reload()
                    showMessage("챌린지가 시작됐어요")
                },
                // 계약상 모바일은 실패 code 를 구분하지 않는다. 어떤 실패든 스낵바 + 목록 재조회로 통일한다.
                onError = { message ->
                    closeAcceptDialog(challengeId)
                    showMessage(message)
                    reload()
                },
            )
        }
    }

    fun rejectChallenge(item: ReceivedChallengeItemState) {
        val challengeId = item.challengeId
        if (challengeId in _acceptForm.value.inFlightIds) return

        _acceptForm.update { it.copy(inFlightIds = it.inFlightIds + challengeId) }

        viewModelScope.launch {
            challengeRepository.rejectChallenge(
                challengeId = challengeId,
                onSuccess = ::reload,
                onError = { message ->
                    showMessage(message)
                    reload()
                },
            )
            _acceptForm.update { it.copy(inFlightIds = it.inFlightIds - challengeId) }
        }
    }

    private fun closeAcceptDialog(challengeId: Long) {
        _acceptForm.update {
            it.copy(
                target = null,
                mission = "",
                isSubmitting = false,
                inFlightIds = it.inFlightIds - challengeId,
            )
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

    private data class AcceptFormState(
        val target: ReceivedChallengeItemState? = null,
        val mission: String = "",
        val isSubmitting: Boolean = false,
        val inFlightIds: Set<Long> = emptySet(),
    )
}
