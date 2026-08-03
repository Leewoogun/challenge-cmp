package com.lwg.challenge.feature.challenge.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.signature.Signature
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
        // 단계 되돌리기는 TopBar 뒤로가기만 한다. PlatformBackHandler 가 :core:ui 로 옮겨져 이제 쓸 수는
        // 있으나, 위저드의 시스템 백 동작을 맞추는 건 별도 작업이라 여기서는 손대지 않는다.
        onBack = {
            if (uiState.step != ChallengeCreateStep.FRIEND_PICK) {
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
        onNext = viewModel::goToOathStep,
        onSignatureChange = viewModel::updateSignature,
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
    onNext: () -> Unit,
    onSignatureChange: (Signature) -> Unit,
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
                onNext = onNext,
                onSignatureChange = onSignatureChange,
                onSubmit = onSubmit,
            )
        }
    }
}
