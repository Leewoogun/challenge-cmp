package com.lwg.challenge.feature.ranking

import app.cash.turbine.test
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.feature.ranking.contract.RankingUiEffect
import com.lwg.challenge.feature.ranking.contract.RankingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
class RankingViewModelTest {

    private lateinit var fakeRanking: FakeRankingRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRanking = FakeRankingRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): RankingViewModel = RankingViewModel(fakeRanking)

    // uiState 가 WhileSubscribed 이므로 구독자가 있어야 파이프라인이 시작된다.
    // 초기 로딩 실패의 ShowMessage 를 검증하는 테스트는 이 호출을 `uiEffect.test { }` 안에 둬야 한다 —
    // uiEffect 가 replay 0 SharedFlow 라 collector 가 붙기 전에 emit 된 이펙트는 그대로 버려진다.
    private fun TestScope.subscribeUiState(viewModel: RankingViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun entry(
        rank: Int,
        userId: Long = rank.toLong(),
        nickname: String = "유저$rank",
        isMe: Boolean = false,
        losses: Int = 30 - rank,
        lossRate: Int = 90 - rank,
        currentLossStreak: Int = 0,
        totalChallenges: Int = 40,
    ) = LoserRankingEntry(
        rank = rank,
        userId = userId,
        nickname = nickname,
        profileImageUrl = null,
        isMe = isMe,
        losses = losses,
        lossRate = lossRate,
        currentLossStreak = currentLossStreak,
        totalChallenges = totalChallenges,
    )

    private fun entries(count: Int): List<LoserRankingEntry> =
        (1..count).map { rank -> entry(rank = rank) }

    @Test
    fun `구독 전에는 Loading 이고 구독하면 Data 로 전이한다`() = runTest {
        fakeRanking.entries = entries(5)

        val viewModel = createViewModel()
        assertIs<RankingUiState.Loading>(viewModel.uiState.value)

        subscribeUiState(viewModel)

        assertIs<RankingUiState.Data>(viewModel.uiState.value)
    }

    @Test
    fun `구독하면 랭킹을 1회 조회한다`() = runTest {
        fakeRanking.entries = entries(5)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)

        assertEquals(1, fakeRanking.callCount)
    }

    @Test
    fun `참가자가 3명 이상이면 포디움이 목록 앞 3개로 채워진다`() = runTest {
        fakeRanking.entries = entries(5)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        val podium = assertNotNull(state.podium)
        assertEquals(state.entries[0], podium.first)
        assertEquals(state.entries[1], podium.second)
        assertEquals(state.entries[2], podium.third)
        assertEquals(listOf("1", "2", "3"), listOf(podium.first, podium.second, podium.third).map { it.rankText })
    }

    /** 신규 사용자의 기본 상태다. 자리가 비면 빈 칸이 생겨 고장난 화면으로 보인다. */
    @Test
    fun `참가자가 2명이면 포디움을 세우지 않는다`() = runTest {
        fakeRanking.entries = entries(2)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        assertNull(state.podium)
        assertEquals(2, state.entries.size)
    }

    @Test
    fun `참가자가 1명이면 포디움이 없고 빈 상태를 띄운다`() = runTest {
        fakeRanking.entries = entries(1)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        assertNull(state.podium)
        assertTrue(state.isEmptyStateVisible)
        assertTrue(state.isRankListVisible)
    }

    @Test
    fun `참가자가 0명이면 빈 상태만 남고 명단도 감춘다`() = runTest {
        fakeRanking.entries = emptyList()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        assertNull(state.podium)
        assertTrue(state.isEmptyStateVisible)
        assertFalse(state.isRankListVisible)
    }

    @Test
    fun `참가자가 3명 이상이면 빈 상태를 띄우지 않는다`() = runTest {
        fakeRanking.entries = entries(3)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        assertFalse(state.isEmptyStateVisible)
        assertNotNull(state.podium)
    }

    /**
     * 명단은 1위부터 전원이 정본이다 — 포디움과 중복돼도 잘라내지 않는다.
     * `drop(3)` 으로 중복을 없애는 회귀를 여기서 막는다.
     */
    @Test
    fun `명단이 1위부터 전원이라 포디움과 중복된다`() = runTest {
        fakeRanking.entries = entries(6)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        assertEquals(6, state.entries.size)
        assertEquals(listOf("1", "2", "3", "4", "5", "6"), state.entries.map { it.rankText })
        assertEquals(state.entries.first(), assertNotNull(state.podium).first)
    }

    /**
     * 정렬과 랭크 부여는 서버 책임이다. losses 는 rank 와 반대 방향으로 증가하도록 심어 두었으므로,
     * 앱이 losses 기준으로 다시 정렬하면 순서가 뒤집혀 이 단언이 깨진다.
     */
    @Test
    fun `서버가 준 순서를 앱이 재정렬하지 않는다`() = runTest {
        fakeRanking.entries = listOf(
            entry(rank = 1, userId = 101L, losses = 2, lossRate = 5),
            entry(rank = 2, userId = 102L, losses = 5, lossRate = 20),
            entry(rank = 3, userId = 103L, losses = 9, lossRate = 45),
            entry(rank = 4, userId = 104L, losses = 20, lossRate = 80),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        assertEquals(listOf(101L, 102L, 103L, 104L), state.entries.map { it.userId })
        assertEquals(listOf("1", "2", "3", "4"), state.entries.map { it.rankText })
    }

    @Test
    fun `내 행의 isMe 와 표시 이름이 그대로 살아서 온다`() = runTest {
        fakeRanking.entries = listOf(
            entry(rank = 1, nickname = "준혁"),
            entry(rank = 2, nickname = "이우건", isMe = true),
            entry(rank = 3, nickname = "민수"),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as RankingUiState.Data

        assertEquals(1, state.entries.count { it.isMe })
        assertEquals("나", state.entries.first { it.isMe }.displayName)
        assertEquals("준혁", state.entries.first().displayName)
    }

    @Test
    fun `조회가 실패하면 ShowMessage 를 발행하고 Loading 을 유지한다`() = runTest {
        fakeRanking.error = "랭킹 조회 실패"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            val effect = awaitItem()
            assertIs<RankingUiEffect.ShowMessage>(effect)
            assertEquals("랭킹 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        assertIs<RankingUiState.Loading>(viewModel.uiState.value)
    }
}
