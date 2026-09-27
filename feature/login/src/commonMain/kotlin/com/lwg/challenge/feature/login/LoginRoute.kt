package com.lwg.challenge.feature.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
import com.lwg.challenge.feature.login.contract.TestAccount
import com.lwg.challenge.feature.login.contract.TestLoginUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val testLoginState by viewModel.testLoginState.collectAsStateWithLifecycle()
    val navigateAction = LocalNavigateAction.current
    val mainAction = LocalMainAction.current

    LoginContent(
        uiState = uiState,
        testLoginState = testLoginState,
        onLoginClick = viewModel::login,
        onTestAccountClick = viewModel::loginWithTestAccount,
    )

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LoginUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
                is LoginUiEffect.NavigateToHome -> {
                    navigateAction.switchTab(Route.Home)
                }
            }
        }
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    testLoginState: TestLoginUiState,
    onLoginClick: () -> Unit,
    onTestAccountClick: (TestAccount) -> Unit,
) {
    LoginScreen(
        uiState = uiState,
        testLoginState = testLoginState,
        onLoginClick = onLoginClick,
        onTestAccountClick = onTestAccountClick,
    )
}
