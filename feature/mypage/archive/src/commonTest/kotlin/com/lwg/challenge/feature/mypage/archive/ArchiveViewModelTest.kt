package com.lwg.challenge.feature.mypage.archive

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.usecase.GetChallengeArchiveUseCase
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiEffect
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiState
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
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

    // 기기 시계를 읽으면 실제 이번 달이 바뀔 때마다 테스트가 흔들려 진입 시각을 고정한다.
    private fun createViewModel(
        now: LocalDateTime = LocalDateTime(2026, 8, 15, 12, 0),
    ): ArchiveViewModel = ArchiveViewModel(
        getChallengeArchiveUseCase = GetChallengeArchiveUseCase(fakeChallenge),
        now = { now },
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
     * [runCurrent] 를 붙이지 않으면 그 시각의 작업이 빠져 "아무 일도 안 일어났다" 로 읽힌다.
     */
    private fun TestScope.advanceTime(millis: Long) {
        advanceTimeBy(millis)
        runCurrent()
    }

    private val ArchiveUiState.data: ArchiveUiState.Data get() = assertIs(this)

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

    @Test
    fun `첫 진입은 기기 시계의 이번 달을 조회한다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertEquals(listOf(YearMonth(2026, 8)), fakeChallenge.requestedMonths)
    }

    // 미래 달에는 종료된 챌린지가 있을 수 없어 이번 달이 상한이다.
    @Test
    fun `이번 달에서는 다음으로 갈 수 없다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val data = viewModel.uiState.value.data
        assertEquals("2026년 8월", data.monthLabel)
        assertFalse(data.canGoNext)
    }

    @Test
    fun `이전 달로 이동하면 그 달을 조회한다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.previousMonth()
        runCurrent()

        assertEquals(listOf(YearMonth(2026, 8), YearMonth(2026, 7)), fakeChallenge.requestedMonths)
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

        viewModel.previousMonth()
        runCurrent()

        assertEquals(listOf(YearMonth(2026, 8), YearMonth(2026, 7)), fakeChallenge.requestedMonths)
        assertEquals("2026년 7월", viewModel.uiState.value.data.monthLabel)
    }

    // 위와 같은 규칙의 반대쪽 끝. 기록이 아예 없으면 하한으로 삼을 월조차 없는데, 그 상태에서
    // 이동이 막히면 사용자는 첫 화면에서 아무 데도 갈 수 없다.
    @Test
    fun `기록이 하나도 없어도 이전 달로 갈 수 있다`() = runTest {
        fakeChallenge.firstArchivedMonth = null

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.previousMonth()
        runCurrent()

        assertEquals(listOf(YearMonth(2026, 8), YearMonth(2026, 7)), fakeChallenge.requestedMonths)
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
        assertEquals("2026년 8월", beforeTap.data.monthLabel)
        assertIs<ArchiveUiState.Data>(beforeTap)

        fakeChallenge.neverRespondTo(YearMonth(2026, 7))
        viewModel.previousMonth()
        runCurrent()

        // 라벨도 본문도 손대지 않았으므로 탭 직전 상태와 완전히 같은 값이어야 한다.
        assertEquals(beforeTap, viewModel.uiState.value)

        // 응답이 끝내 오지 않아도 놓지 않는다.
        advanceTime(10_000)

        assertEquals(beforeTap, viewModel.uiState.value)
    }

    // **깜빡임 제거의 회귀 테스트다.** 응답이 도착하면 옛 짝에서 새 짝으로 곧장
    // 넘어가야 한다 — 그 사이에 Loading 이 한 번이라도 끼면 그것이 사용자가 신고한
    // 깜빡임이다. 실제 로컬 응답이 3~49ms 라 이 경로가 월 전환의 거의 전부다.
    //
    // 두 awaitItem 사이에 아무것도 없다는 것이 단언의 핵심이다. 중간 상태가 하나라도 방출되면
    // 두 번째 awaitItem 이 그것을 집어 라벨 단언에서 걸린다.
    //
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
            assertEquals("2026년 8월", before.data.monthLabel)
            assertIs<ArchiveUiState.Data>(before)
            assertEquals("1건", before.data.countText)

            viewModel.previousMonth()
            runCurrent()

            val after = awaitItem()
            assertEquals("2026년 7월", after.data.monthLabel)
            val content = assertIs<ArchiveUiState.Data>(after)
            assertEquals(listOf(2L, 3L), content.challenges.map { it.challengeId })
            assertEquals("2건", after.data.countText)

            advanceTime(1_000)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
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
            assertEquals("2026년 8월", viewModel.uiState.value.data.monthLabel)

            viewModel.previousMonth()
            runCurrent()

            val effect = awaitItem()
            assertIs<ArchiveUiEffect.ShowMessage>(effect)
            assertEquals("목록을 불러오지 못했어요", effect.message)

            val state = viewModel.uiState.value
            assertEquals("2026년 8월", state.data.monthLabel)
            val content = assertIs<ArchiveUiState.Data>(state)
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

        viewModel.previousMonth()
        runCurrent()
        assertEquals(1, fakeChallenge.requestedMonths.count { it == YearMonth(2026, 7) })

        viewModel.previousMonth()
        runCurrent()

        assertEquals(2, fakeChallenge.requestedMonths.count { it == YearMonth(2026, 7) })
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
            assertIs<ArchiveUiState.Data>(beforeTap)

            viewModel.previousMonth()
            runCurrent()
            // 응답이 끝내 오지 않아도 아무것도 방출하지 않는다. 스피너가 한 번이라도 끼면 여기서 걸린다.
            advanceTime(10_000)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        // 반대쪽 끝. 첫 로딩만 즉시 스피너인 이유는 **감출 이전 달이 애초에 없다**는 것뿐이다 —
        // 붙들 옛 화면이 있는 전환과 달리 여기서는 본문 전체가 로딩을 말하는 것 말고 할 말이 없다.
        fakeChallenge.neverRespondTo(YearMonth(2026, 8))
        val firstLoad = createViewModel()
        subscribeUiState(firstLoad)

        val loading = firstLoad.uiState.value
        assertIs<ArchiveUiState.Loading>(loading)
    }

    // 빈 응답은 조회 실패가 아니라 성공한 0건이다. 둘을 같은 화면으로 접으면 실패를
    // "이 달엔 없다" 로 둔갑시키게 된다.
    @Test
    fun `빈 달은 실패가 아니다`() = runTest {
        fakeChallenge.histories = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val content = viewModel.uiState.value
        assertIs<ArchiveUiState.Data>(content)
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
        assertIs<ArchiveUiState.Data>(neverArchivedState)
        assertIs<ArchiveUiState.Data>(emptyMonthState)

        assertTrue(neverArchivedState.data.showsNoRecordsEver)
        assertFalse(emptyMonthState.data.showsNoRecordsEver)
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
        assertTrue(viewModel.uiState.value.data.showsNoRecordsEver)

        viewModel.previousMonth()
        runCurrent()

        val state = viewModel.uiState.value
        assertIs<ArchiveUiState.Data>(state)
        assertFalse(state.data.showsNoRecordsEver)
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
            viewModel.previousMonth()
            runCurrent()
        }
        assertEquals(YearMonth(2026, 6), viewModel.uiState.value.data.selectedMonth)
        assertEquals("화살표로 다른 달을 볼 수 있어요", viewModel.uiState.value.data.emptyMonthSubtitle)

        // 6월 → 3월. 첫 기록을 지나쳤으므로 B형으로 갈린다.
        repeat(3) {
            viewModel.previousMonth()
            runCurrent()
        }
        assertEquals(YearMonth(2026, 3), viewModel.uiState.value.data.selectedMonth)
        assertEquals("첫 기록은 2026년 5월이에요", viewModel.uiState.value.data.emptyMonthSubtitle)
    }

    // 실패 화면엔 월 이동 바가 없어 실패 화면을 띄운 채 달을 바꿀 수 없다. 전환 실패는 스낵바로만 알린다.
    @Test
    fun `첫 로딩이 실패하면 Error 상태다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        val state = viewModel.uiState.value
        assertIs<ArchiveUiState.Error>(state)
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

        viewModel.previousMonth()
        runCurrent()
        assertEquals("2026년 8월", viewModel.uiState.value.data.monthLabel)

        fakeChallenge.respondTo(
            month = YearMonth(2026, 7),
            histories = listOf(history(2L, LocalDate(2026, 7, 3))),
        )
        viewModel.previousMonth()
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("2026년 7월", state.data.monthLabel)
        val content = assertIs<ArchiveUiState.Data>(state)
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

    // 전체 실패로 보류된 메시지는 그 수집과 함께 버려지고 끝나야 한다. 보류 자리를 수집 단위(monthFlow
    // 지역 변수)가 아니라 ViewModel 프로퍼티로 올리면 값이 재수집까지 살아남아, 재수집의 첫 emit 에
    // 직전 실패 메시지가 실린다 — 성공한 재시도 위에 엉뚱한 실패 스낵바가 뜬다.
    @Test
    fun `전체 실패 후 재시도가 성공하면 이전 실패 메시지가 새지 않는다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            assertIs<ArchiveUiState.Error>(viewModel.uiState.value)

            fakeChallenge.error = null
            fakeChallenge.histories = listOf(history(1L))
            viewModel.retry()
            runCurrent()

            assertIs<ArchiveUiState.Data>(viewModel.uiState.value)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 재수집이 선택된 달을 이번 달로 초기화하면 사용자가 화살표로 걸어 들어간 걸음이 사라진다.
    @Test
    fun `재시도는 현재 달을 다시 조회한다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        viewModel.previousMonth()
        runCurrent()
        viewModel.retry()
        runCurrent()

        assertEquals(
            listOf(YearMonth(2026, 8), YearMonth(2026, 7), YearMonth(2026, 7)),
            fakeChallenge.requestedMonths,
        )
    }

    // 재수집도 전환 경로를 타므로 응답을 기다리는 동안 화면이 첫 로딩으로 되돌아가면 안 된다.
    @Test
    fun `재시도해도 월 이동 바가 사라지지 않는다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        fakeChallenge.neverRespondTo(YearMonth(2026, 8))
        viewModel.retry()
        runCurrent()
        advanceTime(10_000)

        val state = viewModel.uiState.value
        assertIs<ArchiveUiState.Data>(state)
        assertEquals("2026년 8월", state.data.monthLabel)
    }

    @Test
    fun `비활성 방향으로 눌러도 재조회가 일어나지 않는다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertEquals(1, fakeChallenge.callCount)

        viewModel.nextMonth()
        runCurrent()

        assertEquals(1, fakeChallenge.callCount)
    }

    // 과거 하한이 있던 시절 기록이 전부 이번 달인 계정은 양쪽 화살표가 함께 죽어 고장으로 읽혔다.
    @Test
    fun `기록이 없어도 이전 달로 계속 갈 수 있다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        repeat(3) {
            viewModel.previousMonth()
            runCurrent()
        }

        assertEquals("2026년 5월", viewModel.uiState.value.data.monthLabel)
    }

    @Test
    fun `이번 달에서만 isThisMonth 가 참이다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertTrue(viewModel.uiState.value.data.isThisMonth)

        viewModel.previousMonth()
        runCurrent()

        val data = viewModel.uiState.value.data
        assertFalse(data.isThisMonth)
        assertTrue(data.canGoNext)
    }

    // 월 숫자만 더하고 빼면 12월 다음이 13월이 된다.
    @Test
    fun `연 경계를 넘어 이동한다`() = runTest {
        val viewModel = createViewModel(now = LocalDateTime(2026, 1, 10, 9, 0))
        subscribeUiState(viewModel)

        viewModel.previousMonth()
        runCurrent()
        assertEquals("2025년 12월", viewModel.uiState.value.data.monthLabel)

        viewModel.nextMonth()
        runCurrent()
        val data = viewModel.uiState.value.data
        assertEquals("2026년 1월", data.monthLabel)
        assertFalse(data.canGoNext)
    }
}
