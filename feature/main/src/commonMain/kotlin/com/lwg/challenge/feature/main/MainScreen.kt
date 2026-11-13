package com.lwg.challenge.feature.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.main.contract.MainUiEffect
import com.lwg.challenge.feature.challenge.create.ChallengeCreateRoute
import com.lwg.challenge.feature.challenge.detail.ChallengeDetailRoute
import com.lwg.challenge.feature.challenge.oath.OathRoute
import com.lwg.challenge.feature.challenge.verify.VerifyRoute
import com.lwg.challenge.feature.friends.list.FriendsRoute
import com.lwg.challenge.feature.friends.search.FriendsSearchRoute
import com.lwg.challenge.feature.home.HomeRoute
import com.lwg.challenge.feature.login.LoginRoute
import com.lwg.challenge.feature.main.component.BottomNavItem
import com.lwg.challenge.feature.main.component.ChallengeBottomBar
import com.lwg.challenge.feature.mypage.archive.ArchiveRoute
import com.lwg.challenge.feature.mypage.mypage.MyPageRoute
import com.lwg.challenge.feature.notification.NotificationRoute
import com.lwg.challenge.feature.ranking.RankingRoute
import com.lwg.challenge.feature.splash.SplashRoute
import com.lwg.challenge.ui.components.PlatformBackHandler
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.MainAction
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.navigation.routeSerializersModule
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val savedStateConfiguration = SavedStateConfiguration {
    serializersModule = routeSerializersModule
}

/**
 * NavEntry 의 contentKey 로 쓸 route 고유 키.
 *
 * nav3 기본 contentKey 는 `key.toString()` 인데, 중첩 `data object` route 는 toString 이
 * 단순 이름만 반환해 서로 다른 화면이 같은 키로 충돌할 수 있다(과거 전부 "Main" 이던 시절
 * 전 화면이 ViewModelStore 를 공유하고 pop 정리가 발동하지 않는 버그의 원인이었음).
 * qualifiedName + toString 조합으로 화면별(인자 있는 route 는 인자별) 고유 키를 만든다.
 * 새 entry 추가 시 반드시 clazzContentKey 로 지정할 것.
 */
private fun NavKey.uniqueContentKey(): String = "${this::class.qualifiedName}#$this"

@Composable
fun MainRoute(
    onFinishApp: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(savedStateConfiguration, Route.Splash)
    val navigator = remember(backStack) { MainNavigator(backStack) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }

    val viewModel: MainViewModel = koinViewModel()

    val mainAction = remember(snackbarHostState, scope, viewModel) {
        object : MainAction {
            override fun showSnackBar(message: String) {
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }

            override fun finishApp() {
                onFinishApp()
            }

            override fun logout() {
                viewModel.logout()
            }

            override fun withdrawn() {
                viewModel.withdrawn()
            }
        }
    }

    // Splash/Login 을 벗어난 순간이 곧 인증 영역 진입이다. 자동 로그인 경로도 같은 지점을 지난다.
    // 알림 권한은 이때 묻는다 — 로그인 전에 물으면 왜 필요한지 알 수 없는 시점에 뜬다.
    val isAuthenticatedArea = backStack.lastOrNull().let {
        it != null && it !is Route.Splash && it !is Route.Login
    }

    LaunchedEffect(isAuthenticatedArea) {
        viewModel.setAuthenticatedArea(isAuthenticatedArea)
        if (isAuthenticatedArea) viewModel.requestNotificationPermission()
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                MainUiEffect.NavigateToLogin -> navigator.switchTab(Route.Login)

                is MainUiEffect.NavigateToPush -> {
                    val route = effect.route
                    if (backStack.lastOrNull() == route) return@collect

                    // 탭 루트가 목적지면 백스택을 정리한다 — 뒤로가기가 알림 전 화면으로 돌아가면 혼란스럽다.
                    if (BottomNavItem.find(route) != null) navigator.switchTab(route)
                    else navigator.navigateTo(route)
                }
            }
        }
    }

    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    ChallengeTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(
            LocalNavigateAction provides navigator,
            LocalMainAction provides mainAction,
        ) {
            MainScreen(
                backStack = backStack,
                navigator = navigator,
                snackbarHostState = snackbarHostState,
                onBackPressed = {
                    if (backPressedOnce) {
                        mainAction.finishApp()
                    } else {
                        backPressedOnce = true
                        scope.launch {
                            snackbarHostState.showSnackbar("뒤로 버튼을 한번 더 누르면 종료됩니다.")
                            backPressedOnce = false
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun MainScreen(
    backStack: NavBackStack<NavKey>,
    navigator: MainNavigator,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit,
) {
    val currentRoute = backStack.lastOrNull()
    val showBottomBar = BottomNavItem.find(currentRoute) != null

    PlatformBackHandler(enabled = backStack.size <= 1) {
        onBackPressed()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
            )
        },
        bottomBar = {
            if (showBottomBar) {
                ChallengeBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = navigator::switchTab,
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            onBack = {
                if (backStack.size > 1) {
                    navigator.popBackStack()
                } else {
                    onBackPressed()
                }
            },
            // 바텀바가 있으면 innerPadding 이 하단 시스템 영역까지 포함하므로 consume 해서
            // 하위 ChallengeScaffold 가 같은 인셋을 이중으로 적용하지 않게 한다.
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            entryProvider = entryProvider {
                entry<Route.Splash>(clazzContentKey = { it.uniqueContentKey() }) { SplashRoute() }
                entry<Route.Login>(clazzContentKey = { it.uniqueContentKey() }) { LoginRoute() }
                entry<Route.Home>(clazzContentKey = { it.uniqueContentKey() }) { HomeRoute() }
                entry<Route.Friends.List>(clazzContentKey = { it.uniqueContentKey() }) { FriendsRoute() }
                entry<Route.Friends.Search>(clazzContentKey = { it.uniqueContentKey() }) { FriendsSearchRoute() }
                entry<Route.Ranking>(clazzContentKey = { it.uniqueContentKey() }) { RankingRoute() }
                entry<Route.MyPage.Main>(clazzContentKey = { it.uniqueContentKey() }) { MyPageRoute() }
                entry<Route.MyPage.Archive>(clazzContentKey = { it.uniqueContentKey() }) { ArchiveRoute() }
                entry<Route.Challenge.Detail>(clazzContentKey = { it.uniqueContentKey() }) { route ->
                    ChallengeDetailRoute(challengeId = route.challengeId)
                }
                entry<Route.Challenge.Create>(clazzContentKey = { it.uniqueContentKey() }) {
                    ChallengeCreateRoute()
                }
                entry<Route.Challenge.Oath>(clazzContentKey = { it.uniqueContentKey() }) { route ->
                    OathRoute(challengeId = route.challengeId)
                }
                entry<Route.Challenge.Verify>(clazzContentKey = { it.uniqueContentKey() }) { route ->
                    VerifyRoute(challengeId = route.challengeId, isReplace = route.isReplace)
                }
                entry<Route.Notifications>(clazzContentKey = { it.uniqueContentKey() }) {
                    NotificationRoute()
                }
            },
        )
    }
}

