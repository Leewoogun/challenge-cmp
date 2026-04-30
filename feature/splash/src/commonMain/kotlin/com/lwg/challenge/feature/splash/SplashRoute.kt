package com.lwg.challenge.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.lwg.challenge.feature.splash.contract.SplashUiEffect
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashRoute(
    viewModel: SplashViewModel = koinViewModel(),
) {
    val navigateAction = LocalNavigateAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                SplashUiEffect.NavigateToHome -> navigateAction.switchTab(Route.HomeRoute.Main)
                SplashUiEffect.NavigateToLogin -> navigateAction.switchTab(Route.LoginRoute.Main)
            }
        }
    }

    SplashScreen()
}
