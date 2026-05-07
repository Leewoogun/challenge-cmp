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

/**
 * 로그인 화면 ViewModel.
 *
 * 흐름:
 * 1. [login] 호출 → Loading 진입.
 * 2. [kakaoAuthClient] 가 카카오 OAuth (인가 → access_token 교환) 를 캡슐화하여 access_token 반환.
 *    - Cancelled: 사용자 취소. 메시지 노출 없이 Idle 복귀.
 *    - Failure: SDK / 네트워크 에러. 스낵바.
 *    - Success: access_token 을 [LoginWithKakaoUseCase] 에 넘겨 서버 로그인 진행.
 * 3. 서버 응답 처리 후 NavigateToHome.
 */
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
