package com.lwg.challenge.feature.challenge.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.feature.challenge.create.component.ChallengeCreateLoading
import com.lwg.challenge.feature.challenge.create.component.WizardProgressBar
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiEffect
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChallengeCreateRoute(
    viewModel: ChallengeCreateViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ChallengeCreateUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
                ChallengeCreateUiEffect.NavigateBack -> navigator.popBackStack()
            }
        }
    }

    ChallengeCreateContent(
        uiState = uiState,
        // 시스템 백 버튼은 단계 전환을 하지 않는다 — PlatformBackHandler 가 :feature:main 에 있어 이 모듈에서 못 쓴다.
        onBack = {
            if (uiState.step == ChallengeCreateStep.MISSION_INPUT) {
                viewModel.goToPreviousStep()
            } else {
                navigator.popBackStack()
            }
        },
        onSelectFriend = viewModel::selectFriend,
        onClickAddFriend = { navigator.navigateTo(Route.Friends.Search) },
        onRetryLoadFriends = viewModel::retryLoadFriends,
        onMyMissionChange = viewModel::updateMyMission,
        onBetContentChange = viewModel::updateBetContent,
        onDeadlineChange = viewModel::selectDeadline,
        onSubmit = viewModel::submit,
    )
}

@Composable
private fun ChallengeCreateContent(
    uiState: ChallengeCreateUiState,
    onBack: () -> Unit,
    onSelectFriend: (Long) -> Unit,
    onClickAddFriend: () -> Unit,
    onRetryLoadFriends: () -> Unit,
    onMyMissionChange: (String) -> Unit,
    onBetContentChange: (String) -> Unit,
    onDeadlineChange: (DeadlineType) -> Unit,
    onSubmit: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            ChallengeTopBar(
                title = "새 챌린지",
                onBack = onBack,
            )
            WizardProgressBar(step = uiState.step)
        },
    ) {
        when (uiState) {
            ChallengeCreateUiState.Loading -> ChallengeCreateLoading()

            is ChallengeCreateUiState.Data -> ChallengeCreateScreen(
                uiState = uiState,
                onSelectFriend = onSelectFriend,
                onClickAddFriend = onClickAddFriend,
                onRetryLoadFriends = onRetryLoadFriends,
                onMyMissionChange = onMyMissionChange,
                onBetContentChange = onBetContentChange,
                onDeadlineChange = onDeadlineChange,
                onSubmit = onSubmit,
            )
        }
    }
}
