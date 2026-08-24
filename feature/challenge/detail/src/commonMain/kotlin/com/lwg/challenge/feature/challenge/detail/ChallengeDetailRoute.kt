package com.lwg.challenge.feature.challenge.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.screen.ChallengeDetailErrorScreen
import com.lwg.challenge.feature.challenge.detail.screen.ChallengeDetailLoadingScreen
import com.lwg.challenge.feature.challenge.detail.screen.ChallengeDetailScreen
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * 읽기 전용 화면이라 이탈 확인도 [com.lwg.challenge.ui.components.PlatformBackHandler] 도 걸지 않는다 —
 * 나가면서 잃을 게 없다.
 */
@Composable
fun ChallengeDetailRoute(
    challengeId: Long,
    viewModel: ChallengeDetailViewModel = koinViewModel { parametersOf(challengeId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigateAction.current

    ChallengeDetailContent(
        uiState = uiState,
        onBack = { navigator.popBackStack() },
        onRetry = viewModel::retry,
        onVerifyClick = { navigator.navigateTo(Route.Challenge.Verify(challengeId)) },
    )
}

@Composable
private fun ChallengeDetailContent(
    uiState: ChallengeDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onVerifyClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
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
            )
        }
    }
}
