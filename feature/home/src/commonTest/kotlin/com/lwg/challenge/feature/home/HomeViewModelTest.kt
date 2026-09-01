@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.home

import app.cash.turbine.test
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeEmptyType
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.utils.datetime.KST
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.isDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var fakeRecord: FakeRecordRepository
    private lateinit var fakeChallenges: FakeActiveChallengeRepository
    private lateinit var fakeUserInfo: FakeUserInfoRepository
    private lateinit var fakeChallengeRepo: FakeChallengeRepository
    private lateinit var fakeNotification: FakeNotificationRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRecord = FakeRecordRepository()
        fakeChallenges = FakeActiveChallengeRepository()
        fakeUserInfo = FakeUserInfoRepository()
        fakeChallengeRepo = FakeChallengeRepository()
        fakeNotification = FakeNotificationRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            GetHomeDataUseCase(
                fakeRecord,
                fakeChallenges,
                fakeUserInfo,
                fakeChallengeRepo,
                fakeNotification,
            ),
            fakeChallengeRepo,
        )

    // uiState 가 WhileSubscribed 이므로 구독자가 있어야 파이프라인이 시작된다.
    // 초기 로딩 실패의 ShowMessage 를 검증하는 테스트는 이 호출을 `uiEffect.test { }` 안에 둬야 한다 —
    // uiEffect 가 replay 0 SharedFlow 라 collector 가 붙기 전에 emit 된 이펙트는 그대로 버려진다.
    private fun TestScope.subscribeUiState(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    // ViewModel 이 내부에서 nowKst() 를 재측정하므로 픽스처도 실제 시계 기준 상대값으로 만든다.
    private fun LocalDateTime.plus(duration: Duration): LocalDateTime =
        (toInstant(KST) + duration).toLocalDateTime(KST)

    private fun receivedChallenge(
        challengeId: Long = 1L,
        nickname: String = "민수",
        mission: String = "책 30페이지 읽기",
        bet: String = "커피 사기",
        deadline: LocalDateTime = nowKst().plus(3.hours),
    ) = ReceivedChallenge(
        challengeId = challengeId,
        challengerId = challengeId * 10,
        challengerNickname = nickname,
        challengerProfileImageUrl = null,
        challengerMission = mission,
        betContent = bet,
        deadline = deadline,
        createdAt = nowKst(),
    )

    private fun activeChallenge(
        challengeId: Long = 1001L,
        myMission: String = "오늘 운동 1시간 하기",
        opponentNickname: String = "민수",
        opponentMission: String = "책 30페이지 읽기",
        deadline: LocalDateTime = LocalDateTime.parse("2026-12-31T23:59:59"),
        myVerificationStatus: VerificationStatus = VerificationStatus.PENDING,
        opponentVerificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
        bet: String = "커피 사기 ☕",
        status: ActiveChallengeStatus = ActiveChallengeStatus.IN_PROGRESS,
        outcome: ChallengeOutcome? = null,
    ) = ActiveChallenge(
        challengeId = challengeId,
        myMission = myMission,
        opponentNickname = opponentNickname,
        opponentMission = opponentMission,
        deadline = deadline,
        myVerificationStatus = myVerificationStatus,
        opponentVerificationStatus = opponentVerificationStatus,
        bet = bet,
        status = status,
        outcome = outcome,
    )

    private fun completedChallenge(
        challengeId: Long,
        outcome: ChallengeOutcome = ChallengeOutcome.WIN,
    ) = activeChallenge(
        challengeId = challengeId,
        status = ActiveChallengeStatus.COMPLETED,
        outcome = outcome,
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
        fakeRecord.record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3)
        fakeChallenges.challenges = listOf(activeChallenge(challengeId = 1001L))

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

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("전적 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        assertIs<HomeUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `challenges 가 실패하면 uiState 는 Loading 유지 + ShowMessage 발행`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.error = "챌린지 조회 실패"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("챌린지 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
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

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("유저 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
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

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
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
    fun `안 읽은 알림이 있으면 벨 뱃지가 켜지고 0 이면 꺼진다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeNotification.unreadCount = 3

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(3, state.unreadCount)
        assertTrue(state.hasUnreadNotification)

        fakeNotification.unreadCount = 0
        viewModel.uiState.restart()
        runCurrent()

        assertFalse((viewModel.uiState.value as HomeUiState.Data).hasUnreadNotification)
    }

    /** 8dp 점 하나 때문에 홈 전체가 Loading 에 갇히면 안 된다. */
    @Test
    fun `안 읽은 개수 조회가 실패해도 홈은 Data 로 전이하고 뱃지는 꺼진다`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeChallenges.challenges = emptyList()
        fakeNotification.error = "알림 개수 조회 실패"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            val effect = awaitItem()
            assertIs<HomeUiEffect.ShowMessage>(effect)
            assertEquals("알림 개수 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertIs<HomeUiState.Data>(state)
        assertFalse(state.hasUnreadNotification)
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

    /**
     * 수락은 별도 라우트(맹세 화면)로 빠져 홈 ViewModel 에 성공 콜백이 없다. 맹세 후 홈이 갱신되는
     * 유일한 근거가 `WhileSubscribed(0)` 의 이 재구독 재조회다.
     * 이 동작이 깨지면 수락에 성공해도 받은 도전장이 홈에 그대로 남는다.
     */
    @Test
    fun `구독이 끊겼다 다시 붙으면 목록을 재조회한다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 95L))

        val viewModel = createViewModel()

        val subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        assertEquals(1, fakeChallengeRepo.callCount)

        // 맹세 화면 push 로 홈 컴포지션이 dispose 되는 상황. stopTimeout 이 0이라도 STOP 은
        // 스케줄러를 한 번 돌려야 실제로 발화하므로 runCurrent 가 필요하다.
        subscription.cancelAndJoin()
        runCurrent()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        runCurrent()

        assertEquals(2, fakeChallengeRepo.callCount)
    }

    /**
     * 알림 목록이 진입만으로 전부 읽음 처리를 하는데, 홈에는 그 사실을 전해 받을 콜백이 없다.
     * 돌아온 홈이 안 읽은 개수를 서버에서 다시 받는 것이 뱃지가 꺼지는 유일한 근거다.
     */
    @Test
    fun `알림 목록에서 돌아오면 홈이 안 읽은 개수를 다시 받아 뱃지를 끈다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeNotification.unreadCount = 3

        val viewModel = createViewModel()

        val subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        assertTrue((viewModel.uiState.value as HomeUiState.Data).hasUnreadNotification)
        assertEquals(1, fakeNotification.callCount)

        // 알림 목록으로 push 되면 홈 컴포지션이 dispose 된다. STOP 은 스케줄러를 한 번 돌려야
        // 실제로 발화하므로 runCurrent 가 필요하다.
        subscription.cancelAndJoin()
        runCurrent()
        fakeNotification.unreadCount = 0

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        runCurrent()

        assertEquals(2, fakeNotification.callCount)
        val state = viewModel.uiState.value as HomeUiState.Data
        assertEquals(0, state.unreadCount)
        assertFalse(state.hasUnreadNotification)
    }

    @Test
    fun `restart 하면 구독을 유지한 채 목록을 재조회하고 직전 값을 잃지 않는다`() = runTest {
        fakeRecord.record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3)
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(receivedChallenge(challengeId = 96L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertEquals(1, fakeChallengeRepo.callCount)

        viewModel.uiState.restart()
        runCurrent()

        assertEquals(2, fakeChallengeRepo.callCount)
        // STOP 은 replay 캐시를 비우지 않으므로 재조회 중에도 Loading 으로 되돌아가지 않는다.
        val state = viewModel.uiState.value
        assertIs<HomeUiState.Data>(state)
        assertEquals(7, state.record.win)
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

    // isDebug 는 테스트 실행 타깃의 빌드 타입에 따라 달라지므로 양쪽 분기를 모두 고정한다.
    @Test
    fun `userInfo 가 있으면 debug 빌드에서만 debugAccountLabel 이 닉네임이다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeUserInfo.seedCache(
            UserInfo(id = 12, kakaoId = 999, nickname = "이우건", profileImageUrl = null),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        if (isDebug) {
            assertEquals("이우건", state.debugAccountLabel)
        } else {
            assertNull(state.debugAccountLabel)
        }
    }

    @Test
    fun `userInfo 가 null 이면 debugAccountLabel 도 null 이다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertNull(state.userInfo)
        assertNull(state.debugAccountLabel)
    }

    @Test
    fun `마감까지 1시간 이하면 isUrgent 이고 넘으면 아니다`() = runTest {
        val now = nowKst()
        fakeRecord.record = UserRecord.Empty
        fakeChallenges.challenges = emptyList()
        fakeChallengeRepo.challenges = listOf(
            receivedChallenge(challengeId = 91L, deadline = now.plus(30.minutes)),
            receivedChallenge(challengeId = 92L, deadline = now.plus(3.hours)),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val items = (viewModel.uiState.value as HomeUiState.Data).receivedChallenges

        assertTrue(items.first { it.challengeId == 91L }.isUrgent)
        assertFalse(items.first { it.challengeId == 92L }.isUrgent)
    }

    @Test
    fun `진행 중과 판정 완료가 섞여 오면 두 목록으로 정확히 갈린다`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeChallenges.challenges = listOf(
            activeChallenge(challengeId = 10L),
            completedChallenge(challengeId = 20L, outcome = ChallengeOutcome.WIN),
            activeChallenge(challengeId = 30L),
            completedChallenge(challengeId = 40L, outcome = ChallengeOutcome.BOTH_LOSE),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(listOf(10L, 30L), state.inProgressChallenges.map { it.challengeId })
        assertEquals(listOf(20L, 40L), state.completedChallenges.map { it.challengeId })
        assertEquals(ChallengeOutcome.WIN, state.completedChallenges.first().outcome)
    }

    /**
     * 판정된 카드가 같은 배열에 섞여 온다는 이유만으로 진행 중 빈 상태가 사라지면 안 된다 —
     * 빈 상태 문구가 "진행 중인 챌린지가 없어요" 라 화면과 문구가 어긋난다.
     */
    @Test
    fun `진행 중 0건이어도 판정 완료가 있으면 emptyType 은 NO_ACTIVE_CHALLENGE`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeChallenges.challenges = listOf(
            completedChallenge(challengeId = 21L),
            completedChallenge(challengeId = 22L, outcome = ChallengeOutcome.DRAW),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(HomeEmptyType.NO_ACTIVE_CHALLENGE, state.emptyType)
        assertTrue(state.inProgressChallenges.isEmpty())
        assertEquals(2, state.completedChallenges.size)
    }

    @Test
    fun `전적 0 + 진행 중 0 + 받은 도전장 0 이어도 최근 결과가 있으면 FIRST_USER 가 아니다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeChallengeRepo.challenges = emptyList()
        fakeChallenges.challenges = listOf(completedChallenge(challengeId = 23L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(HomeEmptyType.NO_ACTIVE_CHALLENGE, state.emptyType)
    }

    // 정렬 책임은 서버에 있다. 분할이 안정적이지 않으면 서버가 정한 순서가 화면에서 뒤집힌다.
    @Test
    fun `서버가 준 순서가 각 구획 안에서 보존된다`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeChallenges.challenges = listOf(
            completedChallenge(challengeId = 70L),
            activeChallenge(challengeId = 71L),
            completedChallenge(challengeId = 72L, outcome = ChallengeOutcome.LOSE),
            activeChallenge(challengeId = 73L),
            activeChallenge(challengeId = 74L),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as HomeUiState.Data

        assertEquals(listOf(71L, 73L, 74L), state.inProgressChallenges.map { it.challengeId })
        assertEquals(listOf(70L, 72L), state.completedChallenges.map { it.challengeId })
        // 원본은 손대지 않는다.
        assertEquals(
            listOf(70L, 71L, 72L, 73L, 74L),
            state.challenges.map { it.challengeId },
        )
    }
}
