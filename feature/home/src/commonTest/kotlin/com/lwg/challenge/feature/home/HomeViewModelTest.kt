@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.home

import app.cash.turbine.test
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeEmptyType
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var fakeRecord: FakeRecordRepository
    private lateinit var fakeChallenges: FakeActiveChallengeRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRecord = FakeRecordRepository()
        fakeChallenges = FakeActiveChallengeRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(GetHomeDataUseCase(fakeRecord, fakeChallenges))

    @Test
    fun `둘 다 성공하면 Data 로 전이`() = runTest {
        fakeRecord.record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3)
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertIs<HomeUiState.Data>(state)
        assertEquals(7, state.record.win)
    }

    @Test
    fun `전적 0 + 챌린지 0 이면 emptyType FIRST_USER`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(HomeEmptyType.FIRST_USER, state.emptyType)
    }

    @Test
    fun `기존 사용자 + 챌린지 0 이면 emptyType NO_ACTIVE_CHALLENGE`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(HomeEmptyType.NO_ACTIVE_CHALLENGE, state.emptyType)
    }

    @Test
    fun `챌린지가 있으면 emptyType NONE 이며 카드 정보가 그대로 전달된다`() = runTest {
        val challenge = ActiveChallenge(
            challengeId = 1001L,
            myMission = "오늘 운동 1시간 하기",
            opponentNickname = "민수",
            opponentMission = "책 30페이지 읽기",
            deadline = Instant.parse("2026-12-31T23:59:59Z"),
            myVerificationStatus = VerificationStatus.PENDING,
            opponentVerificationStatus = VerificationStatus.VERIFIED,
            bet = "커피 사기 ☕",
        )
        fakeRecord.record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3)
        fakeChallenges.challenges = listOf(challenge)

        val viewModel = createViewModel()
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(HomeEmptyType.NONE, state.emptyType)
        assertEquals(1, state.challenges.size)
        assertEquals(1001L, state.challenges.first().challengeId)
    }

    @Test
    fun `record 가 실패하면 uiState 는 Loading 유지 + ShowMessage 발행`() = runTest {
        fakeRecord.error = "전적 조회 실패"
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("전적 조회 실패", effect.message)
        }
        assertIs<HomeUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `challenges 가 실패하면 uiState 는 Loading 유지 + ShowMessage 발행`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.error = "챌린지 조회 실패"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("챌린지 조회 실패", effect.message)
        }
        assertIs<HomeUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `init 시 양쪽 API 가 1회씩 호출된다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()

        createViewModel()

        assertEquals(1, fakeRecord.callCount)
        assertEquals(1, fakeChallenges.callCount)
    }
}
