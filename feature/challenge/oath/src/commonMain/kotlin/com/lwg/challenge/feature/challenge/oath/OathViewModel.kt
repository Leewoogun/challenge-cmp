package com.lwg.challenge.feature.challenge.oath

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.feature.challenge.oath.contract.MAX_OATH_MISSION_LENGTH
import com.lwg.challenge.feature.challenge.oath.contract.OathModalEffect
import com.lwg.challenge.feature.challenge.oath.contract.OathUiEffect
import com.lwg.challenge.feature.challenge.oath.contract.OathUiState
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

private const val SUBMIT_SUCCESS_MESSAGE = "맹세가 완료됐어요 🔥"

@KoinViewModel
class OathViewModel(
    @InjectedParam private val challengeId: Long,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    // 조회 파이프라인 밖에 둬서 재조회해도 작성 중이던 미션·서명이 남는다.
    private val _myMission = MutableStateFlow("")
    private val _signature = MutableStateFlow(Signature.Empty)
    private val _isSubmitting = MutableStateFlow(false)

    private val _modalEffect = MutableStateFlow<OathModalEffect>(OathModalEffect.Hidden)
    val modalEffect: StateFlow<OathModalEffect> = _modalEffect.asStateFlow()

    private val _uiEffect = MutableSharedFlow<OathUiEffect>()
    val uiEffect: SharedFlow<OathUiEffect> = _uiEffect.asSharedFlow()

    val uiState: TriggerStateFlow<OathUiState> = oathFlow()
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = OathUiState.Loading,
        )

    fun retry() {
        uiState.restart()
    }

    fun updateMyMission(value: String) {
        _myMission.update { value.take(MAX_OATH_MISSION_LENGTH) }
    }

    fun updateSignature(value: Signature) {
        _signature.update { value }
    }

    fun submit() {
        uiState.withData<OathUiState.Data> { data ->
            if (!data.canSubmit) return

            _isSubmitting.update { true }

            viewModelScope.launch {
                challengeRepository.acceptChallenge(
                    challengeId = challengeId,
                    myMission = data.myMission.trim(),
                    signature = data.signature,
                    onSuccess = {
                        // 화면이 닫히기 전에 완료 메시지가 먼저 나가야 해서 한 코루틴에서 순서대로 보낸다.
                        viewModelScope.launch {
                            _uiEffect.emit(OathUiEffect.ShowMessage(SUBMIT_SUCCESS_MESSAGE))
                            _uiEffect.emit(OathUiEffect.NavigateBack)
                        }
                    },
                    onError = { message ->
                        _isSubmitting.update { false }
                        showMessage(message)
                    },
                )
            }
        }
    }

    /** 제출 중에는 결과를 모른 채 화면을 잃게 되므로 이탈 자체를 막는다. */
    fun requestExit() {
        uiState.withData<OathUiState.Data> { data ->
            if (data.isSubmitting) return

            if (data.isDirty) {
                _modalEffect.update { OathModalEffect.ExitConfirm }
                return
            }
        }

        navigateBack()
    }

    fun confirmExit() {
        dismiss()
        navigateBack()
    }

    fun dismiss() {
        _modalEffect.update { OathModalEffect.Hidden }
    }

    private fun oathFlow(): Flow<OathUiState> = channelFlow {
        uiState.withData<OathUiState.Error> { send(OathUiState.Loading) }

        challengeRepository.getChallengeDetail(
            challengeId = challengeId,
            onError = onError@{ message ->
                uiState.withData<OathUiState.Data> {
                    showMessage(message)
                    return@onError
                }
                trySend(OathUiState.Error(message))
            },
        ).collectLatest { detail ->
            val now = nowKst()
            combine(_myMission, _signature, _isSubmitting) { myMission, signature, isSubmitting ->
                OathUiState.Data(
                    detail = detail,
                    now = now,
                    myMission = myMission,
                    signature = signature,
                    isSubmitting = isSubmitting,
                )
            }.collect(::send)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiEffect.emit(OathUiEffect.NavigateBack)
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(OathUiEffect.ShowMessage(message))
        }
    }
}
