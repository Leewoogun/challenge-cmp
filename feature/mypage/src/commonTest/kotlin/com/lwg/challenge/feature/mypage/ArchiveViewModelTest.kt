package com.lwg.challenge.feature.mypage

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.usecase.GetChallengeHistoriesUseCase
import com.lwg.challenge.feature.mypage.contract.ArchiveUiEffect
import com.lwg.challenge.feature.mypage.contract.ArchiveUiState
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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

    private fun createViewModel(): ArchiveViewModel = ArchiveViewModel(
        getChallengeHistoriesUseCase = GetChallengeHistoriesUseCase(fakeChallenge),
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
        challengeDate: LocalDate,
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
    fun `같은 달 챌린지끼리 한 섹션으로 묶인다`() = runTest {
        fakeChallenge.histories = listOf(
            history(1L, LocalDate(2026, 8, 14)),
            history(2L, LocalDate(2026, 8, 3)),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as ArchiveUiState.Data

        assertEquals(1, state.months.size)
        assertEquals("2026년 8월", state.months[0].label)
        assertEquals("2건", state.months[0].countText)
        assertEquals(listOf(1L, 2L), state.months[0].challenges.map { it.challengeId })
    }

    @Test
    fun `월 섹션이 최신 월부터 나온다`() = runTest {
        fakeChallenge.histories = listOf(
            history(3L, LocalDate(2026, 6, 12)),
            history(1L, LocalDate(2026, 8, 14)),
            history(2L, LocalDate(2026, 7, 31)),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as ArchiveUiState.Data

        assertEquals(
            listOf("2026년 8월", "2026년 7월", "2026년 6월"),
            state.months.map { it.label },
        )
    }

    /** 판정 시각으로 묶었다면 8월 헤더로 넘어갔을 건이다. 그룹 키가 `challengeDate` 임을 고정한다. */
    @Test
    fun `말일 챌린지가 그 달 섹션에 남는다`() = runTest {
        fakeChallenge.histories = listOf(
            history(1L, LocalDate(2026, 8, 1)),
            history(2L, LocalDate(2026, 7, 31)),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as ArchiveUiState.Data

        assertEquals("2026년 7월", state.months[1].label)
        assertEquals(listOf(2L), state.months[1].challenges.map { it.challengeId })
    }

    /** 해가 다르면 같은 달 번호라도 섹션이 갈린다 — 그룹 키에 연도가 빠지면 8월 두 해가 합쳐진다. */
    @Test
    fun `연도가 다르면 같은 달이어도 섹션이 갈린다`() = runTest {
        fakeChallenge.histories = listOf(
            history(1L, LocalDate(2026, 8, 14)),
            history(2L, LocalDate(2025, 8, 14)),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as ArchiveUiState.Data

        assertEquals(listOf("2026년 8월", "2025년 8월"), state.months.map { it.label })
    }

    @Test
    fun `종료된 챌린지가 없으면 빈 상태다`() = runTest {
        fakeChallenge.histories = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as ArchiveUiState.Data

        assertTrue(state.isEmpty)
    }

    // 첫 진입에는 유지할 직전 값이 없어 Loading 에 갇히면 그대로 무한 스피너가 된다 (design.md §2.5.4.1).
    @Test
    fun `조회가 실패하면 Loading 이 아니라 실패 상태가 된다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertIs<ArchiveUiState.Failure>(viewModel.uiState.value)
    }

    // 전체 실패는 실패 영역이 화면 전체라 화면이 직접 실패를 말한다 — 스낵바를 겹쳐 띄우지 않는다.
    @Test
    fun `조회가 실패해도 스낵바 이펙트를 발행하지 않는다`() = runTest {
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
    fun `일부 항목만 실패하면 목록을 그리고 스낵바도 띄운다`() = runTest {
        fakeChallenge.histories = listOf(history(1L, LocalDate(2026, 8, 14)))
        fakeChallenge.partialError = "일부 항목을 불러오지 못했어요"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)

            val effect = awaitItem()
            assertIs<ArchiveUiEffect.ShowMessage>(effect)
            assertEquals("일부 항목을 불러오지 못했어요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertIs<ArchiveUiState.Data>(state)
        assertEquals(listOf(1L), state.months[0].challenges.map { it.challengeId })
    }

    // 보류한 메시지를 수집 단위로 두지 않으면 재수집의 첫 emit 에 직전 실패 메시지가 실려,
    // 성공한 재시도에 엉뚱한 스낵바가 뜬다.
    @Test
    fun `전체 실패 후 재시도가 성공하면 이전 실패 메시지가 새지 않는다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            assertIs<ArchiveUiState.Failure>(viewModel.uiState.value)

            fakeChallenge.error = null
            fakeChallenge.histories = listOf(history(1L, LocalDate(2026, 8, 14)))
            viewModel.retry()
            runCurrent()

            assertIs<ArchiveUiState.Data>(viewModel.uiState.value)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 0건과 실패는 다른 사실이라 실패를 Data(빈 목록)로 접으면 안 된다 — 증거 보관 화면에서
    // "기록이 하나도 없다" 는 거짓이 가장 나쁘다 (design.md §2.5.4.1).
    @Test
    fun `조회가 실패하면 빈 상태로 그리지 않는다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertTrue(viewModel.uiState.value !is ArchiveUiState.Data)
    }

    @Test
    fun `재시도하면 목록을 다시 조회한다`() = runTest {
        fakeChallenge.error = "목록을 불러오지 못했어요"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertIs<ArchiveUiState.Failure>(viewModel.uiState.value)

        fakeChallenge.error = null
        fakeChallenge.histories = listOf(history(1L, LocalDate(2026, 8, 14)))
        viewModel.retry()
        runCurrent()

        assertEquals(2, fakeChallenge.callCount)
        val state = viewModel.uiState.value
        assertIs<ArchiveUiState.Data>(state)
        assertEquals(listOf(1L), state.months[0].challenges.map { it.challengeId })
    }
}
