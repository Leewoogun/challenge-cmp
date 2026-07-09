package com.lwg.challenge.feature.main

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.create.ChallengeCreateRoute
import com.lwg.challenge.feature.friends.list.FriendsRoute
import com.lwg.challenge.feature.friends.search.FriendsSearchRoute
import com.lwg.challenge.feature.home.HomeRoute
import com.lwg.challenge.feature.login.LoginRoute
import com.lwg.challenge.feature.main.component.ChallengeBottomBar
import com.lwg.challenge.feature.mypage.MyPageRoute
import com.lwg.challenge.feature.ranking.RankingRoute
import com.lwg.challenge.feature.splash.SplashRoute
import com.lwg.challenge.ui.components.PlaceholderScreen
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
 * 전부 "Main" 이라 모든 화면이 같은 키로 취급된다 — ViewModelStore/SaveableState 가
 * 화면 간 공유되고, "Main" 이 백스택에 항상 존재해 pop 정리(onPop)가 영원히 발동하지 않는다.
 * qualifiedName + toString 조합으로 화면별(인자 있는 route 는 인자별) 고유 키를 만든다.
 */
private fun NavKey.uniqueContentKey(): String = "${this::class.qualifiedName}#$this"

@Composable
fun MainRoute(
    onFinishApp: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(savedStateConfiguration, Route.SplashRoute.Main)
    val navigator = remember(backStack) { MainNavigator(backStack) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }

    val mainAction = remember(snackbarHostState, scope) {
        object : MainAction {
            override fun showSnackBar(message: String) {
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }

            override fun finishApp() {
                onFinishApp()
            }
        }
    }

    val viewModel: MainViewModel = koinViewModel()

    LaunchedEffect(viewModel) {
        viewModel.sessionExpired.collect {
            navigator.switchTab(Route.LoginRoute.Main)
        }
    }

    ChallengeTheme {
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
    val showBottomBar = currentRoute !is Route.SplashRoute && currentRoute !is Route.LoginRoute

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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            entryProvider = entryProvider {
                entry<Route.SplashRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) { SplashRoute() }
                entry<Route.LoginRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) { LoginRoute() }
                entry<Route.HomeRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) { HomeRoute() }
                entry<Route.FriendsRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) { FriendsRoute() }
                entry<Route.FriendsRoute.Search>(clazzContentKey = { it.uniqueContentKey() }) { FriendsSearchRoute() }
                entry<Route.RankingRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) { RankingRoute() }
                entry<Route.MyPageRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) { MyPageRoute() }
                entry<Route.ChallengeDetailRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) { route ->
                    PlaceholderScreen(
                        title = "챌린지 상세",
                        subtitle = "id=${route.challengeId} (준비 중)",
                    )
                }
                entry<Route.ChallengeCreateRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) {
                    ChallengeCreateRoute()
                }
                entry<Route.NotificationsRoute.Main>(clazzContentKey = { it.uniqueContentKey() }) {
                    PlaceholderScreen(title = "알림")
                }
            },
        )
    }
}

