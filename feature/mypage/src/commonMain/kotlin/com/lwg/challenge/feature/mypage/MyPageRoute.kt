package com.lwg.challenge.feature.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.feature.mypage.contract.MyPageUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPageContent(
        uiState = uiState,
    )
}

@Composable
private fun MyPageContent(
    uiState: MyPageUiState,
) {
    when (uiState) {
        is MyPageUiState.Loading,
        is MyPageUiState.Data -> {
            MyPageScreen()
        }
    }
}
