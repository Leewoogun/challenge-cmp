package com.lwg.challenge.feature.friends.record

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.friend.FriendChallengeStatus
import com.lwg.challenge.domain.model.friend.FriendOngoingChallenge
import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.model.friend.FriendScore
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.domain.usecase.GetFriendChallengesUseCase
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiEffect
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import com.lwg.challenge.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FriendRecordViewModelTest {

    private lateinit var fakeFriends: FakeFriendsRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeFriends = FakeFriendsRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // uiState 가 WhileSubscribed 라 구독해야 조회가 시작된다. 화면 진입 = 구독.
    private fun TestScope.createViewModel(): FriendRecordViewModel =
        FriendRecordViewModel(
            friendId = FRIEND_ID,
            friendsRepository = fakeFriends,
            getFriendChallengesUseCase = GetFriendChallengesUseCase(fakeFriends),
        ).also { viewModel ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }
        }

    private fun dataState(viewModel: FriendRecordViewModel): FriendRecordUiState.Data {
        val state = viewModel.uiState.value
        assertIs<FriendRecordUiState.Data>(state)
        return state
    }

    private fun record(
        score: FriendScore = FriendScore(myWins = 2, friendWins = 1, draws = 1, bothLose = 1),
        ongoing: List<FriendOngoingChallenge> = listOf(ongoing(31L)),
    ) = FriendRecord(friend = FRIEND, score = score, ongoing = ongoing)

    private fun ongoing(
        challengeId: Long,
        status: FriendChallengeStatus = FriendChallengeStatus.IN_PROGRESS,
        sentByMe: Boolean = true,
        // 기본값은 양쪽 미션이 다 있다 — 목적지가 미션 null 이 아니라 sentByMe 로 정해지는지 보려는 것이다.
        myMission: String? = "책 한 권 읽기",
        friendMission: String? = "러닝 5km",
    ) = FriendOngoingChallenge(
        challengeId = challengeId,
        status = status,
        sentByMe = sentByMe,
        deadline = LocalDateTime(2099, 1, 1, 0, 0),
        myMission = myMission,
        friendMission = friendMission,
        bet = "커피 사기",
    )

    private fun past(challengeId: Long, myResult: ChallengeOutcome = ChallengeOutcome.WIN) = FriendPastChallenge(
        challengeId = challengeId,
        challengeDate = LocalDate(2026, 11, 2),
        myMission = "운동 1시간",
        friendMission = "책 30페이지",
        bet = "커피 사기",
        myResult = myResult,
    )

    private fun pasts(ids: List<Long>): List<FriendPastChallenge> = ids.map { past(it) }

    private fun TestScope.loadData(
        score: FriendScore = FriendScore(myWins = 2, friendWins = 1, draws = 1, bothLose = 1),
        ongoing: List<FriendOngoingChallenge> = listOf(ongoing(31L)),
        pastIds: List<Long> = listOf(19L),
    ): FriendRecordUiState.Data {
        fakeFriends.enqueueRecord(record(score = score, ongoing = ongoing))
        fakeFriends.enqueuePage(pasts(pastIds), nextChallengeId = null)
        return dataState(createViewModel())
    }

    // region 첫 로드

    @Test
    fun `첫 로드가 둘 다 성공하면 스코어와 진행 중과 첫 쪽을 담은 Data 다`() = runTest {
        val score = FriendScore(myWins = 2, friendWins = 1, draws = 1, bothLose = 1)
        fakeFriends.enqueueRecord(record(score = score, ongoing = listOf(ongoing(31L), ongoing(33L))))
        fakeFriends.enqueuePage(pasts(listOf(19L, 17L)), nextChallengeId = 12L)

        val state = dataState(createViewModel())

        assertEquals(score, state.record.score)
        assertEquals(listOf(31L, 33L), state.record.ongoing.map { it.challengeId })
        assertEquals(listOf(19L, 17L), state.pastChallenges.map { it.challengeId })
        assertFalse(state.isPageEnd)
    }

    @Test
    fun `스코어 조회가 실패하면 서버 메시지를 담은 Error 다`() = runTest {
        fakeFriends.enqueueRecordFailure("친구를 찾을 수 없어요")
        fakeFriends.enqueuePage(pasts(listOf(19L)), nextChallengeId = null)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertIs<FriendRecordUiState.Error>(state)
        assertEquals("친구를 찾을 수 없어요", state.message)
    }

    @Test
    fun `지난 첫 쪽 조회가 실패하면 스코어가 와도 Error 다`() = runTest {
        fakeFriends.enqueueRecord(record())
        fakeFriends.enqueuePageFailure("일시적인 오류예요")

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertIs<FriendRecordUiState.Error>(state)
        assertEquals("일시적인 오류예요", state.message)
    }

    @Test
    fun `retry 는 스코어와 첫 쪽을 둘 다 다시 불러 Data 로 간다`() = runTest {
        fakeFriends.enqueueRecordFailure("친구를 찾을 수 없어요")
        fakeFriends.enqueuePage(pasts(listOf(19L)), nextChallengeId = null)
        val viewModel = createViewModel()
        assertIs<FriendRecordUiState.Error>(viewModel.uiState.value)

        fakeFriends.enqueueRecord(record())
        fakeFriends.enqueuePage(pasts(listOf(19L)), nextChallengeId = null)
        viewModel.retry()
        runCurrent()

        assertEquals(2, fakeFriends.getFriendRecordCallCount)
        assertEquals(2, fakeFriends.getFriendChallengesCallCount)
        assertIs<FriendRecordUiState.Data>(viewModel.uiState.value)
    }

    // endregion

    // region 빈 상태 — 목록 기준

    @Test
    fun `지난 첫 쪽과 진행 중이 모두 비면 Empty 다`() = runTest {
        fakeFriends.enqueueRecord(record(score = FriendScore(0, 0, 0, 0), ongoing = emptyList()))
        fakeFriends.enqueuePage(emptyList(), nextChallengeId = null)

        val state = createViewModel().uiState.value

        assertIs<FriendRecordUiState.Empty>(state)
        assertEquals(FRIEND, state.friend)
    }

    @Test
    fun `지난 첫 쪽이 비어도 진행 중이 있으면 지난 섹션만 빈 Data 다`() = runTest {
        val state = loadData(score = FriendScore(0, 0, 0, 0), ongoing = listOf(ongoing(31L)), pastIds = emptyList())

        assertTrue(state.isPastEmpty)
        assertTrue(state.hasOngoing)
    }

    /** 불변식상 없는 조합이지만, 판별이 스코어를 보지 않는다는 걸 고정하려고 두 축을 갈라놓는다. */
    @Test
    fun `스코어가 0이 아니어도 지난 첫 쪽과 진행 중이 비면 Empty 다`() = runTest {
        fakeFriends.enqueueRecord(record(score = FriendScore(3, 1, 0, 0), ongoing = emptyList()))
        fakeFriends.enqueuePage(emptyList(), nextChallengeId = null)

        assertIs<FriendRecordUiState.Empty>(createViewModel().uiState.value)
    }

    // endregion

    // region 스코어 표시 — 스코어 기준

    @Test
    fun `스코어 4값이 모두 0이면 muted 다`() = runTest {
        val state = loadData(score = FriendScore(0, 0, 0, 0), pastIds = emptyList())

        assertTrue(state.isScoreMuted)
    }

    @Test
    fun `스코어 중 하나라도 0이 아니면 muted 가 아니다`() = runTest {
        val state = loadData(score = FriendScore(myWins = 0, friendWins = 0, draws = 1, bothLose = 0))

        assertFalse(state.isScoreMuted)
    }

    @Test
    fun `무와 양측 패배가 둘 다 있으면 보조 줄에 둘 다 쓴다`() = runTest {
        val state = loadData(score = FriendScore(myWins = 2, friendWins = 1, draws = 1, bothLose = 1))

        assertEquals("무 1 · 양측 패배 1", state.scoreSubLineText)
    }

    @Test
    fun `양측 패배가 0이면 보조 줄에 무만 쓴다`() = runTest {
        val state = loadData(score = FriendScore(myWins = 1, friendWins = 1, draws = 2, bothLose = 0))

        assertEquals("무 2", state.scoreSubLineText)
    }

    @Test
    fun `무가 0이면 보조 줄에 양측 패배만 쓴다`() = runTest {
        val state = loadData(score = FriendScore(myWins = 1, friendWins = 1, draws = 0, bothLose = 3))

        assertEquals("양측 패배 3", state.scoreSubLineText)
    }

    @Test
    fun `무와 양측 패배가 둘 다 0이면 보조 줄이 없다`() = runTest {
        val state = loadData(score = FriendScore(myWins = 4, friendWins = 2, draws = 0, bothLose = 0))

        assertNull(state.scoreSubLineText)
    }

    // endregion

    // region 진행 중 탭 목적지

    @Test
    fun `받은 도전장을 탭하면 맹세 화면으로 간다`() = runTest {
        val received = ongoing(33L, status = FriendChallengeStatus.PENDING, sentByMe = false)
        val state = loadData(ongoing = listOf(received))

        assertEquals(Route.Challenge.Oath(33L), state.routeOf(received))
    }

    @Test
    fun `보낸 도전장을 탭하면 상세로 간다`() = runTest {
        val sent = ongoing(35L, status = FriendChallengeStatus.PENDING, sentByMe = true)
        val state = loadData(ongoing = listOf(sent))

        assertEquals(Route.Challenge.Detail(35L), state.routeOf(sent))
    }

    @Test
    fun `진행 중을 탭하면 받은 쪽이어도 상세로 간다`() = runTest {
        val inProgress = ongoing(31L, status = FriendChallengeStatus.IN_PROGRESS, sentByMe = false)
        val state = loadData(ongoing = listOf(inProgress))

        assertEquals(Route.Challenge.Detail(31L), state.routeOf(inProgress))
    }

    // endregion

    // region 다음 쪽

    /** nextChallengeId 를 마지막 항목 id 와도, 그 id - 1 과도 다르게 심어 앱이 키를 직접 계산하면 깨진다. */
    @Test
    fun `nextPage 는 두 번째 쪽을 뒤에 덧붙이고 서버가 준 nextChallengeId 를 그대로 싣는다`() = runTest {
        fakeFriends.enqueueRecord(record())
        fakeFriends.enqueuePage(pasts(listOf(19L, 17L)), nextChallengeId = 12L)
        fakeFriends.enqueuePage(pasts(listOf(12L, 9L)), nextChallengeId = null)

        val viewModel = createViewModel()
        viewModel.nextPage()

        assertEquals(listOf(null, 12L), fakeFriends.requestedNextChallengeIds)
        val state = dataState(viewModel)
        assertEquals(listOf(19L, 17L, 12L, 9L), state.pastChallenges.map { it.challengeId })
        assertTrue(state.isPageEnd)
    }

    @Test
    fun `다음 쪽을 받아도 스코어는 다시 부르지 않는다`() = runTest {
        fakeFriends.enqueueRecord(record())
        fakeFriends.enqueuePage(pasts(listOf(19L)), nextChallengeId = 12L)
        fakeFriends.enqueuePage(pasts(listOf(12L)), nextChallengeId = null)

        val viewModel = createViewModel()
        viewModel.nextPage()

        assertEquals(1, fakeFriends.getFriendRecordCallCount)
    }

    @Test
    fun `isPageEnd 면 nextPage 를 불러도 요청이 나가지 않는다`() = runTest {
        fakeFriends.enqueueRecord(record())
        fakeFriends.enqueuePage(pasts(listOf(19L, 17L)), nextChallengeId = null)

        val viewModel = createViewModel()
        viewModel.nextPage()
        viewModel.nextPage()

        assertEquals(listOf<Long?>(null), fakeFriends.requestedNextChallengeIds)
    }

    @Test
    fun `다음 쪽이 실패해도 목록은 남고 서버 메시지를 스낵바로 알린다`() = runTest {
        fakeFriends.enqueueRecord(record())
        fakeFriends.enqueuePage(pasts(listOf(19L, 17L)), nextChallengeId = 12L)
        fakeFriends.enqueuePageFailure("일시적인 오류예요")

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.nextPage()
            val effect = awaitItem()
            assertIs<FriendRecordUiEffect.ShowMessage>(effect)
            assertEquals("일시적인 오류예요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(19L, 17L), dataState(viewModel).pastChallenges.map { it.challengeId })
    }

    // endregion

    private companion object {
        const val FRIEND_ID = 42L
        val FRIEND = UserBrief(userId = FRIEND_ID, nickname = "민수", profileImageUrl = null)
    }
}
