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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SplashViewModel(
    private val getStoredTokensUseCase: GetStoredTokensUseCase,
    private val refreshAccessTokenUseCase: RefreshAccessTokenUseCase,
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
            val tokens = getStoredTokensUseCase()
            if (tokens.refreshToken.isBlank()) {
                _uiEffect.emit(SplashUiEffect.NavigateToLogin)
                return@launch
            }

            val refreshed = refreshAccessTokenUseCase(onError = { /* 로그인으로 분기 */ })
                .firstOrNull()
            _uiEffect.emit(
                if (refreshed != null) SplashUiEffect.NavigateToHome else SplashUiEffect.NavigateToLogin,
            )
        }
    }
}
