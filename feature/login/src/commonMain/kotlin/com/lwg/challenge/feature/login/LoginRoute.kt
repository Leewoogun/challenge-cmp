package com.lwg.challenge.feature.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateAction = LocalNavigateAction.current
    val mainAction = LocalMainAction.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LoginUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
                is LoginUiEffect.NavigateToHome -> {
                    // TODO: isNewUser=true 일 때 온보딩 화면으로 (별도 feature). 지금은 홈으로.
                    navigateAction.switchTab(Route.HomeRoute.Main)
                }
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onLoginClick = viewModel::login,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onLoginClick: () -> Unit,
) {
    LoginScreen(
        uiState = uiState,
        onLoginClick = onLoginClick,
    )
}
