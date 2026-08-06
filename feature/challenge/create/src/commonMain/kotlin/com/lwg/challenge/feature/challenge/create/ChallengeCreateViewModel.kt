@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.challenge.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.usecase.CreateChallengeUseCase
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiEffect
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.feature.challenge.create.contract.MAX_INPUT_LENGTH
import com.lwg.challenge.utils.datetime.kstDeadlineHintText
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChallengeCreateViewModel(
    private val friendsRepository: FriendsRepository,
    private val createChallengeUseCase: CreateChallengeUseCase,
) : ViewModel() {

    // 진입 시 1회만 환산한다. 자정을 넘겨 화면을 켜둔 채로 두면 표시가 낡지만,
    // 표시 전용 힌트에 실시간 갱신 타이머를 두는 건 과하고 매 emit 마다 다시 재면 값이 흔들린다.
    private val todayDeadlineHint = kstDeadlineHintText(daysAhead = 0)
    private val tomorrowDeadlineHint = kstDeadlineHintText(daysAhead = 1)

    private val _loadFailed = MutableStateFlow(false)
    private val _form = MutableStateFlow(FormState())

    private val friendsFlow = friendsRepository
        .listFriends(
            onError = { message ->
                _loadFailed.update { true }
                showMessage(message)
            },
        )
        // 재시도(restart)마다 상류가 다시 수집되므로, 직전 실패 표시는 여기서 지운다.
        .onStart { _loadFailed.update { false } }
        // 실패한 Flow 는 emit 없이 완료되므로 combine 이 영원히 대기해 화면이 Loading 에 갇힌다.
        // 빈 목록을 흘려 파이프라인을 진행시키고, 실패 여부는 loadFailed 로 따로 전달한다.
        .onEmpty { emit(emptyList()) }

    val uiState: TriggerStateFlow<ChallengeCreateUiState> =
        combine(friendsFlow, _form, _loadFailed) { friends, form, loadFailed ->
            ChallengeCreateUiState.Data(
                step = form.step,
                friends = friends,
                selectedFriendId = form.selectedFriendId,
                myMission = form.myMission,
                betContent = form.betContent,
                deadlineType = form.deadlineType,
                todayDeadlineHint = todayDeadlineHint,
                tomorrowDeadlineHint = tomorrowDeadlineHint,
                signature = form.signature,
                isSubmitting = form.isSubmitting,
                loadFailed = loadFailed,
            )
        }.triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = ChallengeCreateUiState.Loading,
        )

    private val _uiEffect = MutableSharedFlow<ChallengeCreateUiEffect>()
    val uiEffect: SharedFlow<ChallengeCreateUiEffect> = _uiEffect.asSharedFlow()

    fun selectFriend(friendId: Long) {
        _form.update {
            it.copy(
                selectedFriendId = friendId,
                step = ChallengeCreateStep.MISSION_INPUT,
            )
        }
    }

    fun goToOathStep() {
        _form.update { form ->
            val canProceed = !form.isSubmitting &&
                form.myMission.trim().length in 1..MAX_INPUT_LENGTH &&
                form.betContent.trim().length in 1..MAX_INPUT_LENGTH
            if (form.step == ChallengeCreateStep.MISSION_INPUT && canProceed) {
                form.copy(step = ChallengeCreateStep.OATH)
            } else {
                form
            }
        }
    }

    fun goToPreviousStep() {
        _form.update { form ->
            when (form.step) {
                ChallengeCreateStep.OATH -> form.copy(step = ChallengeCreateStep.MISSION_INPUT)
                ChallengeCreateStep.MISSION_INPUT -> form.copy(step = ChallengeCreateStep.FRIEND_PICK)
                ChallengeCreateStep.FRIEND_PICK -> form
            }
        }
    }

    fun updateMyMission(value: String) {
        _form.update { it.copy(myMission = value.take(MAX_INPUT_LENGTH)) }
    }

    fun updateBetContent(value: String) {
        _form.update { it.copy(betContent = value.take(MAX_INPUT_LENGTH)) }
    }

    fun selectDeadline(type: DeadlineType) {
        _form.update { it.copy(deadlineType = type) }
    }

    fun updateSignature(value: Signature) {
        _form.update { it.copy(signature = value) }
    }

    fun clearSignature() {
        _form.update { it.copy(signature = Signature.Empty) }
    }

    fun retryLoadFriends() {
        uiState.restart()
    }

    fun submit() {
        val form = _form.value
        val opponentId = form.selectedFriendId ?: return
        // 빈 서명은 서버가 거부한다 — 요청을 내보내기 전에 막는다.
        if (form.signature.isEmpty) return
        if (form.isSubmitting) return

        _form.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            createChallengeUseCase(
                input = ChallengeCreateInput(
                    opponentId = opponentId,
                    myMission = form.myMission.trim(),
                    betContent = form.betContent.trim(),
                    deadlineType = form.deadlineType,
                    signature = form.signature,
                ),
                onSuccess = { navigateBack() },
                onError = { message ->
                    _form.update { it.copy(isSubmitting = false) }
                    showMessage(message)
                },
            )
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiEffect.emit(ChallengeCreateUiEffect.NavigateBack)
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(ChallengeCreateUiEffect.ShowMessage(message))
        }
    }

    private data class FormState(
        val step: ChallengeCreateStep = ChallengeCreateStep.FRIEND_PICK,
        val selectedFriendId: Long? = null,
        val myMission: String = "",
        val betContent: String = "",
        val deadlineType: DeadlineType = DeadlineType.TODAY,
        val signature: Signature = Signature.Empty,
        val isSubmitting: Boolean = false,
    )
}
