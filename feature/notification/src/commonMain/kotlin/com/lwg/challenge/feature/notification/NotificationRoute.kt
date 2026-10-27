package com.lwg.challenge.feature.notification

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.lwg.challenge.ui.components.PaginationEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationRoute(
    viewModel: NotificationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is NotificationUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
            }
        }
    }

    NotificationContent(
        uiState = uiState,
        listState = listState,
        onBack = navigator::popBackStack,
        onNotificationClick = navigator::navigateTo,
        onRetryClick = viewModel::retry,
    )

    PaginationEffect(
        listState = listState,
        onLoadNextPage = viewModel::nextPage,
    )
}

// Route.Notifications 는 BottomNavItem 에 없어 MainScreen 이 하단 바를 그리지 않는다.
@Composable
private fun NotificationContent(
    uiState: NotificationUiState,
    listState: LazyListState,
    onBack: () -> Unit,
    onNotificationClick: (Route) -> Unit,
    onRetryClick: () -> Unit,
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
                listState = listState,
                onNotificationClick = onNotificationClick,
            )

            NotificationUiState.Empty -> NotificationEmptyScreen()

            NotificationUiState.Error -> NotificationErrorScreen(onRetryClick = onRetryClick)
        }
    }
}
