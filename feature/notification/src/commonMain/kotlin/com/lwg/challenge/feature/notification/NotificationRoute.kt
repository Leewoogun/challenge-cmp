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

    NotificationBody(
        uiState = uiState,
        onBack = navigator::popBackStack,
        // 목적지는 이미 ItemState 가 확정해 들고 있다 — 화면은 타입을 다시 보지 않는다.
        onNotificationClick = navigator::navigateTo,
        onRetryClick = viewModel::retry,
        onLoadMore = viewModel::loadMore,
    )
}

/**
 * 하단 바를 그리지 않는다 — `Route.Notifications` 는 `BottomNavItem` 에 없어 `MainScreen` 이
 * 애초에 바를 그리지 않는다. 화면이 직접 그리면 탭 4개짜리 바에 활성 탭이 없는 상태가 된다.
 * 정본이 `<BottomNav />` 를 달고 있는 것은 웹 프리뷰 사정이다.
 */
@Composable
private fun NotificationBody(
    uiState: NotificationUiState,
    onBack: () -> Unit,
    onNotificationClick: (Route) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            // 탭이 아니라 벨에서 push 된 하위 화면이라 titleStyle 은 기본값(bold18)을 쓴다 —
            // 보관함·상세·생성·친구검색과 같다. 정본이 20px 인 것은 웹에서 이 페이지가 하단 바를
            // 달고 있어 탭처럼 취급됐기 때문이다.
            //
            // 뒤로가기는 정본에 없는 것을 더한 것이다. 웹에는 브라우저 뒤로가기와 하단 바가 있지만
            // 앱에는 둘 다 없어, 버튼 없이 내보내면 Android 에서 화면에 갇힌다.
            ChallengeTopBar(title = "알림", onBack = onBack)
        },
    ) {
        NotificationScreen(
            uiState = uiState,
            onNotificationClick = onNotificationClick,
            onRetryClick = onRetryClick,
            onLoadMore = onLoadMore,
        )
    }
}
