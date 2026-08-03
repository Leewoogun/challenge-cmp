package com.lwg.challenge.feature.challenge.create

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.domain.usecase.CreateChallengeUseCase
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiEffect
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeCreateViewModelTest {

    private lateinit var fakeFriendsRepository: FakeFriendsRepository
    private lateinit var fakeChallengeRepository: FakeChallengeRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeFriendsRepository = FakeFriendsRepository()
        fakeChallengeRepository = FakeChallengeRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ChallengeCreateViewModel =
        ChallengeCreateViewModel(
            friendsRepository = fakeFriendsRepository,
            createChallengeUseCase = CreateChallengeUseCase(fakeChallengeRepository),
        )

    // uiState 가 WhileSubscribed 이므로 구독자가 있어야 파이프라인이 시작된다.
    private fun TestScope.subscribeUiState(viewModel: ChallengeCreateViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun ChallengeCreateViewModel.data(): ChallengeCreateUiState.Data =
        assertIs<ChallengeCreateUiState.Data>(uiState.value)

    private fun sampleFriend(id: Long, nickname: String = "친구$id") = Friend(
        userId = id,
        nickname = nickname,
        profileImageUrl = null,
        since = LocalDateTime.parse("2026-01-01T00:00:00"),
    )

    private fun sampleSignature() = Signature(
        strokes = listOf(
            SignatureStroke(listOf(SignaturePoint(10, 20), SignaturePoint(30, 40))),
        ),
    )

    @Test
    fun `init 시 친구 목록을 1회 조회하고 상대 선택 단계로 진입한다`() = runTest {
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(1), sampleFriend(2))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertEquals(1, fakeFriendsRepository.callCount)
        val state = viewModel.data()
        assertEquals(ChallengeCreateStep.FRIEND_PICK, state.step)
        assertEquals(listOf(1L, 2L), state.friends.map { it.userId })
    }

    @Test
    fun `친구가 0명이면 빈 상태이고 로드 실패는 아니다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val state = viewModel.data()
        assertTrue(state.isFriendsEmpty)
        assertFalse(state.loadFailed)
    }

    @Test
    fun `친구 목록 로드 실패 시 Loading 에 갇히지 않고 loadFailed 와 함께 Data 로 전이한다`() = runTest {
        fakeFriendsRepository.error = "네트워크 오류가 발생하였습니다."

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)

            val effect = awaitItem()
            assertIs<ChallengeCreateUiEffect.ShowMessage>(effect)
            assertEquals("네트워크 오류가 발생하였습니다.", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.data()
        assertTrue(state.loadFailed)
        assertFalse(state.isFriendsEmpty)
    }

    @Test
    fun `retryLoadFriends 는 목록을 재조회하고 loadFailed 를 되돌린다`() = runTest {
        fakeFriendsRepository.error = "네트워크 오류가 발생하였습니다."

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertTrue(viewModel.data().loadFailed)

        fakeFriendsRepository.error = null
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(1))
        viewModel.retryLoadFriends()

        assertEquals(2, fakeFriendsRepository.callCount)
        val state = viewModel.data()
        assertFalse(state.loadFailed)
        assertEquals(listOf(1L), state.friends.map { it.userId })
    }

    @Test
    fun `selectFriend 는 선택을 저장하고 미션 입력 단계로 넘어간다`() = runTest {
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(7))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(7L)

        val state = viewModel.data()
        assertEquals(7L, state.selectedFriendId)
        assertEquals(ChallengeCreateStep.MISSION_INPUT, state.step)
    }

    @Test
    fun `goToPreviousStep 은 상대 선택 단계로 돌아가되 선택값을 유지한다`() = runTest {
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(7))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(7L)
        viewModel.goToPreviousStep()

        val state = viewModel.data()
        assertEquals(ChallengeCreateStep.FRIEND_PICK, state.step)
        assertEquals(7L, state.selectedFriendId)
    }

    @Test
    fun `미션이 공백뿐이면 맹세 단계로 넘어갈 수 없다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.updateMyMission("   ")
        viewModel.updateBetContent("커피 사기")

        assertFalse(viewModel.data().canProceedToOath)
    }

    @Test
    fun `내기를 입력하지 않으면 맹세 단계로 넘어갈 수 없다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.updateMyMission("오늘 운동 1시간 하기")

        assertFalse(viewModel.data().canProceedToOath)
    }

    @Test
    fun `미션과 내기가 모두 유효하면 맹세 단계로 넘어갈 수 있다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")

        assertTrue(viewModel.data().canProceedToOath)
    }

    @Test
    fun `goToOathStep 은 미션과 내기가 유효할 때만 맹세 단계로 넘어간다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(7L)

        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.goToOathStep()
        assertEquals(ChallengeCreateStep.MISSION_INPUT, viewModel.data().step)

        viewModel.updateBetContent("커피 사기")
        viewModel.goToOathStep()
        assertEquals(ChallengeCreateStep.OATH, viewModel.data().step)
    }

    @Test
    fun `goToPreviousStep 은 맹세에서 미션 입력 그리고 상대 선택으로 한 단계씩 내려간다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(7L)
        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")
        viewModel.goToOathStep()
        assertEquals(ChallengeCreateStep.OATH, viewModel.data().step)

        viewModel.goToPreviousStep()
        assertEquals(ChallengeCreateStep.MISSION_INPUT, viewModel.data().step)

        viewModel.goToPreviousStep()
        assertEquals(ChallengeCreateStep.FRIEND_PICK, viewModel.data().step)

        // 첫 단계에서는 더 내려갈 곳이 없다 — 화면을 벗어나는 건 Route 의 popBackStack 책임이다.
        viewModel.goToPreviousStep()
        assertEquals(ChallengeCreateStep.FRIEND_PICK, viewModel.data().step)
    }

    @Test
    fun `서명이 비어 있으면 제출할 수 없고 획을 하나 그으면 제출할 수 있다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")

        assertFalse(viewModel.data().canSubmit)

        viewModel.updateSignature(sampleSignature())

        assertTrue(viewModel.data().canSubmit)
    }

    @Test
    fun `clearSignature 후에는 제출할 수 없다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")
        viewModel.updateSignature(sampleSignature())
        assertTrue(viewModel.data().canSubmit)

        viewModel.clearSignature()

        val state = viewModel.data()
        assertTrue(state.signature.isEmpty)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `서명이 비어 있으면 submit 해도 생성 요청이 나가지 않는다`() = runTest {
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(9))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(9L)
        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")

        viewModel.submit()

        assertTrue(fakeChallengeRepository.createCalls.isEmpty())
        assertFalse(viewModel.data().isSubmitting)
    }

    @Test
    fun `updateMyMission 은 100자를 넘는 입력을 잘라낸다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.updateMyMission("가".repeat(101))

        assertEquals(100, viewModel.data().myMission.length)
    }

    @Test
    fun `마감 기본값은 오늘이고 selectDeadline 으로 내일로 바꾼다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertEquals(DeadlineType.TODAY, viewModel.data().deadlineType)

        viewModel.selectDeadline(DeadlineType.TOMORROW)

        assertEquals(DeadlineType.TOMORROW, viewModel.data().deadlineType)
    }

    @Test
    fun `마감 옵션 힌트가 오늘 내일 KST 날짜로 노출된다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val state = viewModel.data()
        val hintFormat = Regex("""\d{1,2}/\d{1,2} 24:00""")
        assertTrue(hintFormat.matches(state.todayDeadlineHint), state.todayDeadlineHint)
        assertTrue(hintFormat.matches(state.tomorrowDeadlineHint), state.tomorrowDeadlineHint)
        assertTrue(state.todayDeadlineHint != state.tomorrowDeadlineHint)

        // 실제 날짜는 실행 시점마다 달라지므로 "하루 뒤" 라는 관계만 검증한다.
        val (todayMonth, todayDay) = state.todayDeadlineHint.toMonthDay()
        val (tomorrowMonth, tomorrowDay) = state.tomorrowDeadlineHint.toMonthDay()
        val isNextDay = tomorrowMonth == todayMonth && tomorrowDay == todayDay + 1
        val isFirstDayOfNextMonth = tomorrowDay == 1 && tomorrowMonth == todayMonth % 12 + 1
        assertTrue(
            isNextDay || isFirstDayOfNextMonth,
            "${state.todayDeadlineHint} -> ${state.tomorrowDeadlineHint}",
        )
    }

    private fun String.toMonthDay(): Pair<Int, Int> {
        val (month, day) = substringBefore(' ').split('/')
        return month.toInt() to day.toInt()
    }

    @Test
    fun `submit 성공 시 입력값 그대로 생성 요청하고 NavigateBack 을 발행한다`() = runTest {
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(9))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(9L)
        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")
        viewModel.selectDeadline(DeadlineType.TOMORROW)
        viewModel.updateSignature(sampleSignature())

        viewModel.uiEffect.test {
            viewModel.submit()
            assertEquals(ChallengeCreateUiEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, fakeChallengeRepository.createCalls.size)
        val input = fakeChallengeRepository.createCalls.first()
        assertEquals(9L, input.opponentId)
        assertEquals("오늘 운동 1시간 하기", input.myMission)
        assertEquals("커피 사기", input.betContent)
        assertEquals(DeadlineType.TOMORROW, input.deadlineType)
        assertEquals(sampleSignature(), input.signature)
    }

    @Test
    fun `submit 실패 시 메시지를 노출하고 제출 상태와 입력값을 되돌린다`() = runTest {
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(9))
        fakeChallengeRepository.createError = "친구가 아닌 사용자입니다."

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(9L)
        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")
        viewModel.updateSignature(sampleSignature())

        viewModel.uiEffect.test {
            viewModel.submit()
            val effect = awaitItem()
            assertIs<ChallengeCreateUiEffect.ShowMessage>(effect)
            assertEquals("친구가 아닌 사용자입니다.", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.data()
        assertFalse(state.isSubmitting)
        assertEquals("오늘 운동 1시간 하기", state.myMission)
        assertEquals("커피 사기", state.betContent)
        assertEquals(ChallengeCreateStep.MISSION_INPUT, state.step)
    }

    @Test
    fun `submit 을 연속 호출해도 생성 요청은 1회만 나간다`() = runTest {
        fakeFriendsRepository.friendsFlow.value = listOf(sampleFriend(9))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.selectFriend(9L)
        viewModel.updateMyMission("오늘 운동 1시간 하기")
        viewModel.updateBetContent("커피 사기")
        viewModel.updateSignature(sampleSignature())

        viewModel.submit()
        viewModel.submit()

        assertEquals(1, fakeChallengeRepository.createCalls.size)
    }
}
