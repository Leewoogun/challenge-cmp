package com.lwg.challenge.feature.challenge.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.navigation.LocalNavigateAction
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

    ChallengeDetailScreen(
        uiState = uiState,
        onBack = { navigator.popBackStack() },
        onRetry = viewModel::retry,
    )
}
