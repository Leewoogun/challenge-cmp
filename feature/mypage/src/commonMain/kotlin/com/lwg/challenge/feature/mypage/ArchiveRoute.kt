package com.lwg.challenge.feature.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.mypage.contract.ArchiveUiEffect
import com.lwg.challenge.feature.mypage.contract.ArchiveUiState
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
        // 계약서 원문·서명·인증 사진이 전부 기존 상세에 있다. 보관함은 목록만 만든다.
        onChallengeClick = { challengeId -> navigator.navigateTo(Route.Challenge.Detail(challengeId)) },
        onCreateChallengeClick = { navigator.navigateTo(Route.Challenge.Create) },
        onRetryClick = viewModel::retry,
    )
}

@Composable
private fun ArchiveContent(
    uiState: ArchiveUiState,
    onBack: () -> Unit,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            // 탭이 아니라 하위 화면이라 titleStyle 은 기본값(bold18)을 쓴다 — 상세·생성·친구검색과 같다.
            ChallengeTopBar(title = "계약서 보관함", onBack = onBack)
        },
    ) {
        when (uiState) {
            is ArchiveUiState.Loading -> ArchiveLoadingScreen()

            // 0건과 실패는 다른 사실이라 실패를 빈 상태로 그리지 않는다 — 증거 보관 화면에서
            // "기록이 하나도 없다" 는 거짓이 가장 나쁘다 (design.md §2.5.4.1).
            is ArchiveUiState.Failure -> ArchiveFailureScreen(onRetryClick = onRetryClick)

            is ArchiveUiState.Data -> ArchiveScreen(
                uiState = uiState,
                onChallengeClick = onChallengeClick,
                onCreateChallengeClick = onCreateChallengeClick,
            )
        }
    }
}
