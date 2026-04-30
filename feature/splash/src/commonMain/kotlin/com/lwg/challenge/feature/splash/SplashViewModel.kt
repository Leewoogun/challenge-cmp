package com.lwg.challenge.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.GetStoredTokensUseCase
import com.lwg.challenge.domain.usecase.RefreshAccessTokenUseCase
import com.lwg.challenge.feature.splash.contract.SplashUiEffect
import com.lwg.challenge.feature.splash.contract.SplashUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/**
 * Splash ViewModel.
 *
 * 진입 시:
 * 1. 저장된 refresh token 조회.
 * 2. 비어있으면 바로 로그인 화면으로.
 * 3. 존재하면 `/auth/refresh` 시도.
 *    - 성공: 홈.
 *    - 실패 (code=401 등): 로그인.
 */
@KoinViewModel
class SplashViewModel(
    private val getStoredTokens: GetStoredTokensUseCase,
    private val refreshAccessToken: RefreshAccessTokenUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SplashUiEffect>(replay = 1)
    val uiEffect: SharedFlow<SplashUiEffect> = _uiEffect.asSharedFlow()

    init {
        decideDestination()
    }

    private fun decideDestination() {
        viewModelScope.launch {
            val tokens = getStoredTokens()
            if (tokens.refreshToken.isBlank()) {
                _uiEffect.emit(SplashUiEffect.NavigateToLogin)
                return@launch
            }

            val refreshed = refreshAccessToken { _, _ -> /* no-op, 실패 시 아래 null 로 분기 */ }
            if (refreshed != null) {
                _uiEffect.emit(SplashUiEffect.NavigateToHome)
            } else {
                _uiEffect.emit(SplashUiEffect.NavigateToLogin)
            }
        }
    }
}
