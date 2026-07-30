@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.home

import app.cash.turbine.test
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeEmptyType
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var fakeRecord: FakeRecordRepository
    private lateinit var fakeChallenges: FakeActiveChallengeRepository
    private lateinit var fakeUserInfo: FakeUserInfoRepository
    private lateinit var fakeChallengeRepo: FakeChallengeRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRecord = FakeRecordRepository()
        fakeChallenges = FakeActiveChallengeRepository()
        fakeUserInfo = FakeUserInfoRepository()
        fakeChallengeRepo = FakeChallengeRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            GetHomeDataUseCase(fakeRecord, fakeChallenges, fakeUserInfo, fakeChallengeRepo),
            fakeChallengeRepo,
        )

    // uiState 가 WhileSubscribed 이므로 구독자가 있어야 파이프라인이 시작된다.
    private fun TestScope.subscribeUiState(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun receivedChallenge(
        challengeId: Long = 1L,
        nickname: String = "민수",
        mission: String = "책 30페이지 읽기",
        bet: String = "커피 사기",
        deadline: Instant = Clock.System.now() + 3.hours,
    ) = ReceivedChallenge(
        challengeId = challengeId,
        challengerId = challengeId * 10,
        challengerNickname = nickname,
        challengerProfileImageUrl = null,
        challengerMission = mission,
        betContent = bet,
        deadline = deadline,
        createdAt = Clock.System.now(),
    )

    @Test
    fun `둘 다 성공하면 Data 로 전이`() = runTest {
        fakeRecord.record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3)
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value

        assertIs<HomeUiState.Data>(state)
        assertEquals(7, state.record.win)
    }

    @Test
    fun `전적 0 + 챌린지 0 이면 emptyType FIRST_USER`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(HomeEmptyType.FIRST_USER, state.emptyType)
    }

    @Test
    fun `기존 사용자 + 챌린지 0 이면 emptyType NO_ACTIVE_CHALLENGE`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
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
        subscribeUiState(viewModel)
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
        subscribeUiState(viewModel)

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
        subscribeUiState(viewModel)

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

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertEquals(1, fakeRecord.callCount)
        assertEquals(1, fakeChallenges.callCount)
    }

    @Test
    fun `캐시 있으면 init 시 즉시 userInfo 노출`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeUserInfo.seedCache(
            UserInfo(id = 12, kakaoId = 999, nickname = "이우건", profileImageUrl = null),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals("이우건", state.userInfo?.nickname)
    }

    @Test
    fun `캐시 없으면 init 트리거가 fetch + 관찰로 노출`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeUserInfo.fetchResult =
            UserInfo(id = 7, kakaoId = 1, nickname = "민수", profileImageUrl = null)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals("민수", state.userInfo?.nickname)
        assertEquals(1, fakeUserInfo.getUserInfoCallCount)
    }

    @Test
    fun `유저 조회 실패 시 ShowMessage 1회 + userInfo 는 null`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeUserInfo.error = "유저 조회 실패"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.uiEffect.test {
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("유저 조회 실패", effect.message)
        }
        val state = viewModel.uiState.value as HomeUiState.Data
        assertEquals(null, state.userInfo)
    }

    @Test
    fun `받은 도전장이 있으면 카드 문구가 그대로 매핑된다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(
            receivedChallenge(challengeId = 55L, nickname = "민수", bet = "커피 사기"),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(1, state.receivedChallenges.size)
        val item = state.receivedChallenges.first()
        assertEquals(55L, item.challengeId)
        assertEquals("민수의 도전장", item.titleText)
        assertEquals("내기: 커피 사기", item.betText)
    }

    @Test
    fun `받은 도전장 조회가 실패해도 홈은 Data 로 전이하고 ShowMessage 를 발행한다`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.error = "받은 도전장 조회 실패"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.uiEffect.test {
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("받은 도전장 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertIs<HomeUiState.Data>(state)
        assertTrue(state.receivedChallenges.isEmpty())
    }

    @Test
    fun `받은 도전장이 있으면 전적 0 이어도 emptyType 은 NO_ACTIVE_CHALLENGE`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 60L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(HomeEmptyType.NO_ACTIVE_CHALLENGE, state.emptyType)
    }

    @Test
    fun `openAcceptDialog 는 대상을 세팅하고 dismissAcceptDialog 는 해제한다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 71L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val item = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges.first()

        viewModel.openAcceptDialog(item)
        assertEquals(item, (viewModel.uiState.value as HomeUiState.Data).acceptTarget)

        viewModel.dismissAcceptDialog()
        assertNull((viewModel.uiState.value as HomeUiState.Data).acceptTarget)
    }

    @Test
    fun `updateAcceptMission 은 최대 길이로 잘린다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.updateAcceptMission("가".repeat(101))

        val state = viewModel.uiState.value as HomeUiState.Data
        assertEquals(100, state.acceptMission.length)
    }

    @Test
    fun `submitAccept 성공 시 trim 된 미션으로 요청하고 목록을 재조회한다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 77L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val item = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges.first()
        val callCountBeforeSubmit = fakeChallengeRepo.callCount

        viewModel.openAcceptDialog(item)
        viewModel.updateAcceptMission("  물 2L 마시기  ")
        viewModel.submitAccept()
        advanceUntilIdle()

        assertEquals(77L, fakeChallengeRepo.lastAcceptedId)
        assertEquals("물 2L 마시기", fakeChallengeRepo.lastAcceptedMission)
        assertEquals(callCountBeforeSubmit + 1, fakeChallengeRepo.callCount)
        assertNull((viewModel.uiState.value as HomeUiState.Data).acceptTarget)
    }

    @Test
    fun `submitAccept 실패 시 ShowMessage 를 발행하고 제출 상태를 풀고 재조회한다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 78L))
        fakeChallengeRepo.acceptError = "이미 응답한 도전장이에요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val item = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges.first()
        val callCountBeforeSubmit = fakeChallengeRepo.callCount

        viewModel.openAcceptDialog(item)
        viewModel.updateAcceptMission("물 2L 마시기")

        viewModel.uiEffect.test {
            viewModel.submitAccept()
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("이미 응답한 도전장이에요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeUiState.Data
        assertFalse(state.isSubmittingAccept)
        assertEquals(callCountBeforeSubmit + 1, fakeChallengeRepo.callCount)
    }

    @Test
    fun `submitAccept 를 연속 호출해도 수락 요청은 1회만 나간다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 79L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val item = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges.first()

        viewModel.openAcceptDialog(item)
        viewModel.updateAcceptMission("물 2L 마시기")
        viewModel.submitAccept()
        viewModel.submitAccept()
        advanceUntilIdle()

        assertEquals(1, fakeChallengeRepo.acceptCallCount)
    }

    @Test
    fun `rejectChallenge 성공 시 해당 id 로 요청하고 목록을 재조회한다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 88L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val item = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges.first()
        val callCountBeforeReject = fakeChallengeRepo.callCount

        viewModel.rejectChallenge(item)
        advanceUntilIdle()

        assertEquals(1, fakeChallengeRepo.rejectCallCount)
        assertEquals(88L, fakeChallengeRepo.lastRejectedId)
        assertEquals(callCountBeforeReject + 1, fakeChallengeRepo.callCount)
    }

    @Test
    fun `rejectChallenge 실패 시 ShowMessage 를 발행하고 재조회한다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 89L))
        fakeChallengeRepo.rejectError = "이미 응답한 도전장이에요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val item = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges.first()
        val callCountBeforeReject = fakeChallengeRepo.callCount

        viewModel.uiEffect.test {
            viewModel.rejectChallenge(item)
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("이미 응답한 도전장이에요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        assertEquals(callCountBeforeReject + 1, fakeChallengeRepo.callCount)
    }

    @Test
    fun `마감까지 1시간 이하면 isUrgent 이고 넘으면 아니다`() = runTest {
        val now = Clock.System.now()
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(
            receivedChallenge(challengeId = 91L, deadline = now + 30.minutes),
            receivedChallenge(challengeId = 92L, deadline = now + 3.hours),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val items = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges

        assertTrue(items.first { it.challengeId == 91L }.isUrgent)
        assertFalse(items.first { it.challengeId == 92L }.isUrgent)
    }
}
