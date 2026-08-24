package com.lwg.challenge.feature.challenge.verify.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface VerifyModalEffect {

    @Immutable
    data object Hidden : VerifyModalEffect

    /** CAMERA 가 DENIED_ALWAYS — 시스템 프롬프트가 더는 뜨지 않아 설정 앱으로 안내한다. */
    @Immutable
    data object CameraPermissionSettings : VerifyModalEffect

    /** 촬영한 사진이 있는 채 이탈 확인. 사진이 없으면 묻지 않고 바로 나간다. */
    @Immutable
    data object ExitConfirm : VerifyModalEffect
}

@Stable
sealed interface VerifyUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : VerifyUiEffect

    /** 권한 확인을 통과했다 — 화면이 들고 있는 CameraCapture 를 실행한다. */
    @Immutable
    data object LaunchCamera : VerifyUiEffect

    @Immutable
    data object NavigateBack : VerifyUiEffect
}
