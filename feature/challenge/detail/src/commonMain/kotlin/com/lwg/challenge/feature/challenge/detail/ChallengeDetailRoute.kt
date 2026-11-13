package com.lwg.challenge.feature.challenge.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailModalEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_CONFIRM_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_DIALOG_MESSAGE
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_DIALOG_TITLE
import com.lwg.challenge.feature.challenge.detail.screen.ChallengeDetailErrorScreen
import com.lwg.challenge.feature.challenge.detail.screen.ChallengeDetailLoadingScreen
import com.lwg.challenge.feature.challenge.detail.screen.ChallengeDetailScreen
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.ui.components.ConfirmDialog
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChallengeDetailRoute(
    challengeId: Long,
    viewModel: ChallengeDetailViewModel = koinViewModel { parametersOf(challengeId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    // 인증 화면에 다시 물어보게 하지 않는다 — 교체 진입인지는 이 화면이 이미 들고 있는 값이다.
    val isReplace = (uiState as? ChallengeDetailUiState.Data)?.isMyVerified == true

    ChallengeDetailContent(
        uiState = uiState,
        onBack = { navigator.popBackStack() },
        onRetry = viewModel::retry,
        onVerifyClick = { navigator.navigateTo(Route.Challenge.Verify(challengeId, isReplace)) },
        onForfeitClick = viewModel::requestForfeit,
    )

    ChallengeDetailModalContent(
        modalEffect = modalEffect,
        onConfirmForfeit = viewModel::forfeit,
        onDismiss = viewModel::dismiss,
    )

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ChallengeDetailUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }
}

@Composable
private fun ChallengeDetailContent(
    uiState: ChallengeDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onVerifyClick: () -> Unit,
    onForfeitClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.background.card,
        topBar = {
            ChallengeTopBar(
                title = "챌린지 상세",
                onBack = onBack,
            )
        },
    ) {
        when (uiState) {
            ChallengeDetailUiState.Loading -> ChallengeDetailLoadingScreen()

            is ChallengeDetailUiState.Error -> ChallengeDetailErrorScreen(
                message = uiState.message,
                onRetry = onRetry,
            )

            is ChallengeDetailUiState.Data -> ChallengeDetailScreen(
                uiState = uiState,
                onVerifyClick = onVerifyClick,
                onForfeitClick = onForfeitClick,
                onRetry = onRetry,
            )
        }
    }
}

/** Data 인지 보지 않고 [modalEffect] 만 따른다 — 다이얼로그를 띄운 주체가 이미 상태를 판정했다. */
@Composable
private fun ChallengeDetailModalContent(
    modalEffect: ChallengeDetailModalEffect,
    onConfirmForfeit: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (modalEffect) {
        ChallengeDetailModalEffect.Hidden -> Unit

        ChallengeDetailModalEffect.ConfirmForfeit -> ConfirmDialog(
            title = FORFEIT_DIALOG_TITLE,
            message = FORFEIT_DIALOG_MESSAGE,
            confirmLabel = FORFEIT_CONFIRM_LABEL,
            onConfirm = onConfirmForfeit,
            onDismiss = onDismiss,
            destructive = true,
        )
    }
}
