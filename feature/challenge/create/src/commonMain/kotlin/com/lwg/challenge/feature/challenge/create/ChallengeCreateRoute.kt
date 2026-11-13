package com.lwg.challenge.feature.challenge.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.feature.challenge.create.component.ChallengeCreateLoading
import com.lwg.challenge.feature.challenge.create.component.WizardProgressBar
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiEffect
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.feature.challenge.create.screen.FriendPickScreen
import com.lwg.challenge.feature.challenge.create.screen.MissionInputScreen
import com.lwg.challenge.feature.challenge.create.screen.OathScreen
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.ui.components.PlatformBackHandler
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

    val onBack = {
        if (uiState.step != ChallengeCreateStep.FRIEND_PICK) {
            viewModel.goToPreviousStep()
        } else {
            navigator.popBackStack()
        }
    }

    PlatformBackHandler(enabled = true, onBack = onBack)

    ChallengeCreateContent(
        uiState = uiState,
        onBack = onBack,
        onSelectFriend = viewModel::selectFriend,
        onClickAddFriend = { navigator.navigateTo(Route.Friends.Search) },
        onRetryLoadFriends = viewModel::retryLoadFriends,
        onMyMissionChange = viewModel::updateMyMission,
        onPenaltyChange = viewModel::updatePenalty,
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
    onSelectFriend: (Friend) -> Unit,
    onClickAddFriend: () -> Unit,
    onRetryLoadFriends: () -> Unit,
    onMyMissionChange: (String) -> Unit,
    onPenaltyChange: (String) -> Unit,
    onDeadlineChange: (DeadlineType) -> Unit,
    onNext: () -> Unit,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.background.card,
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

            is ChallengeCreateUiState.FriendPick -> FriendPickScreen(
                uiState = uiState,
                onSelectFriend = onSelectFriend,
                onClickAddFriend = onClickAddFriend,
                onRetryLoadFriends = onRetryLoadFriends,
            )

            is ChallengeCreateUiState.MissionInput -> MissionInputScreen(
                uiState = uiState,
                onMyMissionChange = onMyMissionChange,
                onPenaltyChange = onPenaltyChange,
                onDeadlineChange = onDeadlineChange,
                onNext = onNext,
            )

            is ChallengeCreateUiState.Oath -> OathScreen(
                uiState = uiState,
                onSignatureChange = onSignatureChange,
                onSubmit = onSubmit,
            )
        }
    }
}
