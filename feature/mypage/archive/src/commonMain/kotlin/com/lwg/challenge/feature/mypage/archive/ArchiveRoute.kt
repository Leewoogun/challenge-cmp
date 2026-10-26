package com.lwg.challenge.feature.mypage.archive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiEffect
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiState
import com.lwg.challenge.feature.mypage.archive.screen.ArchiveErrorScreen
import com.lwg.challenge.feature.mypage.archive.screen.ArchiveLoadingScreen
import com.lwg.challenge.feature.mypage.archive.screen.ArchiveScreen
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArchiveRoute(
    viewModel: ArchiveViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ArchiveUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    ArchiveContent(
        uiState = uiState,
        onBack = navigator::popBackStack,
        onPreviousMonthClick = viewModel::previousMonth,
        onNextMonthClick = viewModel::nextMonth,
        onChallengeClick = { challengeId -> navigator.navigateTo(Route.Challenge.Detail(challengeId)) },
        onCreateChallengeClick = { navigator.navigateTo(Route.Challenge.Create) },
        onRetryClick = viewModel::retry,
    )
}

@Composable
private fun ArchiveContent(
    uiState: ArchiveUiState,
    onBack: () -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            ChallengeTopBar(title = "계약서 보관함", onBack = onBack)
        },
    ) {
        when (uiState) {
            ArchiveUiState.Loading -> ArchiveLoadingScreen()

            // 실패를 빈 상태로 그리면 "기록이 하나도 없다" 는 거짓이 된다.
            ArchiveUiState.Error -> ArchiveErrorScreen(onRetryClick = onRetryClick)

            is ArchiveUiState.Data -> ArchiveScreen(
                uiState = uiState,
                onPreviousMonthClick = onPreviousMonthClick,
                onNextMonthClick = onNextMonthClick,
                onChallengeClick = onChallengeClick,
                onCreateChallengeClick = onCreateChallengeClick,
            )
        }
    }
}
