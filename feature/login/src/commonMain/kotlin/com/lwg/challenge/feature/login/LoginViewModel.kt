package com.lwg.challenge.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
import com.lwg.challenge.feature.login.kakao.KakaoAuthClient
import com.lwg.challenge.feature.login.kakao.KakaoAuthResult
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val kakaoAuthClient: KakaoAuthClient,
    private val loginWithKakaoUseCase: LoginWithKakaoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LoginUiEffect>()
    val uiEffect: SharedFlow<LoginUiEffect> = _uiEffect.asSharedFlow()

    fun login() {
        _uiState.withData<LoginUiState.Loading> { return }

        viewModelScope.launch {
            _uiState.update { LoginUiState.Loading }

            when (val authResult = kakaoAuthClient.requestAccessToken()) {
                is KakaoAuthResult.Cancelled -> {
                    // 사용자가 동의 화면에서 취소 — 조용히 Idle 복귀.
                }

                is KakaoAuthResult.Failure -> {
                    _uiEffect.emit(LoginUiEffect.ShowMessage(authResult.message))
                }

                is KakaoAuthResult.Success -> {
                    loginWithKakaoUseCase(
                        kakaoAccessToken = authResult.accessToken,
                        onError = ::showMessage,
                    ).collect { result ->
                        _uiEffect.emit(LoginUiEffect.NavigateToHome(isNewUser = result.userProfile.isNewUser))
                    }
                }
            }

            _uiState.update { LoginUiState.Idle }
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(LoginUiEffect.ShowMessage(message))
        }
    }
}
