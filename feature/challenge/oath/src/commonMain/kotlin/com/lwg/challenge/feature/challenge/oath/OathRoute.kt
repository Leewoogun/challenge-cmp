package com.lwg.challenge.feature.challenge.oath

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.feature.challenge.oath.component.ExitConfirmDialog
import com.lwg.challenge.feature.challenge.oath.contract.OathModalEffect
import com.lwg.challenge.feature.challenge.oath.contract.OathUiEffect
import com.lwg.challenge.feature.challenge.oath.contract.OathUiState
import com.lwg.challenge.feature.challenge.oath.screen.OathErrorScreen
import com.lwg.challenge.feature.challenge.oath.screen.OathFormScreen
import com.lwg.challenge.feature.challenge.oath.screen.OathLoadingScreen
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.ui.components.PlatformBackHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OathRoute(
    challengeId: Long,
    viewModel: OathViewModel = koinViewModel { parametersOf(challengeId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is OathUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
                OathUiEffect.NavigateBack -> navigator.popBackStack()
            }
        }
    }

    // 시스템 백도 TopBar 뒤로가기와 같은 경로를 태운다 — 작성 중이면 확인부터 받는다.
    PlatformBackHandler(enabled = true) {
        viewModel.requestExit()
    }

    OathContent(
        uiState = uiState,
        onBack = viewModel::requestExit,
        onRetry = viewModel::retry,
        onMyMissionChange = viewModel::updateMyMission,
        onSignatureChange = viewModel::updateSignature,
        onSubmit = viewModel::submit,
    )

    // uiState 분기 밖에 둔다 — Loading / Error 상태에서도 모달을 띄울 수 있어야 한다.
    OathModalContent(
        modalEffect = modalEffect,
        onConfirmExit = viewModel::confirmExit,
        onDismiss = viewModel::dismiss,
    )
}

@Composable
private fun OathContent(
    uiState: OathUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMyMissionChange: (String) -> Unit,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            ChallengeTopBar(
                title = "영혼의 맹세",
                onBack = onBack,
            )
        },
    ) {
        when (uiState) {
            OathUiState.Loading -> OathLoadingScreen()

            is OathUiState.Error -> OathErrorScreen(
                message = uiState.message,
                onRetry = onRetry,
            )

            is OathUiState.Data -> OathFormScreen(
                uiState = uiState,
                onMyMissionChange = onMyMissionChange,
                onSignatureChange = onSignatureChange,
                onSubmit = onSubmit,
            )
        }
    }
}

@Composable
private fun OathModalContent(
    modalEffect: OathModalEffect,
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (modalEffect) {
        OathModalEffect.Hidden -> Unit

        OathModalEffect.ExitConfirm -> ExitConfirmDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismiss,
        )
    }
}
