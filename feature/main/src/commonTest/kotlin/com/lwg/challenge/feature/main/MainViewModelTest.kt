package com.lwg.challenge.feature.main

import app.cash.turbine.test
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.feature.main.contract.MainUiEffect
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.push.PushEvent
import com.lwg.challenge.push.PushEventBus
import com.lwg.challenge.utils.AuthEventBus
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var authEventBus: AuthEventBus
    private lateinit var pushEventBus: PushEventBus
    private lateinit var fakeLoginRepository: FakeLoginRepository
    private lateinit var fakeUserInfoRepository: FakeUserInfoRepository
    private lateinit var fakePermissionManager: FakePermissionManager

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authEventBus = AuthEventBus()
        pushEventBus = PushEventBus()
        fakeLoginRepository = FakeLoginRepository()
        fakeUserInfoRepository = FakeUserInfoRepository()
        fakePermissionManager = FakePermissionManager()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MainViewModel {
        return MainViewModel(
            authEventBus = authEventBus,
            pushEventBus = pushEventBus,
            logoutUseCase = LogoutUseCase(fakeLoginRepository, fakeUserInfoRepository),
            permissionManager = fakePermissionManager,
        )
    }

    @Test
    fun `인증 영역에 들어온 뒤 알림이 도착하면 즉시 목적지를 방출한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)
            pushEventBus.emit(PushEvent.ChallengeAccepted(7L))

            assertEquals(MainUiEffect.NavigateToPush(Route.Challenge.Detail(7L)), awaitItem())
        }
    }

    @Test
    fun `알림이 먼저 도착해도 인증 영역 진입 시점에 방출한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            pushEventBus.emit(PushEvent.ChallengeAccepted(7L))
            expectNoEvents()

            viewModel.setAuthenticatedArea(true)
            assertEquals(MainUiEffect.NavigateToPush(Route.Challenge.Detail(7L)), awaitItem())
        }
    }

    @Test
    fun `방출 후 pending 이 소비되어 재방출하지 않는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)
            pushEventBus.emit(PushEvent.ChallengeAccepted(7L))
            assertEquals(MainUiEffect.NavigateToPush(Route.Challenge.Detail(7L)), awaitItem())

            assertNull(pushEventBus.pending.value)
            expectNoEvents()
        }
    }

    @Test
    fun `미인증 상태에서는 방출하지 않는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            pushEventBus.emit(PushEvent.ChallengeAccepted(7L))

            expectNoEvents()
        }
    }

    @Test
    fun `로그아웃으로 인증 영역을 벗어나면 게이트가 닫힌다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)
            pushEventBus.emit(PushEvent.ChallengeAccepted(7L))
            assertEquals(MainUiEffect.NavigateToPush(Route.Challenge.Detail(7L)), awaitItem())

            viewModel.setAuthenticatedArea(false)
            pushEventBus.emit(PushEvent.ChallengeAccepted(9L))

            expectNoEvents()
        }
    }

    @Test
    fun `로그아웃하면 대기 중이던 pending 이 소거되어 다음 인증에서 재방출되지 않는다`() = runTest {
        val viewModel = createViewModel()

        pushEventBus.emit(PushEvent.ChallengeAccepted(7L))

        viewModel.logout()

        assertNull(
            pushEventBus.pending.value,
            "로그아웃 시 딥링크를 버리지 않으면 다음 로그인한 사람이 앞사람 목적지로 끌려간다",
        )

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)

            expectNoEvents()
        }
    }

    @Test
    fun `수동 로그아웃은 USER 로 처리돼 서버 세션까지 끊는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.logout()

        assertEquals(listOf("logout", "clearTokens"), fakeLoginRepository.callLog)
    }

    // 탈퇴는 계정 자체가 사라진 뒤라 끊을 세션이 없다. 이동 경로만 로그아웃과 공유한다.
    @Test
    fun `탈퇴는 WITHDRAWN 으로 처리돼 서버를 부르지 않는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.withdrawn()

        assertEquals(listOf("clearTokens"), fakeLoginRepository.callLog)
    }

    // 갱신이 이미 실패한 상태라 서버를 부르면 401 → 갱신 재시도 → 만료 이벤트로 이 흐름에 재진입한다.
    @Test
    fun `세션 만료는 SESSION_EXPIRED 로 처리돼 서버를 부르지 않는다`() = runTest {
        val viewModel = createViewModel()

        authEventBus.emitSessionExpired()

        assertEquals(listOf("clearTokens"), fakeLoginRepository.callLog)
    }

    @Test
    fun `CHALLENGE_ACCEPTED 는 상세로 간다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)
            pushEventBus.emit(PushEvent.ChallengeAccepted(7L))

            assertEquals(MainUiEffect.NavigateToPush(Route.Challenge.Detail(7L)), awaitItem())
        }
    }

    @Test
    fun `CHALLENGE_REQUEST 는 홈으로 간다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)
            pushEventBus.emit(PushEvent.ChallengeRequest(7L))

            assertEquals(MainUiEffect.NavigateToPush(Route.Home), awaitItem())
        }
    }

    @Test
    fun `CHALLENGE_REJECTED 는 홈으로 간다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)
            pushEventBus.emit(PushEvent.ChallengeRejected(7L))

            assertEquals(MainUiEffect.NavigateToPush(Route.Home), awaitItem())
        }
    }

    @Test
    fun `OPPONENT_VERIFIED 는 상세로 간다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.setAuthenticatedArea(true)
            pushEventBus.emit(PushEvent.OpponentVerified(7L))

            assertEquals(MainUiEffect.NavigateToPush(Route.Challenge.Detail(7L)), awaitItem())
        }
    }
}
