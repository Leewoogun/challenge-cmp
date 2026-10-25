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

    ArchiveBody(
        uiState = uiState,
        onBack = navigator::popBackStack,
        onPreviousMonthClick = viewModel::onPreviousMonthClick,
        onNextMonthClick = viewModel::onNextMonthClick,
        // 계약서 원문·서명·인증 사진이 전부 기존 상세에 있다. 보관함은 목록만 만든다.
        onChallengeClick = { challengeId -> navigator.navigateTo(Route.Challenge.Detail(challengeId)) },
        onCreateChallengeClick = { navigator.navigateTo(Route.Challenge.Create) },
        onRetryClick = viewModel::retry,
    )
}

/**
 * 상태 sealed interface 인 `ArchiveContent` 와 이름이 겹쳐, 1차의 `ArchiveContent` 컴포저블을
 * `ArchiveBody` 로 바꿨다.
 */
@Composable
private fun ArchiveBody(
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
            // 탭이 아니라 하위 화면이라 titleStyle 은 기본값(bold18)을 쓴다 — 상세·생성·친구검색과 같다.
            ChallengeTopBar(title = "계약서 보관함", onBack = onBack)
        },
    ) {
        ArchiveScreen(
            uiState = uiState,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick,
            onChallengeClick = onChallengeClick,
            onCreateChallengeClick = onCreateChallengeClick,
            onRetryClick = onRetryClick,
        )
    }
}
