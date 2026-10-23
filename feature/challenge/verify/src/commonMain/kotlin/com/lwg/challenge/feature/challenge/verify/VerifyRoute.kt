package com.lwg.challenge.feature.challenge.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.camera.CompressionSpec
import com.lwg.challenge.camera.rememberCameraCapture
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.verify.component.CameraPermissionDialog
import com.lwg.challenge.feature.challenge.verify.component.ExitConfirmDialog
import com.lwg.challenge.feature.challenge.verify.contract.VerifyModalEffect
import com.lwg.challenge.feature.challenge.verify.contract.VerifyUiEffect
import com.lwg.challenge.feature.challenge.verify.contract.VerifyUiState
import com.lwg.challenge.feature.challenge.verify.screen.VerifyCaptureScreen
import com.lwg.challenge.feature.challenge.verify.screen.VerifyConfirmScreen
import com.lwg.challenge.feature.challenge.verify.screen.VerifyDoneScreen
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.ui.components.PlatformBackHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun VerifyRoute(
    challengeId: Long,
    isReplace: Boolean,
    viewModel: VerifyViewModel = koinViewModel { parametersOf(challengeId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    val cameraCapture = rememberCameraCapture(
        spec = CompressionSpec.VERIFICATION,
        onResult = viewModel::completeCapture,
    )

    LaunchedEffect(viewModel, cameraCapture) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is VerifyUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
                VerifyUiEffect.LaunchCamera -> cameraCapture.capture()
                VerifyUiEffect.NavigateBack -> navigator.popBackStack()
            }
        }
    }

    // 시스템 백도 TopBar 뒤로가기와 같은 경로를 태운다 — 촬영한 사진이 있으면 확인부터 받는다.
    PlatformBackHandler(enabled = true) {
        viewModel.requestExit()
    }

    VerifyContent(
        uiState = uiState,
        isReplace = isReplace,
        onBack = viewModel::requestExit,
        onCaptureClick = viewModel::requestCapture,
        onRetakeClick = viewModel::requestCapture,
        onSubmitClick = viewModel::submit,
        onDoneClick = viewModel::complete,
    )

    // uiState 분기 밖에 둔다 — 어느 단계에서도 모달을 띄울 수 있어야 한다.
    VerifyModalContent(
        modalEffect = modalEffect,
        onOpenSettings = viewModel::openPermissionSettings,
        onConfirmExit = viewModel::confirmExit,
        onDismiss = viewModel::dismiss,
    )
}

@Composable
private fun VerifyContent(
    uiState: VerifyUiState,
    isReplace: Boolean,
    onBack: () -> Unit,
    onCaptureClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onDoneClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            ChallengeTopBar(
                title = "챌린지 인증",
                onBack = onBack,
            )
        },
    ) {
        when (uiState) {
            VerifyUiState.Capture -> VerifyCaptureScreen(
                onCaptureClick = onCaptureClick,
            )

            is VerifyUiState.Confirm -> VerifyConfirmScreen(
                uiState = uiState,
                onRetakeClick = onRetakeClick,
                onSubmitClick = onSubmitClick,
            )

            VerifyUiState.Done -> VerifyDoneScreen(
                isReplace = isReplace,
                onDoneClick = onDoneClick,
            )
        }
    }
}

@Composable
private fun VerifyModalContent(
    modalEffect: VerifyModalEffect,
    onOpenSettings: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (modalEffect) {
        VerifyModalEffect.Hidden -> Unit

        VerifyModalEffect.CameraPermissionSettings -> CameraPermissionDialog(
            onOpenSettings = onOpenSettings,
            onDismiss = onDismiss,
        )

        VerifyModalEffect.ExitConfirm -> ExitConfirmDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismiss,
        )
    }
}
