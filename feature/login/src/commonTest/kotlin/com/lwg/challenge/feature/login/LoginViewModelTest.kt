package com.lwg.challenge.feature.login

import app.cash.turbine.test
import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.model.UserProfile
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
import com.lwg.challenge.feature.login.kakao.KakaoAuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var fakeRepository: FakeLoginRepository
    private lateinit var fakeKakaoAuthClient: FakeKakaoAuthClient

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepository = FakeLoginRepository()
        fakeKakaoAuthClient = FakeKakaoAuthClient()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(
            kakaoAuthClient = fakeKakaoAuthClient,
            loginWithKakaoUseCase = LoginWithKakaoUseCase(fakeRepository),
        )
    }

    @Test
    fun `카카오 OAuth 성공 후 서버 로그인 성공 시 NavigateToHome UiEffect 를 발행하고 Idle 로 복귀한다`() =
        runTest {
            fakeKakaoAuthClient.result = KakaoAuthResult.Success(accessToken = "real-kakao-token")
            fakeRepository.loginResponse = FakeLoginRepository.LoginResponse.Success(
                result = LoginResult(
                    tokens = AuthTokens("a", "r"),
                    userProfile = UserProfile(userId = 42L, isNewUser = true),
                ),
            )
            val viewModel = createViewModel()

            viewModel.uiEffect.test {
                viewModel.login()
                val effect = awaitItem()
                assertIs<LoginUiEffect.NavigateToHome>(effect)
                assertEquals(true, effect.isNewUser)
            }
            assertIs<LoginUiState.Idle>(viewModel.uiState.value)
        }

    @Test
    fun `카카오 OAuth Cancelled 시 UiEffect 발행 없이 Idle 로 복귀한다`() = runTest {
        fakeKakaoAuthClient.result = KakaoAuthResult.Cancelled
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.login()
            expectNoEvents()
        }
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }

    @Test
    fun `카카오 OAuth Failure 시 ShowMessage UiEffect 로 SDK 메시지를 노출하고 Idle 로 복귀한다`() = runTest {
        fakeKakaoAuthClient.result = KakaoAuthResult.Failure(message = "카카오톡이 설치되어 있지 않습니다.")
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.login()
            val effect = awaitItem()
            assertIs<LoginUiEffect.ShowMessage>(effect)
            assertEquals("카카오톡이 설치되어 있지 않습니다.", effect.message)
        }
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }

    @Test
    fun `서버 로그인 실패 시 ShowMessage UiEffect 로 스낵바 메시지를 발행하고 Idle 로 복귀한다`() = runTest {
        fakeKakaoAuthClient.result = KakaoAuthResult.Success(accessToken = "real-kakao-token")
        fakeRepository.loginResponse = FakeLoginRepository.LoginResponse.Error(
            message = "카카오 로그인이 만료되었습니다. 다시 시도해주세요",
        )
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.login()
            val effect = awaitItem()
            assertIs<LoginUiEffect.ShowMessage>(effect)
            assertEquals("카카오 로그인이 만료되었습니다. 다시 시도해주세요", effect.message)
        }
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }
}
