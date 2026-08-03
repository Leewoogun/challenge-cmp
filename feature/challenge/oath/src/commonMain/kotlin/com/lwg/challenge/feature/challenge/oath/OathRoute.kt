package com.lwg.challenge.feature.challenge.oath

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.challenge.oath.contract.OathUiEffect
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

    OathScreen(
        uiState = uiState,
        onBack = viewModel::requestExit,
        onRetry = viewModel::retry,
        onMyMissionChange = viewModel::updateMyMission,
        onSignatureChange = viewModel::updateSignature,
        onSubmit = viewModel::submit,
        onConfirmExit = viewModel::confirmExit,
        onDismissExitConfirm = viewModel::dismissExitConfirm,
    )
}
