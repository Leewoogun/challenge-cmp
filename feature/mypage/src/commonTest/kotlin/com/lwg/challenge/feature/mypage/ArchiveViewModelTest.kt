package com.lwg.challenge.feature.mypage

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.usecase.GetChallengeArchiveUseCase
import com.lwg.challenge.feature.mypage.contract.ArchiveContent
import com.lwg.challenge.feature.mypage.contract.ArchiveUiEffect
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
import kotlinx.datetime.YearMonth
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {

    private lateinit var fakeChallenge: FakeChallengeRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeChallenge = FakeChallengeRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ArchiveViewModel = ArchiveViewModel(
        getChallengeArchiveUseCase = GetChallengeArchiveUseCase(fakeChallenge),
    )

    // uiState 가 WhileSubscribed 이므로 구독자가 있어야 파이프라인이 시작된다.
    // 이펙트 미발행을 검증하는 테스트는 이 호출을 `uiEffect.test { }` 안에 둬야 한다 — uiEffect 가
    // replay 0 SharedFlow 라 collector 가 붙기 전에 emit 된 이펙트는 버려져 회귀를 놓친다.
    private fun TestScope.subscribeUiState(viewModel: ArchiveViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun history(
        challengeId: Long,
        challengeDate: LocalDate = LocalDate(2026, 8, 14),
        opponentNickname: String = "준혁",
    ) = ChallengeHistory(
        challengeId = challengeId,
        challengeDate = challengeDate,
        opponentNickname = opponentNickname,
        myMission = "헬스장 가기",
        opponentMission = "금주",
        bet = "치킨 한 마리",
        myResult = ChallengeOutcome.WIN,
    )

    // 첫 요청에 month 가 붙는 순간 앱이 기기 시계로 첫 달을 정한 것이 된다 —
    // 이번 달을 서버에게서만 배운다는 규칙이 이 한 줄로 고정된다.
    @Test
    fun `첫 진입은 month 를 붙이지 않고 조회한다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertEquals(1, fakeChallenge.callCount)
        assertNull(fakeChallenge.requestedMonths.first())
    }

    // 상한이 되는 "이번 달" 은 파라미터 없이 부른 첫 응답의 에코뿐이다. 이후 `?month=` 응답의 에코로
    // 덮으면 상한이 과거로 내려가 다음 달 화살표가 엉뚱하게 열린다.
    @Test
    fun `첫 응답의 에코가 상한이 된다`() = runTest {
        fakeChallenge.thisMonth = YearMonth(2026, 8)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val month = viewModel.uiState.value.month
        assertNotNull(month)
        assertEquals("2026년 8월", month.label)
        assertFalse(month.canGoNext)
    }

    @Test
    fun `이전 달로 이동하면 그 달을 조회한다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.onPreviousMonthClick()
        runCurrent()

        assertEquals(listOf(null, YearMonth(2026, 7)), fakeChallenge.requestedMonths)
    }

    // 하한이 없다는 것은 종료한 챌린지가 하나도 없다는 뜻이라, 뒤에 아무것도 없는 달을 무한히
    // 거슬러 올라가게 두지 않는다.
    @Test
    fun `firstArchivedMonth 가 null 이면 이전으로 갈 수 없다`() = runTest {
        fakeChallenge.firstArchivedMonth = null

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val month = viewModel.uiState.value.month
        assertNotNull(month)
        assertFalse(month.canGoPrevious)
    }

    @Test
    fun `하한 달에서는 이전으로 갈 수 없다`() = runTest {
        fakeChallenge.thisMonth = YearMonth(2026, 8)
        fakeChallenge.firstArchivedMonth = YearMonth(2026, 8)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val month = viewModel.uiState.value.month
        assertNotNull(month)
        assertFalse(month.canGoPrevious)
    }

    // 라벨과 본문이 한 값에 묶여 짝으로만 바뀌므로 "7월 라벨 밑에 8월 카드" 라는 찢어진 조합이
    // 존재할 수 없다 — 그 성질을 여기서 붙든다.
    //
    // 7월 응답을 보류시킨 구간이라 본문이 Blank 인지 Loading 인지는 가상시간을 얼마나 진행했느냐에
    // 달렸다. 이 테스트가 지키려는 것은 "직전 달 카드가 남지 않는다" 하나뿐이라 Data 가 아니라는
    // 것까지만 단언한다.
    @Test
    fun `월을 바꾸면 이전 달 카드가 남지 않는다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertIs<ArchiveContent.Data>(viewModel.uiState.value.content)

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))
        viewModel.onPreviousMonthClick()
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.content !is ArchiveContent.Data)
        assertEquals("2026년 7월", state.month?.label)
    }

    // 8월을 성공시킨 뒤 재시도로 실패를 만든다 — 첫 응답이 실패하면 month 가 null 이라 월 이동 바
    // 자체가 없어 이 상황을 만들 수 없다. 실패가 특정 달에 귀속된다는 것이 이 배치의 전제다.
    //
    // 위와 같은 이유로 보류 구간의 본문은 Failure 가 아니라는 것까지만 단언한다.
    @Test
    fun `월을 바꾸면 이전 달의 실패가 남지 않는다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        fakeChallenge.error = "목록을 불러오지 못했어요"
        viewModel.retry()
        runCurrent()
        assertIs<ArchiveContent.Failure>(viewModel.uiState.value.content)

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))
        viewModel.onPreviousMonthClick()
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.content !is ArchiveContent.Failure)
        assertEquals("2026년 7월", state.month?.label)
    }

    // 연타로 요청이 겹치면 응답 순서가 뒤집혀 "바는 6월인데 목록은 7월" 이 될 수 있다.
    // 그리기 직전에 에코를 한 번 대조하는 것으로 그 조합을 막는다.
    @Test
    fun `에코가 어긋난 응답은 버린다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        fakeChallenge.respondTo(
            month = YearMonth(2026, 7),
            histories = listOf(history(1L, LocalDate(2026, 7, 3))),
            echo = YearMonth(2026, 8),
        )
        viewModel.onPreviousMonthClick()
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.content !is ArchiveContent.Data)
        assertEquals("2026년 7월", state.month?.label)
    }

    // 1차는 이펙트를 viewModelScope.launch 로 분리해 emit 했는데, 그 코루틴이 flatMapLatest 취소를
    // 타지 않아 8월의 부분 실패 스낵바가 9월 화면 위에 뜰 수 있었다. 그 회귀를 막는다.
    @Test
    fun `부분 실패 스낵바가 다음 달로 새지 않는다`() = runTest {
        fakeChallenge.respondTo(
            month = YearMonth(2026, 8),
            histories = listOf(history(1L)),
            partialError = "일부 항목을 불러오지 못했어요",
        )
        fakeChallenge.respondTo(
            month = YearMonth(2026, 7),
            histories = listOf(history(2L, LocalDate(2026, 7, 3))),
        )

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            assertIs<ArchiveUiEffect.ShowMessage>(awaitItem())

            viewModel.onPreviousMonthClick()
            runCurrent()

            assertIs<ArchiveContent.Data>(viewModel.uiState.value.content)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 빈 응답은 조회 실패가 아니라 성공한 0건이다. 둘을 같은 화면으로 접으면 실패를
    // "이 달엔 없다" 로 둔갑시키게 된다.
    @Test
    fun `빈 달은 실패가 아니다`() = runTest {
        fakeChallenge.histories = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val content = viewModel.uiState.value.content
        assertIs<ArchiveContent.Data>(content)
        assertTrue(content.isEmpty)
    }

    // 둘 다 Data(빈 목록) 이라 본문만으로는 갈리지 않는다. 신호는 커서의 earliest 하나뿐이고 그것이
    // CTA 유무를 가른다 — 합치면 기록이 있는 사용자에게 "아직 끝낸 챌린지가 없어요" 라고 거짓말한다.
    @Test
    fun `기록 전무와 빈 달이 구분된다`() = runTest {
        fakeChallenge.histories = emptyList()

        fakeChallenge.firstArchivedMonth = null
        val neverArchived = createViewModel()
        subscribeUiState(neverArchived)

        fakeChallenge.firstArchivedMonth = YearMonth(2025, 1)
        val emptyMonth = createViewModel()
        subscribeUiState(emptyMonth)

        val neverArchivedState = neverArchived.uiState.value
        val emptyMonthState = emptyMonth.uiState.value
        assertIs<ArchiveContent.Data>(neverArchivedState.content)
        assertIs<ArchiveContent.Data>(emptyMonthState.content)

        val neverArchivedMonth = neverArchivedState.month
        val emptyMonthMonth = emptyMonthState.month
        assertNotNull(neverArchivedMonth)
        assertNotNull(emptyMonthMonth)
        assertNull(neverArchivedMonth.earliest)
        assertNotNull(emptyMonthMonth.earliest)
    }

    // 이번 달을 서버 에코로 알기 때문에 첫 응답이 실패하면 어느 달의 실패인지도 모른다 —
    // 라벨도 경계도 참인 값이 하나도 없어 월 이동 바를 그리지 않는다.
    @Test
    fun `첫 로딩이 실패하면 month 가 null 이다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val state = viewModel.uiState.value
        assertIs<ArchiveContent.Failure>(state.content)
        assertNull(state.month)
    }

    // 실패한 달에 갇히면 안 된다 — 옆 달로 넘어가는 것도 복구다.
    @Test
    fun `두 번째 이후 실패에서는 월 이동이 가능하다`() = runTest {
        fakeChallenge.respondTo(month = YearMonth(2026, 7), error = "목록을 불러오지 못했어요")

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.onPreviousMonthClick()
        runCurrent()

        val state = viewModel.uiState.value
        assertIs<ArchiveContent.Failure>(state.content)
        val month = state.month
        assertNotNull(month)
        assertEquals("2026년 7월", month.label)
        assertTrue(month.canGoNext)
    }

    // 전체 실패는 실패 영역이 곧 본문 전체라 화면이 직접 실패를 말한다 — 스낵바를 겹쳐 띄우지 않는다.
    @Test
    fun `전체 실패는 스낵바를 발행하지 않는다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 부분 실패는 화면의 주 내용이 성공분 목록이라 화면이 실패를 말할 자리가 없다 — 목록과 스낵바를
    // 병행한다. onError 만 보면 전체 실패와 구분되지 않으므로 emit 이 뒤따랐다는 사실로 갈린다.
    @Test
    fun `부분 실패는 목록과 스낵바를 함께 낸다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))
        fakeChallenge.partialError = "일부 항목을 불러오지 못했어요"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)

            val effect = awaitItem()
            assertIs<ArchiveUiEffect.ShowMessage>(effect)
            assertEquals("일부 항목을 불러오지 못했어요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        val content = viewModel.uiState.value.content
        assertIs<ArchiveContent.Data>(content)
        assertEquals(listOf(1L), content.challenges.map { it.challengeId })
    }

    // 전체 실패로 보류된 메시지는 그 수집과 함께 버려지고 끝나야 한다. 보류 자리를 수집 단위(monthFlow
    // 지역 변수)가 아니라 ViewModel 프로퍼티로 올리면 값이 재수집까지 살아남아, 재수집의 첫 emit 에
    // 직전 실패 메시지가 실린다 — 성공한 재시도 위에 엉뚱한 실패 스낵바가 뜬다.
    // 위 `부분 실패 스낵바가 다음 달로 새지 않는다` 가 월 전환 방향의 누수를 막고,
    // 이 테스트가 재수집 방향을 막는다.
    @Test
    fun `전체 실패 후 재시도가 성공하면 이전 실패 메시지가 새지 않는다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            assertIs<ArchiveContent.Failure>(viewModel.uiState.value.content)

            fakeChallenge.error = null
            fakeChallenge.histories = listOf(history(1L))
            viewModel.retry()
            runCurrent()

            assertIs<ArchiveContent.Data>(viewModel.uiState.value.content)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 재수집이 선택된 달을 이번 달로 초기화하면 사용자가 화살표로 걸어 들어간 걸음이 사라진다.
    @Test
    fun `재시도는 현재 달을 다시 조회한다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.onPreviousMonthClick()
        runCurrent()
        viewModel.retry()
        runCurrent()

        assertEquals(
            listOf(null, YearMonth(2026, 7), YearMonth(2026, 7)),
            fakeChallenge.requestedMonths,
        )
    }

    // 화살표를 한 번도 누르지 않은 상태의 재시도는 여전히 month = null 로 나가지만, null 은
    // "달을 모른다" 가 아니라 "이번 달" 이고 이번 달은 첫 응답으로 이미 확정돼 있다 — 한번 생긴
    // 바가 재시도 도중에 사라져 화면이 첫 로딩으로 되돌아가면 안 된다.
    //
    // 재수집 응답을 보류시켜 "도중" 을 관찰한다. 본문이 Blank 인지 Loading 인지는 가상시간 진행에
    // 달렸으므로 Data 가 아니라는 것까지만 단언한다.
    @Test
    fun `재시도해도 월 이동 바가 사라지지 않는다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        fakeChallenge.neverRespondTo(YearMonth(2026, 8))
        viewModel.retry()
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.content !is ArchiveContent.Data)
        assertEquals("2026년 8월", state.month?.label)
    }

    // 경계 판단이 ArchiveMonthCursor 안에만 있고 ViewModel 은 "커서가 안 움직였으면 아무 일도 안 한다"
    // 만 한다 — 규칙의 사본이 두 곳에 생기면 한쪽만 고친 버그가 나온다.
    @Test
    fun `비활성 방향으로 눌러도 재조회가 일어나지 않는다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertEquals(1, fakeChallenge.callCount)

        viewModel.onNextMonthClick()
        runCurrent()

        assertEquals(1, fakeChallenge.callCount)
    }
}
