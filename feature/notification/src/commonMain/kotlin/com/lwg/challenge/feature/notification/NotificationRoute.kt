package com.lwg.challenge.feature.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.notification.contract.NotificationUiEffect
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.feature.notification.screen.NotificationEmptyScreen
import com.lwg.challenge.feature.notification.screen.NotificationErrorScreen
import com.lwg.challenge.feature.notification.screen.NotificationLoadingScreen
import com.lwg.challenge.feature.notification.screen.NotificationScreen
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationRoute(
    viewModel: NotificationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is NotificationUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    // 상세에 갔다 오면 이 블록이 다시 돈다. VM 은 백스택에 살아 있어 스스로 갱신하지 못한다.
    LaunchedEffect(Unit) { viewModel.refresh() }

    NotificationContent(
        uiState = uiState,
        onBack = navigator::popBackStack,
        onNotificationClick = navigator::navigateTo,
        onRetryClick = viewModel::retry,
        onLoadMore = viewModel::loadMore,
    )
}

// Route.Notifications 는 BottomNavItem 에 없어 MainScreen 이 하단 바를 그리지 않는다.
@Composable
private fun NotificationContent(
    uiState: NotificationUiState,
    onBack: () -> Unit,
    onNotificationClick: (Route) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        // 웹 정본엔 뒤로가기가 없지만 앱엔 브라우저 뒤로가기·하단 바가 없어 버튼이 없으면 화면에 갇힌다.
        topBar = { ChallengeTopBar(title = "알림", onBack = onBack) },
    ) {
        when (uiState) {
            NotificationUiState.Loading -> NotificationLoadingScreen()

            is NotificationUiState.Data -> NotificationScreen(
                uiState = uiState,
                onNotificationClick = onNotificationClick,
                onLoadMore = onLoadMore,
            )

            NotificationUiState.Empty -> NotificationEmptyScreen()

            NotificationUiState.Error -> NotificationErrorScreen(onRetryClick = onRetryClick)
        }
    }
}
