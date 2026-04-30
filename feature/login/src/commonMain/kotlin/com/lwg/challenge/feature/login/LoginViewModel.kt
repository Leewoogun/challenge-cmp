package com.lwg.challenge.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.feature.login.contract.LoginModalEffect
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
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
 * 1. 사용자가 "카카오로 시작하기" 버튼 탭 → [login] 호출.
 * 2. 카카오 REST API 로 access token 획득 (TODO).
 * 3. [LoginWithKakaoUseCase] 로 서버 로그인.
 * 4. 성공 시 [LoginUiEffect.NavigateToHome] emit, 실패 시 code 별 처리
 *    (700: 스낵바, 701: ReLogin 다이얼로그, 703: ServerError 다이얼로그, 그 외: 스낵바).
 */
@KoinViewModel
class LoginViewModel(
    private val loginWithKakao: LoginWithKakaoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _modalEffect = MutableStateFlow<LoginModalEffect>(LoginModalEffect.Hidden)
    val modalEffect: StateFlow<LoginModalEffect> = _modalEffect.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LoginUiEffect>()
    val uiEffect: SharedFlow<LoginUiEffect> = _uiEffect.asSharedFlow()

    fun login() {
        if (_uiState.value is LoginUiState.Loading) return

        viewModelScope.launch {
            _uiState.update { LoginUiState.Loading }

            // TODO: 카카오 REST API 연동으로 access token 획득. 현재는 stub.
            val kakaoToken = STUB_KAKAO_TOKEN

            val result = loginWithKakao(
                kakaoAccessToken = kakaoToken,
                onError = { code, message -> handleError(code, message) },
            )

            if (result != null) {
                _uiState.update { LoginUiState.Idle }
                _uiEffect.emit(LoginUiEffect.NavigateToHome(isNewUser = result.userProfile.isNewUser))
            } else {
                _uiState.update { LoginUiState.Idle }
            }
        }
    }

    fun dismissModal() {
        _modalEffect.update { LoginModalEffect.Hidden }
    }

    /**
     * 다이얼로그의 "다시 로그인" 버튼 → 모달 닫고 login 재시도.
     */
    fun retryLogin() {
        _modalEffect.update { LoginModalEffect.Hidden }
        login()
    }

    private fun handleError(code: Int, message: String) {
        when (code) {
            CODE_RE_LOGIN -> _modalEffect.update { LoginModalEffect.ReLogin(message) }
            CODE_SERVER_ERROR -> _modalEffect.update { LoginModalEffect.ServerError(message) }
            else -> {
                viewModelScope.launch {
                    _uiEffect.emit(LoginUiEffect.ShowMessage(message))
                }
            }
        }
    }

    companion object {
        private const val CODE_RE_LOGIN = 701
        private const val CODE_SERVER_ERROR = 703
        private const val STUB_KAKAO_TOKEN = "TEST_TOKEN_DO_NOT_USE"
    }
}
