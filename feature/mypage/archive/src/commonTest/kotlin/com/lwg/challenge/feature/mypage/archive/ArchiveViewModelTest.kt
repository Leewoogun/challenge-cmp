package com.lwg.challenge.feature.mypage.archive

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.usecase.GetChallengeArchiveUseCase
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveContent
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

    /**
     * 가상시간을 [millis] 만큼 진행시키고, 정확히 그 시각에 예약된 작업까지 실행한다.
     *
     * [advanceTimeBy] 는 `현재 + millis` 에 예약된 작업을 **실행하지 않는다** (coroutines 1.7 이후).
     * 로딩 힌트 테스트가 재는 것은 경계 시각 그 자체(300ms)라, [runCurrent] 를 붙이지 않으면
     * 검증하려는 그 send 가 통째로 빠져 "아무 일도 안 일어났다" 로 읽힌다.
     */
    private fun TestScope.advanceTime(millis: Long) {
        advanceTimeBy(millis)
        runCurrent()
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

    // **신고된 버그의 회귀 테스트다.** 하한이 '최초 기록월' 이던 시절, 기록이 전부 이번 달인 계정
    // — 신규 서비스에서 가장 흔한 초기 상태다 — 은 하한과 상한이 같은 달로 겹쳐 양쪽 화살표가 동시에
    // 죽었다. 카드는 보이는데 아무것도 움직이지 않아 사용자가 화면을 고장으로 읽었다.
    //
    // firstArchivedMonth 가 이번 달과 같아도 `<` 는 **실제로 직전 달을 조회해야** 한다. 그 값이
    // 다시 하한으로 쓰이는 순간 이 단언이 깨진다.
    @Test
    fun `기록이 전부 이번 달이어도 이전 달로 갈 수 있다`() = runTest {
        fakeChallenge.firstArchivedMonth = YearMonth(2026, 8)
        fakeChallenge.histories = listOf(history(1L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.onPreviousMonthClick()
        runCurrent()

        assertEquals(listOf(null, YearMonth(2026, 7)), fakeChallenge.requestedMonths)
        assertEquals("2026년 7월", viewModel.uiState.value.month?.label)
    }

    // 위와 같은 규칙의 반대쪽 끝. 기록이 아예 없으면 하한으로 삼을 월조차 없는데, 그 상태에서
    // 이동이 막히면 사용자는 첫 화면에서 아무 데도 갈 수 없다.
    @Test
    fun `기록이 하나도 없어도 이전 달로 갈 수 있다`() = runTest {
        fakeChallenge.firstArchivedMonth = null

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.onPreviousMonthClick()
        runCurrent()

        assertEquals(listOf(null, YearMonth(2026, 7)), fakeChallenge.requestedMonths)
    }

    // 이 테스트의 이전 판은 옛 달을 100ms 만 붙들고 놓았다 — 그 뒤에는 라벨이 새 달로 가고 본문이
    // 비워졌다. 응답보다 짧은 붙듦은 언제든 빈 화면을 내놓기 때문에 **붙드는 시간의 상한 자체를
    // 없앴다.** 지켜야 할 불변식은 그대로 **"라벨과 본문이 언제나 같은 달"** 이고, 달라진 것은
    // 옛 짝을 놓는 시점이 시계가 아니라 **응답 도착**이라는 점뿐이다.
    //
    // 요지는 라벨만 바뀌고 본문이 남는 조합이 아니라는 것이다 — 기다리는 동안에는 **둘 다** 옛 달이고,
    // 놓는 순간 **둘 다** 새 달이 된다.
    @Test
    fun `전환 지연 동안에는 옛 달이 통째로 유지된다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val beforeTap = viewModel.uiState.value
        assertEquals("2026년 8월", beforeTap.month?.label)
        assertIs<ArchiveContent.Data>(beforeTap.content)

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))
        viewModel.onPreviousMonthClick()
        runCurrent()

        // 라벨도 본문도 손대지 않았으므로 탭 직전 상태와 완전히 같은 값이어야 한다.
        assertEquals(beforeTap, viewModel.uiState.value)

        // 응답이 끝내 오지 않아도 놓지 않는다. 힌트가 뜬 뒤로도(300ms) 라벨과 본문은 계속 8월이고,
        // 바뀌는 것은 건수 슬롯 하나뿐이다.
        advanceTime(10_000)

        val waiting = viewModel.uiState.value
        assertEquals(beforeTap.month, waiting.month)
        assertEquals(beforeTap.content, waiting.content)
    }

    // **깜빡임 제거의 회귀 테스트다.** 응답이 힌트 시각 전에 도착하면 옛 짝에서 새 짝으로 곧장
    // 넘어가야 한다 — 그 사이에 Blank 든 Loading 이든 힌트든 한 번이라도 끼면 그것이 사용자가 신고한
    // 깜빡임이다. 실제 로컬 응답이 3~49ms 라 이 경로가 월 전환의 거의 전부다.
    //
    // 두 awaitItem 사이에 아무것도 없다는 것이 단언의 핵심이다. 중간 상태가 하나라도 방출되면
    // 두 번째 awaitItem 이 그것을 집어 라벨 단언에서 걸린다.
    //
    // 마지막의 시간 진행은 힌트 코루틴이 정말 죽었는지 묻는다. 살아남았다면 이미 7월 카드가 그려진
    // 뒤에 "불러오는 중" 이 뒤늦게 도착해 화면이 되레 뒤로 간다.
    @Test
    fun `응답이 빠르면 중간 상태 없이 새 달로 바뀐다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))
        fakeChallenge.respondTo(
            month = YearMonth(2026, 7),
            histories = listOf(
                history(2L, LocalDate(2026, 7, 3)),
                history(3L, LocalDate(2026, 7, 9)),
            ),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.uiState.test {
            val before = awaitItem()
            assertEquals("2026년 8월", before.month?.label)
            assertIs<ArchiveContent.Data>(before.content)
            assertEquals("1건", before.countText)

            viewModel.onPreviousMonthClick()
            runCurrent()

            val after = awaitItem()
            assertEquals("2026년 7월", after.month?.label)
            val content = assertIs<ArchiveContent.Data>(after.content)
            assertEquals(listOf(2L, 3L), content.challenges.map { it.challengeId })
            // 건수도 옛 건수에서 새 건수로 곧장 간다 — 힌트를 거치지 않는다.
            assertFalse(after.isTransitioning)
            assertEquals("2건", after.countText)

            advanceTime(1_000)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 응답이 늦을 때 얹는 것은 **건수 슬롯 하나**다. 라벨까지 새 달로 앞서 가면 그 순간 화면이
    // `7월 라벨 + 8월 카드` 로 두 달을 말하게 되고, 본문을 덮으면 그게 방금 걷어낸 빈 화면이다.
    //
    // 세 단언을 한 테스트에 묶어 둔 이유가 그것이다 — 힌트가 떴다는 사실만 따로 확인하면 라벨과
    // 본문이 함께 끌려간 회귀를 놓친다. **바뀐 것이 건수뿐이라는 것**이 명제다.
    @Test
    fun `응답이 늦으면 건수 슬롯만 불러오는 중으로 바뀐다`() = runTest {
        fakeChallenge.histories = listOf(history(1L), history(2L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val beforeTap = viewModel.uiState.value

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))
        viewModel.onPreviousMonthClick()
        runCurrent()
        advanceTime(300)

        val state = viewModel.uiState.value
        assertEquals("불러오는 중", state.countText)
        assertEquals("2026년 8월", state.month?.label)
        assertEquals(beforeTap.content, state.content)
    }

    // 힌트는 응답이 정말 늦을 때만 얹는다. 그 전에 화면이 조금이라도 움직이면 빠른 응답(로컬 3~49ms)
    // 마다 흔적이 남아, 없애려던 깜빡임이 자리만 건수 슬롯으로 옮긴 꼴이 된다.
    @Test
    fun `삼백 밀리초 전에는 화면이 전혀 바뀌지 않는다`() = runTest {
        fakeChallenge.histories = listOf(history(1L), history(2L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val beforeTap = viewModel.uiState.value
        assertEquals("2건", beforeTap.countText)

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))
        viewModel.onPreviousMonthClick()
        runCurrent()
        advanceTime(299)

        val state = viewModel.uiState.value
        assertEquals(beforeTap, state)
        assertEquals("2건", state.countText)
    }

    // **이 테스트의 결론은 뒤집힌 것이다.** 이전 판은 전환 실패가 `7월 라벨 + Failure` 로 한 번에
    // 간다고 단언했다. 실패가 본문 전체를 덮어도 된다는 근거는 *"잃을 성공 데이터가 애초에 없다"*
    // 였는데, **월 전환에서는 그 전제가 성립하지 않는다** — 화면에는 8월의 성공한 목록이 그대로 있고
    // 실패 카드는 정확히 그것을 지운다. 그래서 화면은 8월에 남고 실패는 스낵바로만 나간다.
    //
    // 화면이 실패를 말할 자리가 없으니 스낵바가 **유일한 채널**이다. 발행 자체가 빠지면 사용자는
    // 화살표가 먹지 않았다고 읽는다 — 그래서 "덮지 않는다" 와 "알린다" 를 함께 단언한다.
    @Test
    fun `전환에 실패하면 화면을 덮지 않고 스낵바로 알린다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))
        fakeChallenge.respondTo(month = YearMonth(2026, 7), error = "목록을 불러오지 못했어요")

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            assertEquals("2026년 8월", viewModel.uiState.value.month?.label)

            viewModel.onPreviousMonthClick()
            runCurrent()

            val effect = awaitItem()
            assertIs<ArchiveUiEffect.ShowMessage>(effect)
            assertEquals("목록을 불러오지 못했어요", effect.message)

            val state = viewModel.uiState.value
            assertEquals("2026년 8월", state.month?.label)
            val content = assertIs<ArchiveContent.Data>(state.content)
            assertEquals(listOf(1L), content.challenges.map { it.challengeId })

            // 되돌림이 8월을 한 번 더 조회하지만 그 성공은 이미 보고 있는 화면이라 말할 것이 없다.
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // **되돌림(revertSelectionTo)의 회귀 테스트다.** 실패한 전환은 화면을 8월에 남기는데, 선택
    // 자체를 7월에 둔 채로 두면 화면과 선택이 어긋난다 — 사용자가 `<` 를 다시 눌러도 계산 결과가
    // 또 7월이라 StateFlow 동일값 필터에 걸려 **요청조차 나가지 않는다.** 재시도 수단이 통째로 죽고,
    // 화면상으로는 화살표가 고장 난 것과 구별되지 않는다.
    //
    // 되돌림이 8월을 한 번 더 조회하므로 요청 목록에 8월이 끼어든다. 정확한 순서 대신
    // **7월이 두 번 들어있는지**만 본다 — 여기서 지키려는 것은 재조회가 일어났다는 사실 하나다.
    @Test
    fun `전환 실패 후 같은 방향을 다시 눌러도 재조회된다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))
        fakeChallenge.respondTo(month = YearMonth(2026, 7), error = "목록을 불러오지 못했어요")

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.onPreviousMonthClick()
        runCurrent()
        assertEquals(1, fakeChallenge.requestedMonths.count { it == YearMonth(2026, 7) })

        viewModel.onPreviousMonthClick()
        runCurrent()

        assertEquals(2, fakeChallenge.requestedMonths.count { it == YearMonth(2026, 7) })
    }

    // 건수 공백("3건" → "" → "2건")도 같은 깜빡임이었다. 라벨이 8월에 머무는 동안 건수는 옛 값
    // 그대로고, 힌트가 뜰 때는 빈 문자열이 아니라 "불러오는 중" 으로 바뀐다 — **한 순간도 비지
    // 않는다.** 경계(299 → 300)를 한 걸음씩 넘는 이유가 그 틈을 찾기 위해서다.
    //
    // 힌트 문구가 건수를 주장하지 않는 문구인 것도 같은 이유다. 옛 *건수 숫자*를 힌트 자리까지
    // 끌고 가면 새 달 라벨이 붙는 순간 `7월 라벨 + 8월 건수` 가 되는데, 이 화면은 라벨을 앞세우지
    // 않으므로 그 조합 자체가 생기지 않는다.
    @Test
    fun `전환 지연 동안 건수가 비지 않는다`() = runTest {
        fakeChallenge.histories = listOf(history(1L), history(2L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertEquals("2건", viewModel.uiState.value.countText)

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))
        viewModel.onPreviousMonthClick()
        runCurrent()
        assertEquals("2건", viewModel.uiState.value.countText)

        advanceTime(299)
        assertEquals("2건", viewModel.uiState.value.countText)

        advanceTime(1)
        assertEquals("불러오는 중", viewModel.uiState.value.countText)
    }

    // **`Loading` 이 첫 로딩 전용으로 좁아진 것이 이번 개정의 산물이다.** 월 전환에서 스피너를 다시
    // 띄우면 본문이 통째로 갈려 나가고, 그게 방금 걷어낸 깜빡임 그대로다 — 되살리는 순간 여기서 걸린다.
    // 지금까지 이 파일에는 Loading 을 단언하는 테스트가 하나도 없어 그 좁아짐을 붙드는 짝이 없었다.
    //
    // 두 사실을 한 테스트에 묶은 이유: *"첫 로딩 전용"* 은 **대조로만 성립하는 주장**이다. 한쪽만 보면
    // "스피너가 있다" / "스피너가 없다" 중 하나로만 읽혀 '전용' 이라는 말이 통째로 빠진다.
    //
    // ⚠️ 순서를 뒤집은 것은 의도다. 첫 로딩을 보류시킨 ViewModel 은 month 가 null 이라 화살표가 먹지
    // 않아(moveTo 가 커서 없이 돌아온다) 그 ViewModel 로는 월 전환을 관찰할 수 없다. 전환을 먼저 보고,
    // 첫 로딩은 새 ViewModel 로 본다.
    @Test
    fun `첫 로딩에서만 스피너가 나온다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))

        viewModel.uiState.test {
            val beforeTap = awaitItem()
            assertIs<ArchiveContent.Data>(beforeTap.content)

            viewModel.onPreviousMonthClick()
            runCurrent()
            // 힌트 시각(300ms)을 한참 넘긴다 — 늦게 스피너로 갈리는 경로도 없다는 뜻이다.
            advanceTime(10_000)

            // 응답이 끝내 오지 않아도 방출은 힌트 하나뿐이다. 중간에 스피너가 한 번이라도 끼면
            // 이 awaitItem 이 그것을 집어 아래 단언에서 걸린다.
            val waiting = awaitItem()
            assertIs<ArchiveContent.Data>(waiting.content)
            assertEquals(beforeTap.content, waiting.content)
            // 본문 대신 건수 슬롯 한 줄만 "오는 중" 을 말한다.
            assertEquals("불러오는 중", waiting.countText)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        // 반대쪽 끝. 첫 로딩만 즉시 스피너인 이유는 **감출 이전 달이 애초에 없다**는 것뿐이다 —
        // 붙들 옛 화면이 있는 전환과 달리 여기서는 본문 전체가 로딩을 말하는 것 말고 할 말이 없다.
        fakeChallenge.neverRespondTo(fakeChallenge.thisMonth)
        val firstLoad = createViewModel()
        subscribeUiState(firstLoad)

        val loading = firstLoad.uiState.value
        assertIs<ArchiveContent.Loading>(loading.content)
        // 첫 응답 전에는 이번 달조차 모르므로 월 이동 바가 없다 — 위 전환 구간과 달리 붙들 바 자체가 없다.
        assertNull(loading.month)
    }

    // 연타로 요청이 겹치면 응답 순서가 뒤집혀 "바는 6월인데 목록은 7월" 이 될 수 있다.
    // 그리기 직전에 에코를 한 번 대조하는 것으로 그 조합을 막는다.
    //
    // 어긋난 에코는 **아무것도 도착하지 않은 것**과 같게 다뤄지므로 화면은 8월에 그대로 남는다.
    // 그래서 단언의 무게는 라벨이 아니라 **목록 내용**에 있다 — 대조를 빼면 7월 응답이 `cursorFor`
    // 를 타고 8월 라벨을 단 채 그려져, 라벨만 보면 지금과 구별되지 않는다.
    @Test
    fun `에코가 어긋난 응답은 버린다`() = runTest {
        fakeChallenge.histories = listOf(history(9L))

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
        assertEquals("2026년 8월", state.month?.label)
        val content = assertIs<ArchiveContent.Data>(state.content)
        assertEquals(listOf(9L), content.challenges.map { it.challengeId })
    }

    // 1차는 이펙트를 viewModelScope.launch 로 분리해 emit 했는데, 그 코루틴이 flatMapLatest 취소를
    // 타지 않아 8월의 부분 실패 스낵바가 9월 화면 위에 뜰 수 있었다. 그 회귀를 막는다.
    //
    // 전환 지연이 생기면서 `Data 인가` 만으로는 7월이 도착했다는 증거가 못 된다 — 붙드는 동안의
    // 8월 카드도 Data 라 그냥 통과한다. 월이 실제로 넘어간 뒤에 스낵바를 확인해야 의미가 있으므로
    // 목록 내용까지 대조한다.
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

            val content = assertIs<ArchiveContent.Data>(viewModel.uiState.value.content)
            assertEquals(listOf(2L), content.challenges.map { it.challengeId })
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

    // 둘 다 Data(빈 목록) 이라 본문만으로는 갈리지 않는다. CTA 유무를 가르는 것은 showsNoRecordsEver
    // 하나뿐이고 — 합치면 기록이 있는 사용자에게 "아직 끝낸 챌린지가 없어요" 라고 거짓말한다.
    // 신호가 커서의 earliest → hasArchivedRecords → 이 계산 프로퍼티로 두 번 이사했을 뿐 사라진 적은 없다.
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

        assertTrue(neverArchivedState.showsNoRecordsEver)
        assertFalse(emptyMonthState.showsNoRecordsEver)
    }

    // CTA 발동이 이번 달로 좁아졌다. "챌린지 만들기" 는 지금 행동할 수 있는 자리에서만 맞는 말이고,
    // 과거 달을 들여다보는 사용자는 회고 중이다. 같은 계정·같은 0건인데 보고 있는 달만으로 화면이
    // 갈린다는 것이 이 테스트의 요점이다.
    @Test
    fun `기록 0건이어도 과거 달에서는 CTA 화면이 아니다`() = runTest {
        fakeChallenge.firstArchivedMonth = null
        fakeChallenge.histories = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertTrue(viewModel.uiState.value.showsNoRecordsEver)

        viewModel.onPreviousMonthClick()
        runCurrent()

        val state = viewModel.uiState.value
        assertIs<ArchiveContent.Data>(state.content)
        assertFalse(state.showsNoRecordsEver)
    }

    // 첫 기록보다 이전 방향에는 영원히 아무것도 없다. 차단이 있던 시절엔 비활성 화살표가 그 사실을
    // 말했고, 차단을 없앤 지금은 부제 한 줄이 그 역할을 받는다 — "다른 달을 볼 수 있어요" 를 그대로
    // 쓰면 더 가면 뭔가 있다는 거짓말이 된다.
    //
    // 이 단언이 firstArchivedMonth 를 Boolean 으로 되돌리지 못하게 붙드는 자리이기도 하다.
    @Test
    fun `첫 기록보다 이전 달이면 부제가 첫 기록 월을 알린다`() = runTest {
        fakeChallenge.firstArchivedMonth = YearMonth(2026, 5)
        fakeChallenge.histories = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        // 8월 → 6월. 아직 기록 구간 안이라 A형이다.
        repeat(2) {
            viewModel.onPreviousMonthClick()
            runCurrent()
        }
        assertEquals(YearMonth(2026, 6), viewModel.uiState.value.month?.selected)
        assertEquals("화살표로 다른 달을 볼 수 있어요", viewModel.uiState.value.emptyMonthSubtitle)

        // 6월 → 3월. 첫 기록을 지나쳤으므로 B형으로 갈린다.
        repeat(3) {
            viewModel.onPreviousMonthClick()
            runCurrent()
        }
        assertEquals(YearMonth(2026, 3), viewModel.uiState.value.month?.selected)
        assertEquals("첫 기록은 2026년 5월이에요", viewModel.uiState.value.emptyMonthSubtitle)
    }

    // 이번 달을 서버 에코로 알기 때문에 첫 응답이 실패하면 어느 달의 실패인지도 모른다 —
    // 라벨도 경계도 참인 값이 하나도 없어 월 이동 바를 그리지 않는다.
    //
    // 실패 카드가 남은 유일한 자리가 여기다. 그 자리에는 월 이동 바가 없으므로 **실패 화면을 띄운 채
    // 달을 바꾸는 상황은 구성 자체가 불가능**하고, 전환 실패는 화면을 덮지 않는다
    // (`전환에 실패하면 화면을 덮지 않고 스낵바로 알린다`). 그래서 첫 로딩만 실패 카드를 쓴다는
    // 사실을 이 테스트가 붙든다.
    //
    // 전환 힌트도 붙지 않는다 — 유지할 이전 달이 없어 본문 전체가 로딩·실패를 말하는 층이다.
    @Test
    fun `첫 로딩이 실패하면 month 가 null 이다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val state = viewModel.uiState.value
        assertIs<ArchiveContent.Failure>(state.content)
        assertNull(state.month)
        assertFalse(state.isTransitioning)
        assertEquals("", state.countText)
    }

    // 실패한 달에 갇히면 안 된다. 이전 판은 실패 화면이 7월에 머무는 것을 전제로 "옆 달로 넘어갈 수
    // 있다" 를 봤는데, 지금은 실패해도 화면이 8월에 남으므로 **갇힐 자리가 애초에 없다.** 남은 질문은
    // 그다음이다 — 그 달이 살아났을 때 사용자가 정말 도달할 수 있는가.
    //
    // 되돌림·재조회·교체가 한 줄로 이어지는 유일한 경로라, 실패가 이동을 **영구히** 막지 않는다는
    // 것을 여기서 끝까지 확인한다.
    @Test
    fun `전환에 실패해도 그 달로 다시 갈 수 있다`() = runTest {
        fakeChallenge.histories = listOf(history(1L))
        fakeChallenge.respondTo(month = YearMonth(2026, 7), error = "목록을 불러오지 못했어요")

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.onPreviousMonthClick()
        runCurrent()
        assertEquals("2026년 8월", viewModel.uiState.value.month?.label)

        fakeChallenge.respondTo(
            month = YearMonth(2026, 7),
            histories = listOf(history(2L, LocalDate(2026, 7, 3))),
        )
        viewModel.onPreviousMonthClick()
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("2026년 7월", state.month?.label)
        val content = assertIs<ArchiveContent.Data>(state.content)
        assertEquals(listOf(2L), content.challenges.map { it.challengeId })
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
    // 재수집 응답을 보류시켜 "도중" 을 관찰한다. 재수집도 첫 로딩이 아니라 전환 경로를 타므로 화면이
    // 직전 값(8월 Data)에 그대로 머물고, 재조회 중이라는 흔적은 힌트 시각(300ms)에야 건수 슬롯에
    // 나타난다 — 그 시점까지 진행시켜야 "도중" 이 관측된다. 이 테스트가 지키는 것은 그 도중에도
    // **바가 살아 있다**는 것 하나다.
    @Test
    fun `재시도해도 월 이동 바가 사라지지 않는다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        fakeChallenge.neverRespondTo(YearMonth(2026, 8))
        viewModel.retry()
        runCurrent()
        advanceTime(300)

        val state = viewModel.uiState.value
        assertTrue(state.isTransitioning)
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
