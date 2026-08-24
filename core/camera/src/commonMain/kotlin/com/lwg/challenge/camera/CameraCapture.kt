package com.lwg.challenge.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

sealed interface CameraCaptureResult {

    class Success(val photo: CapturedPhoto) : CameraCaptureResult

    /** 사용자가 촬영을 취소했다. 오류가 아니므로 호출부는 조용히 되돌아간다. */
    data object Canceled : CameraCaptureResult

    data class Failed(val reason: CameraCaptureFailure) : CameraCaptureResult
}

enum class CameraCaptureFailure {

    /** 카메라 하드웨어 또는 카메라 앱이 없다. iOS 시뮬레이터가 여기 걸린다. */
    NO_CAMERA,

    DECODE_FAILED,

    IO,
}

@Stable
interface CameraCapture {

    fun capture()
}

/**
 * 촬영기를 기억한다. 갤러리 진입점은 없다 — 기획서 §2.5 가 실시간 촬영만 허용한다.
 *
 * 권한 요청은 하지 않는다 — 권한 UX 결정(재요청·설정 이동 안내)이 이 모듈로 새어 들어오지 않도록,
 * 호출부가 `:core:permission` 의 PermissionManager 로 CAMERA 권한을 얻은 뒤 [CameraCapture.capture] 를 부른다.
 */
@Composable
expect fun rememberCameraCapture(
    spec: CompressionSpec,
    onResult: (CameraCaptureResult) -> Unit,
): CameraCapture
