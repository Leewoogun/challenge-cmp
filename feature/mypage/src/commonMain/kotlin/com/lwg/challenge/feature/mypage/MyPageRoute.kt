package com.lwg.challenge.feature.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.mypage.contract.MyPageUiState
import com.lwg.challenge.navigation.LocalMainAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current

    MyPageContent(
        uiState = uiState,
        onLogoutClick = mainAction::logout,
    )
}

@Composable
private fun MyPageContent(
    uiState: MyPageUiState,
    onLogoutClick: () -> Unit,
) {
    when (uiState) {
        is MyPageUiState.Loading,
        is MyPageUiState.Data -> {
            MyPageScreen(onLogoutClick = onLogoutClick)
        }
    }
}
