package com.lwg.challenge.feature.challenge.verify.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.camera.CapturedPhoto

@Stable
sealed interface VerifyUiState {

    /** 촬영 전 안내. 서버 조회가 없는 화면이라 Loading 없이 여기서 시작한다. */
    @Immutable
    data object Capture : VerifyUiState

    /**
     * 촬영한 사진의 미리보기 + 제출 확인.
     *
     * 제출한 사진이 곧바로 상대에게 보이므로 이 단계를 건너뛰지 않는다 —
     * 사용자가 사진을 보고 재촬영할 기회를 가진 뒤에만 제출할 수 있다.
     */
    @Immutable
    data class Confirm(
        val photo: CapturedPhoto,
        val isSubmitting: Boolean,
        val failure: SubmitFailure?,
    ) : VerifyUiState {

        val hasFailure: Boolean get() = failure != null

        val failureMessage: String get() = failure?.message.orEmpty()

        val canSubmit: Boolean get() = !isSubmitting && failure?.isRetryable != false

        val canRetake: Boolean get() = !isSubmitting

        val submitButtonText: String get() = if (hasFailure) "다시 시도" else "이 사진으로 인증"

        val photoAspectRatio: Float
            get() = if (photo.height == 0) 1f else photo.width.toFloat() / photo.height
    }

    /** 제출 확정 완료. */
    @Immutable
    data object Done : VerifyUiState
}

/**
 * 제출 파이프라인 실패 1건.
 *
 * @property isRetryable false 면 같은 사진으로 다시 시도해도 소용없다(예: 크기 초과) —
 *   제출 버튼을 막고 재촬영만 열어 둔다
 */
@Immutable
data class SubmitFailure(
    val message: String,
    val isRetryable: Boolean,
)
