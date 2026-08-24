package com.lwg.challenge.feature.challenge.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.camera.CameraCaptureFailure
import com.lwg.challenge.camera.CameraCaptureResult
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.domain.repository.VerificationRepository
import com.lwg.challenge.feature.challenge.verify.contract.SubmitFailure
import com.lwg.challenge.feature.challenge.verify.contract.VerifyModalEffect
import com.lwg.challenge.feature.challenge.verify.contract.VerifyUiEffect
import com.lwg.challenge.feature.challenge.verify.contract.VerifyUiState
import com.lwg.challenge.permission.AppPermission
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.permission.PermissionStatus
import com.lwg.challenge.utils.flow.updateWithData
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

private const val NO_CAMERA_MESSAGE = "사용할 수 있는 카메라가 없어요"
private const val CAPTURE_FAILED_MESSAGE = "사진을 가져오지 못했어요. 다시 촬영해주세요"

@KoinViewModel
class VerifyViewModel(
    @InjectedParam private val challengeId: Long,
    private val verificationRepository: VerificationRepository,
    private val userInfoRepository: UserInfoRepository,
    private val permissionManager: PermissionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VerifyUiState>(VerifyUiState.Capture)
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    private val _modalEffect = MutableStateFlow<VerifyModalEffect>(VerifyModalEffect.Hidden)
    val modalEffect: StateFlow<VerifyModalEffect> = _modalEffect.asStateFlow()

    private val _uiEffect = MutableSharedFlow<VerifyUiEffect>()
    val uiEffect: SharedFlow<VerifyUiEffect> = _uiEffect.asSharedFlow()

    /** 촬영/재촬영 진입점. CAMERA 권한을 확보한 뒤에만 [VerifyUiEffect.LaunchCamera] 를 낸다. */
    fun requestCapture() {
        _uiState.withData<VerifyUiState.Confirm> { confirm ->
            if (confirm.isSubmitting) return
        }

        viewModelScope.launch {
            when (permissionManager.status(AppPermission.CAMERA)) {
                PermissionStatus.GRANTED -> launchCamera()
                PermissionStatus.DENIED -> requestCameraPermission()
                PermissionStatus.DENIED_ALWAYS -> showPermissionSettingsGuide()
            }
        }
    }

    fun completeCapture(result: CameraCaptureResult) {
        when (result) {
            is CameraCaptureResult.Success -> {
                _uiState.update {
                    VerifyUiState.Confirm(
                        photo = result.photo,
                        isSubmitting = false,
                        failure = null,
                    )
                }
            }

            CameraCaptureResult.Canceled -> Unit

            is CameraCaptureResult.Failed -> showMessage(result.reason.toMessage())
        }
    }

    fun submit() {
        _uiState.withData<VerifyUiState.Confirm> { confirm ->
            if (!confirm.canSubmit) return

            _uiState.updateWithData<VerifyUiState, VerifyUiState.Confirm> {
                it.copy(isSubmitting = true, failure = null)
            }

            viewModelScope.launch {
                when (val outcome = verificationRepository.submitVerification(challengeId, confirm.photo.jpegBytes)) {
                    is VerificationSubmitOutcome.Success -> _uiState.update { VerifyUiState.Done }

                    // 재제출이 전면 거부라 같은 사진으로 다시 눌러도 결과가 같다. 형식·용량 거부도
                    // 같은 사진에서는 반복된다 — 열어 둘 길은 재촬영뿐이다.
                    is VerificationSubmitOutcome.Rejected ->
                        failSubmit(SubmitFailure(outcome.message, isRetryable = false))

                    is VerificationSubmitOutcome.Unreachable -> confirmSubmission(outcome.message)
                }
            }
        }
    }

    fun openPermissionSettings() {
        permissionManager.openAppSettings()
        dismiss()
    }

    /** 제출 중에는 결과를 모른 채 화면을 잃게 되므로 이탈 자체를 막는다. */
    fun requestExit() {
        _uiState.withData<VerifyUiState.Confirm> { confirm ->
            if (confirm.isSubmitting) return

            _modalEffect.update { VerifyModalEffect.ExitConfirm }
            return
        }

        navigateBack()
    }

    fun confirmExit() {
        dismiss()
        navigateBack()
    }

    fun complete() {
        navigateBack()
    }

    fun dismiss() {
        _modalEffect.update { VerifyModalEffect.Hidden }
    }

    private suspend fun requestCameraPermission() {
        when (permissionManager.request(AppPermission.CAMERA)) {
            PermissionStatus.GRANTED -> launchCamera()

            // 방금 시스템 프롬프트에서 거절한 사람에게 곧바로 설정 안내를 얹지 않는다 —
            // 버튼을 다시 누르면 재요청된다.
            PermissionStatus.DENIED -> Unit

            PermissionStatus.DENIED_ALWAYS -> showPermissionSettingsGuide()
        }
    }

    private fun showPermissionSettingsGuide() {
        _modalEffect.update { VerifyModalEffect.CameraPermissionSettings }
    }

    /**
     * 전송이 끊긴 제출을 실제 인증 현황 조회로 판정한다 — 계약이 요구하는 절차다.
     * 전송이 끊기면 서버가 반영했는지 알 수 없고, 재제출은 전면 거부라 맹목 재시도가
     * *이미 성공한 인증*을 실패로 보이게 만든다. 그래서 재시도를 열기 전에 조회해서 판정한다.
     *
     * 에러 문구로 성공을 판정하지 않는다 — 같은 거부 문구가 '진짜로 이미 인증한 상태'에서도
     * 나오므로 문구 매칭은 틀린다.
     */
    private suspend fun confirmSubmission(fallbackMessage: String) {
        val myUserId = myUserId()
        if (myUserId == null) {
            failSubmit(SubmitFailure(fallbackMessage, isRetryable = true))
            return
        }

        verificationRepository.getVerifications(
            challengeId = challengeId,
            onSuccess = { verifications ->
                if (verifications.partyOf(myUserId)?.status == VerificationStatus.VERIFIED) {
                    _uiState.update { VerifyUiState.Done }
                } else {
                    failSubmit(SubmitFailure(fallbackMessage, isRetryable = true))
                }
            },
            onError = { failSubmit(SubmitFailure(fallbackMessage, isRetryable = true)) },
        )
    }

    private suspend fun myUserId(): Long? =
        userInfoRepository
            .getUserInfo(onError = {}, cacheStrategy = CacheStrategy.CACHE_FIRST)
            .firstOrNull()
            ?.id

    private fun failSubmit(failure: SubmitFailure) {
        _uiState.updateWithData<VerifyUiState, VerifyUiState.Confirm> {
            it.copy(isSubmitting = false, failure = failure)
        }
    }

    private fun launchCamera() {
        viewModelScope.launch {
            _uiEffect.emit(VerifyUiEffect.LaunchCamera)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiEffect.emit(VerifyUiEffect.NavigateBack)
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(VerifyUiEffect.ShowMessage(message))
        }
    }

    private fun CameraCaptureFailure.toMessage(): String = when (this) {
        CameraCaptureFailure.NO_CAMERA -> NO_CAMERA_MESSAGE
        CameraCaptureFailure.DECODE_FAILED, CameraCaptureFailure.IO -> CAPTURE_FAILED_MESSAGE
    }
}
