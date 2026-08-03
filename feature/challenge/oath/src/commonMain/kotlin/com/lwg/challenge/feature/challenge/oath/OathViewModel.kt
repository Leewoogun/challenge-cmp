package com.lwg.challenge.feature.challenge.oath

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.feature.challenge.oath.contract.MAX_OATH_MISSION_LENGTH
import com.lwg.challenge.feature.challenge.oath.contract.OathUiEffect
import com.lwg.challenge.feature.challenge.oath.contract.OathUiState
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import com.lwg.challenge.utils.flow.updateWithData
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow<OathUiState>(OathUiState.Loading)
    val uiState: StateFlow<OathUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<OathUiEffect>()
    val uiEffect: SharedFlow<OathUiEffect> = _uiEffect.asSharedFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun updateMyMission(value: String) {
        _uiState.updateWithData<OathUiState, OathUiState.Data> {
            it.copy(myMission = value.take(MAX_OATH_MISSION_LENGTH))
        }
    }

    fun updateSignature(value: Signature) {
        _uiState.updateWithData<OathUiState, OathUiState.Data> { it.copy(signature = value) }
    }

    fun submit() {
        _uiState.withData<OathUiState.Data> { data ->
            if (!data.canSubmit) return@withData

            _uiState.updateWithData<OathUiState, OathUiState.Data> { it.copy(isSubmitting = true) }

            viewModelScope.launch {
                challengeRepository.acceptChallenge(
                    challengeId = challengeId,
                    myMission = data.myMission.trim(),
                    signature = data.signature,
                    onSuccess = {
                        // 한 코루틴에서 순서대로 흘린다 — 화면이 닫히기 전에 완료 메시지가 먼저 나가야 한다.
                        viewModelScope.launch {
                            _uiEffect.emit(OathUiEffect.ShowMessage(SUBMIT_SUCCESS_MESSAGE))
                            _uiEffect.emit(OathUiEffect.NavigateBack)
                        }
                    },
                    onError = { message ->
                        // 실패해도 화면을 닫지 않는다. 공들여 그린 서명을 요청 한 번 실패로 버리게 하는 비용이
                        // 너무 커서, 다이얼로그 시절의 "실패 시 닫기" 와 의도적으로 다르게 간다.
                        _uiState.updateWithData<OathUiState, OathUiState.Data> {
                            it.copy(isSubmitting = false)
                        }
                        showMessage(message)
                    },
                )
            }
        }
    }

    /** 제출 중에는 결과를 모른 채 화면을 잃게 되므로 이탈 자체를 막는다. */
    fun requestExit() {
        val data = _uiState.value as? OathUiState.Data
        if (data?.isSubmitting == true) return

        if (data?.isDirty == true) {
            _uiState.updateWithData<OathUiState, OathUiState.Data> { it.copy(showExitConfirm = true) }
        } else {
            navigateBack()
        }
    }

    fun confirmExit() {
        navigateBack()
    }

    fun dismissExitConfirm() {
        _uiState.updateWithData<OathUiState, OathUiState.Data> { it.copy(showExitConfirm = false) }
    }

    private fun load() {
        // 이미 Data 면 Loading 으로 되돌리지 않는다 — 재시도할 때마다 작성 중이던 화면이 스피너로 덮인다.
        if (_uiState.value !is OathUiState.Data) {
            _uiState.update { OathUiState.Loading }
        }

        viewModelScope.launch {
            challengeRepository.getChallengeDetail(
                challengeId = challengeId,
                onSuccess = { detail ->
                    _uiState.update { state -> detail.toData(state as? OathUiState.Data) }
                },
                onError = { message ->
                    if (_uiState.value is OathUiState.Data) {
                        showMessage(message)
                    } else {
                        _uiState.update { OathUiState.Error(message) }
                    }
                },
            )
        }
    }

    /** @param previous 재조회면 직전 [OathUiState.Data]. 사용자가 이미 쓴 미션·서명은 서버 응답으로 덮지 않는다. */
    private fun ChallengeDetail.toData(previous: OathUiState.Data?): OathUiState.Data {
        // 시계는 매핑 시점 1회만 읽는다. Composable 에서 읽으면 recomposition 마다 값이 흔들린다.
        val now = nowKst()

        return OathUiState.Data(
            challengeId = challengeId,
            challengerNickname = challenger.nickname,
            challengerMission = challenger.mission.orEmpty(),
            challengerSignature = contract.challengerSignature,
            betContent = betContent,
            deadlineText = deadline.toRelativeKoreanString(now),
            myMission = previous?.myMission.orEmpty(),
            signature = previous?.signature ?: Signature.Empty,
            isSubmitting = false,
            showExitConfirm = previous?.showExitConfirm ?: false,
        )
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
