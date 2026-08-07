package com.lwg.challenge.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.FcmTokenRepository
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.domain.usecase.LoginWithTestAccountUseCase
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
import com.lwg.challenge.feature.login.contract.TEST_LOGIN_UNSUPPORTED_MESSAGE
import com.lwg.challenge.feature.login.contract.TestAccount
import com.lwg.challenge.feature.login.contract.TestLoginUiState
import com.lwg.challenge.feature.login.kakao.KakaoAuthClient
import com.lwg.challenge.feature.login.kakao.KakaoAuthResult
import com.lwg.challenge.push.FcmTokenProvider
import com.lwg.challenge.utils.Logger
import com.lwg.challenge.utils.flow.withData
import com.lwg.challenge.utils.isDebug
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
    private val loginWithTestAccountUseCase: LoginWithTestAccountUseCase,
    private val fcmTokenProvider: FcmTokenProvider,
    private val fcmTokenRepository: FcmTokenRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LoginUiEffect>()
    val uiEffect: SharedFlow<LoginUiEffect> = _uiEffect.asSharedFlow()

    private val _testLoginState = MutableStateFlow(TestLoginUiState(isVisible = isDebug))
    val testLoginState: StateFlow<TestLoginUiState> = _testLoginState.asStateFlow()

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
                        registerFcmToken(result.tokens.accessToken)
                        _uiEffect.emit(LoginUiEffect.NavigateToHome(isNewUser = result.isNewUser))
                    }
                }
            }

            _uiState.update { LoginUiState.Idle }
        }
    }

    fun loginWithTestAccount(account: TestAccount) {
        _uiState.withData<LoginUiState.Loading> { return }

        viewModelScope.launch {
            _uiState.update { LoginUiState.Loading }

            loginWithTestAccountUseCase(
                testUserNo = account.no,
                onError = ::showMessage,
                onUnsupported = ::markTestLoginUnavailable,
            ).collect { result ->
                registerFcmToken(result.tokens.accessToken)
                _uiEffect.emit(LoginUiEffect.NavigateToHome(isNewUser = result.isNewUser))
            }

            _uiState.update { LoginUiState.Idle }
        }
    }

    private suspend fun registerFcmToken(accessToken: String) {
        if (accessToken.isBlank()) return

        val fcmToken = fcmTokenProvider.getToken() ?: return
        fcmTokenRepository.updateFcmToken(
            fcmToken = fcmToken,
            onSuccess = {},
            onError = { Logger.e("FCM 토큰 등록 실패: $it") },
        )
    }

    private fun markTestLoginUnavailable() {
        _testLoginState.update { it.copy(isUnavailable = true) }
        showMessage(TEST_LOGIN_UNSUPPORTED_MESSAGE)
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(LoginUiEffect.ShowMessage(message))
        }
    }
}
