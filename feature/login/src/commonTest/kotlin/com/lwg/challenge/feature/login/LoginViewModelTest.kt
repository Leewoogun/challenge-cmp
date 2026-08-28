package com.lwg.challenge.feature.login

import app.cash.turbine.test
import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.usecase.LoginWithKakaoUseCase
import com.lwg.challenge.domain.usecase.LoginWithTestAccountUseCase
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.feature.login.contract.LoginUiEffect
import com.lwg.challenge.feature.login.contract.LoginUiState
import com.lwg.challenge.feature.login.contract.TestAccount
import com.lwg.challenge.feature.login.kakao.KakaoAuthResult
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var fakeRepository: FakeLoginRepository
    private lateinit var fakeUserInfoRepository: FakeUserInfoRepository
    private lateinit var fakeKakaoAuthClient: FakeKakaoAuthClient
    private lateinit var fakeFcmTokenProvider: FakeFcmTokenProvider
    private lateinit var fakeFcmTokenRepository: FakeFcmTokenRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepository = FakeLoginRepository()
        fakeUserInfoRepository = FakeUserInfoRepository()
        fakeKakaoAuthClient = FakeKakaoAuthClient()
        fakeFcmTokenProvider = FakeFcmTokenProvider()
        fakeFcmTokenRepository = FakeFcmTokenRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(
            kakaoAuthClient = fakeKakaoAuthClient,
            loginWithKakaoUseCase = LoginWithKakaoUseCase(fakeRepository),
            loginWithTestAccountUseCase = LoginWithTestAccountUseCase(
                logoutUseCase = LogoutUseCase(fakeRepository, fakeUserInfoRepository),
                loginRepository = fakeRepository,
            ),
            fcmTokenProvider = fakeFcmTokenProvider,
            fcmTokenRepository = fakeFcmTokenRepository,
        )
    }

    @Test
    fun `카카오 OAuth 성공 후 서버 로그인 성공 시 NavigateToHome UiEffect 를 발행하고 Idle 로 복귀한다`() =
        runTest {
            fakeKakaoAuthClient.result = KakaoAuthResult.Success(accessToken = "real-kakao-token")
            fakeRepository.loginResponse = FakeLoginRepository.LoginResponse.Success(
                result = LoginResult(
                    tokens = AuthTokens("a", "r"),
                    userId = 42L,
                    isNewUser = true,
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

    @Test
    fun `테스트 로그인 성공 시 NavigateToHome UiEffect 를 발행하고 선택한 계정 번호가 전달된다`() = runTest {
        fakeRepository.testLoginResponse = FakeLoginRepository.TestLoginResponse.Success(
            result = LoginResult(
                tokens = AuthTokens("a", "r"),
                userId = 2L,
                isNewUser = false,
            ),
        )
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.loginWithTestAccount(TestAccount.TESTER_2)
            assertIs<LoginUiEffect.NavigateToHome>(awaitItem())
        }
        assertEquals(2, fakeRepository.lastTestUserNo)
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }

    @Test
    fun `테스트 로그인 성공 시 isNewUser 가 카카오와 동일하게 그대로 전달된다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            fakeRepository.testLoginResponse = FakeLoginRepository.TestLoginResponse.Success(
                result = LoginResult(
                    tokens = AuthTokens("a", "r"),
                    userId = 1L,
                    isNewUser = true,
                ),
            )
            viewModel.loginWithTestAccount(TestAccount.TESTER_1)
            val first = awaitItem()
            assertIs<LoginUiEffect.NavigateToHome>(first)
            assertEquals(true, first.isNewUser)

            fakeRepository.testLoginResponse = FakeLoginRepository.TestLoginResponse.Success(
                result = LoginResult(
                    tokens = AuthTokens("a", "r"),
                    userId = 1L,
                    isNewUser = false,
                ),
            )
            viewModel.loginWithTestAccount(TestAccount.TESTER_1)
            val second = awaitItem()
            assertIs<LoginUiEffect.NavigateToHome>(second)
            assertEquals(false, second.isNewUser)
        }
    }

    @Test
    fun `서버가 테스트 로그인 미지원 응답 시 isUnavailable 로 비활성화되고 ShowMessage 를 1회 발행한다`() = runTest {
        fakeRepository.testLoginResponse = FakeLoginRepository.TestLoginResponse.Unsupported
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.loginWithTestAccount(TestAccount.TESTER_1)
            val effect = awaitItem()
            assertIs<LoginUiEffect.ShowMessage>(effect)
            assertEquals("이 서버는 테스트 로그인을 지원하지 않아요", effect.message)
            expectNoEvents()
        }
        val testLoginState = viewModel.testLoginState.value
        assertTrue(testLoginState.isUnavailable)
        assertFalse(testLoginState.isEnabled)
        assertNotNull(testLoginState.caption)
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }

    @Test
    fun `테스트 로그인 일반 실패 시 ShowMessage 만 발행하고 isUnavailable 은 false 를 유지한다`() = runTest {
        fakeRepository.testLoginResponse = FakeLoginRepository.TestLoginResponse.Error(
            message = "서버에 문제가 발생했어요",
        )
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.loginWithTestAccount(TestAccount.TESTER_1)
            val effect = awaitItem()
            assertIs<LoginUiEffect.ShowMessage>(effect)
            assertEquals("서버에 문제가 발생했어요", effect.message)
            expectNoEvents()
        }
        assertFalse(viewModel.testLoginState.value.isUnavailable)
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }

    @Test
    fun `테스트 로그인 진행 중 재호출하면 중복 요청이 나가지 않는다`() = runTest {
        val gate = CompletableDeferred<Unit>()
        fakeRepository.testLoginGate = gate
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.loginWithTestAccount(TestAccount.TESTER_1)
            assertIs<LoginUiState.Loading>(viewModel.uiState.value)

            viewModel.loginWithTestAccount(TestAccount.TESTER_2)
            assertEquals(1, fakeRepository.callLog.count { it == "loginWithTestAccount" })
            assertEquals(1, fakeRepository.lastTestUserNo)

            gate.complete(Unit)
            assertIs<LoginUiEffect.NavigateToHome>(awaitItem())
            expectNoEvents()
        }
        assertIs<LoginUiState.Idle>(viewModel.uiState.value)
    }

    @Test
    fun `테스트 로그인 요청 전에 LogoutUseCase 가 먼저 실행되어 토큰과 UserInfo 캐시를 비운다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.loginWithTestAccount(TestAccount.TESTER_1)
            assertIs<LoginUiEffect.NavigateToHome>(awaitItem())
        }
        assertEquals(listOf("logout", "clearTokens", "loginWithTestAccount"), fakeRepository.callLog)
        assertEquals(1, fakeUserInfoRepository.clearUserInfoCacheCallCount)
    }

    @Test
    fun `카카오 로그인 성공 - FCM 토큰을 1회 등록한다`() = runTest {
        fakeKakaoAuthClient.result = KakaoAuthResult.Success(accessToken = "real-kakao-token")
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.login()
            assertIs<LoginUiEffect.NavigateToHome>(awaitItem())
        }

        assertEquals(1, fakeFcmTokenRepository.updateCallCount)
        assertEquals("fcm-token-1", fakeFcmTokenRepository.lastFcmToken)
    }

    @Test
    fun `테스트 로그인 성공 - 카카오와 같은 경로로 FCM 토큰을 등록한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.loginWithTestAccount(TestAccount.TESTER_1)
            assertIs<LoginUiEffect.NavigateToHome>(awaitItem())
        }

        assertEquals(1, fakeFcmTokenRepository.updateCallCount)
    }

    @Test
    fun `로그인 실패 - FCM 토큰을 등록하지 않는다`() = runTest {
        fakeKakaoAuthClient.result = KakaoAuthResult.Success(accessToken = "real-kakao-token")
        fakeRepository.loginResponse = FakeLoginRepository.LoginResponse.Error("로그인 실패")
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.login()
            assertIs<LoginUiEffect.ShowMessage>(awaitItem())
        }

        assertEquals(0, fakeFcmTokenRepository.updateCallCount)
    }

    @Test
    fun `accessToken 이 비어 있으면 등록을 건너뛴다`() = runTest {
        fakeKakaoAuthClient.result = KakaoAuthResult.Success(accessToken = "real-kakao-token")
        fakeRepository.loginResponse = FakeLoginRepository.LoginResponse.Success(
            result = LoginResult(
                tokens = AuthTokens(accessToken = "", refreshToken = "ref"),
                userId = 10L,
                isNewUser = false,
            ),
        )
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.login()
            assertIs<LoginUiEffect.NavigateToHome>(awaitItem())
        }

        assertEquals(0, fakeFcmTokenRepository.updateCallCount)
    }

    @Test
    fun `FCM 토큰 획득 실패해도 홈으로 이동한다`() = runTest {
        fakeKakaoAuthClient.result = KakaoAuthResult.Success(accessToken = "real-kakao-token")
        fakeFcmTokenProvider.token = null
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.login()
            assertIs<LoginUiEffect.NavigateToHome>(awaitItem())
        }

        assertEquals(0, fakeFcmTokenRepository.updateCallCount)
    }
}
