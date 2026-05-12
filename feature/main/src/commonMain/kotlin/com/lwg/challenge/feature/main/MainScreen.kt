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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.FriendsRoute
import com.lwg.challenge.feature.home.HomeRoute
import com.lwg.challenge.feature.login.LoginRoute
import com.lwg.challenge.feature.main.component.ChallengeBottomBar
import com.lwg.challenge.feature.mypage.MyPageRoute
import com.lwg.challenge.feature.ranking.RankingRoute
import com.lwg.challenge.feature.splash.SplashRoute
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.MainAction
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.navigation.routeSerializersModule
import kotlinx.coroutines.launch

private val savedStateConfiguration = SavedStateConfiguration {
    serializersModule = routeSerializersModule
}

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
            entryProvider = { route ->
                when (route) {
                    is Route.SplashRoute -> NavEntry(route) { SplashRoute() }
                    is Route.LoginRoute -> NavEntry(route) { LoginRoute() }
                    is Route.HomeRoute -> NavEntry(route) { HomeRoute() }
                    is Route.FriendsRoute -> NavEntry(route) { FriendsRoute() }
                    is Route.RankingRoute -> NavEntry(route) { RankingRoute() }
                    is Route.MyPageRoute -> NavEntry(route) { MyPageRoute() }
                    else -> NavEntry(route) {}
                }
            },
        )
    }
}

