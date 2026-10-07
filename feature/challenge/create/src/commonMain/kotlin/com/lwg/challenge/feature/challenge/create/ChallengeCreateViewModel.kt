@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.challenge.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.usecase.CreateChallengeUseCase
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiEffect
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.feature.challenge.create.contract.MAX_INPUT_LENGTH
import com.lwg.challenge.feature.challenge.create.contract.UserInputState
import com.lwg.challenge.utils.datetime.kstDeadlineHintText
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChallengeCreateViewModel(
    friendsRepository: FriendsRepository,
    private val createChallengeUseCase: CreateChallengeUseCase,
) : ViewModel() {

    private val todayDeadlineHint = kstDeadlineHintText(daysAhead = 0)
    private val tomorrowDeadlineHint = kstDeadlineHintText(daysAhead = 1)

    private val _friendListLoadFailed = MutableStateFlow(false)
    private val _userInputForm = MutableStateFlow(UserInputState())

    private val friendsFlow = friendsRepository
        .listFriends(
            onError = { message ->
                _friendListLoadFailed.update { true }
                showMessage(message)
            },
        )
        .onStart { _friendListLoadFailed.update { false } }
        .onEmpty { emit(emptyList()) }

    val uiState: TriggerStateFlow<ChallengeCreateUiState> =
        combine(friendsFlow, _userInputForm, _friendListLoadFailed) { friends, form, friendListLoadFailed ->
            form.toUiState(friends, friendListLoadFailed)
        }.triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = ChallengeCreateUiState.Loading,
        )

    private val _uiEffect = MutableSharedFlow<ChallengeCreateUiEffect>()
    val uiEffect: SharedFlow<ChallengeCreateUiEffect> = _uiEffect.asSharedFlow()

    fun selectFriend(friend: Friend) {
        _userInputForm.update {
            it.copy(
                selectedFriend = friend,
                step = ChallengeCreateStep.MISSION_INPUT,
            )
        }
    }

    fun goToOathStep() {
        _userInputForm.update { form ->
            if (form.step == ChallengeCreateStep.MISSION_INPUT && form.canProceedToOath) {
                form.copy(step = ChallengeCreateStep.OATH)
            } else {
                form
            }
        }
    }

    fun goToPreviousStep() {
        _userInputForm.update { form ->
            if (form.isSubmitting) return@update form
            when (form.step) {
                ChallengeCreateStep.OATH -> form.copy(step = ChallengeCreateStep.MISSION_INPUT)
                ChallengeCreateStep.MISSION_INPUT -> form.copy(step = ChallengeCreateStep.FRIEND_PICK)
                ChallengeCreateStep.FRIEND_PICK -> form
            }
        }
    }

    fun updateMyMission(value: String) {
        _userInputForm.update { it.copy(myMission = value.take(MAX_INPUT_LENGTH)) }
    }

    fun updatePenalty(value: String) {
        _userInputForm.update { it.copy(penalty = value.take(MAX_INPUT_LENGTH)) }
    }

    fun selectDeadline(type: DeadlineType) {
        _userInputForm.update { it.copy(deadlineType = type) }
    }

    fun updateSignature(value: Signature) {
        _userInputForm.update { it.copy(signature = value) }
    }

    fun clearSignature() {
        _userInputForm.update { it.copy(signature = Signature.Empty) }
    }

    fun retryLoadFriends() {
        uiState.restart()
    }

    fun submit() {
        val form = _userInputForm.value
        val opponentId = form.selectedFriend?.userId ?: return
        // 빈 서명은 서버가 거부한다 — 요청을 내보내기 전에 막는다.
        if (form.signature.isEmpty) return
        if (form.isSubmitting) return

        _userInputForm.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            createChallengeUseCase(
                input = ChallengeCreateInput(
                    opponentId = opponentId,
                    myMission = form.myMission.trim(),
                    penalty = form.penalty.trim(),
                    deadlineType = form.deadlineType,
                    signature = form.signature,
                ),
                onSuccess = { navigateBack() },
                onError = { message ->
                    _userInputForm.update { it.copy(isSubmitting = false) }
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

    private fun UserInputState.toUiState(
        friends: List<Friend>,
        friendListLoadFailed: Boolean,
    ): ChallengeCreateUiState = when (step) {
        ChallengeCreateStep.FRIEND_PICK -> ChallengeCreateUiState.FriendPick(
            friends = friends,
            selectedFriendId = selectedFriend?.userId,
            loadFailed = friendListLoadFailed,
        )

        ChallengeCreateStep.MISSION_INPUT -> ChallengeCreateUiState.MissionInput(
            myMission = myMission,
            penalty = penalty,
            deadlineType = deadlineType,
            todayDeadlineHint = todayDeadlineHint,
            tomorrowDeadlineHint = tomorrowDeadlineHint,
        )

        ChallengeCreateStep.OATH -> ChallengeCreateUiState.Oath(
            opponent = checkNotNull(selectedFriend),
            myMission = myMission,
            penalty = penalty,
            deadlineText = when (deadlineType) {
                DeadlineType.TODAY -> todayDeadlineHint
                DeadlineType.TOMORROW -> tomorrowDeadlineHint
            },
            signature = signature,
            isSubmitting = isSubmitting,
        )
    }
}
