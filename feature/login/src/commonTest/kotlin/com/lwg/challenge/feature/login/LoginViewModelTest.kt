package com.lwg.challenge.feature.login

import app.cash.turbine.test
import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.model.UserProfile
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.feature.login.contract.LoginModalEffect
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
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

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepository = FakeLoginRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(
            loginWithKakao = LoginWithKakaoUseCase(fakeRepository),
        )
    }

    @Test
    fun `카카오 로그인 성공 시 NavigateToHome UiEffect 를 발행하고 Idle 로 복귀한다`() = runTest {
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
    fun `카카오 토큰 만료 code=701 응답 시 ReLogin 모달 effect 로 전환된다`() = runTest {
        fakeRepository.loginResponse = FakeLoginRepository.LoginResponse.Error(
            code = 701,
            message = "카카오 로그인이 만료되었습니다. 다시 시도해주세요",
        )
        val viewModel = createViewModel()

        viewModel.login()

        val modal = viewModel.modalEffect.value
        assertIs<LoginModalEffect.ReLogin>(modal)
        assertEquals("카카오 로그인이 만료되었습니다. 다시 시도해주세요", modal.message)
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }
}
