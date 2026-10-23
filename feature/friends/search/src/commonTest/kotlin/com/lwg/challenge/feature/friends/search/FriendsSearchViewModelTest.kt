package com.lwg.challenge.feature.friends.search

import app.cash.turbine.test
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiEffect
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiState
import kotlin.test.assertFalse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsSearchViewModelTest {

    private lateinit var fakeRepository: FakeFriendsRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeFriendsRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = FriendsSearchViewModel(fakeRepository)

    private fun sample(
        userId: Long,
        nickname: String = "user$userId",
        relation: Relation = Relation.NONE,
        pendingRequestId: Long? = null,
    ) = UserSearchResult(
        userId = userId,
        nickname = nickname,
        profileImageUrl = null,
        relation = relation,
        pendingRequestId = pendingRequestId,
    )

    @Test
    fun `초기 uiState 는 Idle`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(FriendsSearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `query 1자 입력 시 Idle 유지`() = runTest {
        val viewModel = createViewModel()
        viewModel.search("a")
        advanceTimeBy(1_000)
        assertEquals(FriendsSearchUiState.Idle, viewModel.uiState.value)
        assertTrue(fakeRepository.searchCalls.isEmpty())
    }

    @Test
    fun `query 2자 입력 시 debounce 후 Searching 거쳐 Result 로 전이`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(sample(1), sample(2))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(FriendsSearchUiState.Idle, awaitItem())

            viewModel.search("김민")
            advanceTimeBy(299)
            advanceTimeBy(2)
            // searching emit 후 즉시 Result 로 (StateFlow 는 conflate 가능)
            // → Searching 을 놓칠 수 있으므로 last state 검증
            advanceUntilIdle()
            val finalState = expectMostRecentItem()
            assertIs<FriendsSearchUiState.Result>(finalState)
            assertEquals(2, finalState.items.size)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("김민"), fakeRepository.searchCalls)
    }

    @Test
    fun `결과 0건 → Empty`() = runTest {
        fakeRepository.searchResultsFlow.value = emptyList()

        val viewModel = createViewModel()
        viewModel.search("없는닉")
        advanceUntilIdle()

        assertEquals(FriendsSearchUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `debounce 안에 query 변경 시 마지막 query 만 검색`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(sample(1))

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceTimeBy(100)
        viewModel.search("cd")
        advanceTimeBy(100)
        viewModel.search("ef")
        advanceUntilIdle()

        assertEquals(listOf("ef"), fakeRepository.searchCalls)
    }

    @Test
    fun `sendRequest 성공 시 그 행이 REQUEST_SENT 와 받은 requestId 로 바뀐다`() = runTest {
        fakeRepository.searchResultsFlow.value =
            listOf(sample(100, relation = Relation.NONE))

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()

        val result = viewModel.uiState.value as FriendsSearchUiState.Result
        viewModel.sendRequest(result.items.first())
        advanceUntilIdle()

        val after = viewModel.uiState.value as FriendsSearchUiState.Result
        assertEquals(Relation.REQUEST_SENT, after.items.first().relation)
        assertEquals(fakeRepository.sentRequestId, after.items.first().pendingRequestId)
        assertEquals(listOf(100L), fakeRepository.sendRequestCalls)
    }

    @Test
    fun `acceptRequest 성공 시 그 행이 FRIEND 로 바뀐다`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(
            sample(200, relation = Relation.REQUEST_RECEIVED, pendingRequestId = 555L),
        )

        val viewModel = createViewModel()
        viewModel.search("xy")
        advanceUntilIdle()

        val result = viewModel.uiState.value as FriendsSearchUiState.Result
        viewModel.acceptRequest(result.items.first())
        advanceUntilIdle()

        val after = viewModel.uiState.value as FriendsSearchUiState.Result
        assertEquals(Relation.FRIEND, after.items.first().relation)
        assertEquals(listOf(555L), fakeRepository.acceptRequestCalls)
    }

    @Test
    fun `cancelRequest 성공 시 그 행이 NONE 으로 바뀐다`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(
            sample(300, relation = Relation.REQUEST_SENT, pendingRequestId = 777L),
        )

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()

        val result = viewModel.uiState.value as FriendsSearchUiState.Result
        viewModel.cancelRequest(result.items.first())
        advanceUntilIdle()

        val after = viewModel.uiState.value as FriendsSearchUiState.Result
        assertEquals(Relation.NONE, after.items.first().relation)
        assertEquals(listOf(777L), fakeRepository.cancelRequestCalls)
    }

    @Test
    fun `clickAction 은 relation 에 따라 요청이나 수락으로 갈리고 보냄과 친구는 아무것도 하지 않는다`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(
            sample(1, relation = Relation.NONE),
            sample(2, relation = Relation.REJECTED),
            sample(3, relation = Relation.REQUEST_RECEIVED, pendingRequestId = 900L),
            sample(4, relation = Relation.REQUEST_SENT, pendingRequestId = 901L),
            sample(5, relation = Relation.FRIEND),
        )

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()

        val result = viewModel.uiState.value as FriendsSearchUiState.Result
        result.items.forEach(viewModel::clickAction)
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L), fakeRepository.sendRequestCalls)
        assertEquals(listOf(900L), fakeRepository.acceptRequestCalls)
        assertEquals(emptyList(), fakeRepository.cancelRequestCalls)
    }

    @Test
    fun `REJECTED 에서 sendRequest 성공 시 REQUEST_SENT 로 바뀐다`() = runTest {
        fakeRepository.searchResultsFlow.value =
            listOf(sample(400, relation = Relation.REJECTED))

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()

        val result = viewModel.uiState.value as FriendsSearchUiState.Result
        viewModel.sendRequest(result.items.first())
        advanceUntilIdle()

        val after = viewModel.uiState.value as FriendsSearchUiState.Result
        assertEquals(Relation.REQUEST_SENT, after.items.first().relation)
        assertEquals(listOf(400L), fakeRepository.sendRequestCalls)
    }

    @Test
    fun `sendRequest 실패 시 ShowMessage 를 내고 NONE 을 유지한다`() = runTest {
        fakeRepository.searchResultsFlow.value =
            listOf(sample(500, relation = Relation.NONE))
        fakeRepository.sendRequestError = "차단된 사용자입니다"

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()

        val before = viewModel.uiState.value as FriendsSearchUiState.Result

        viewModel.uiEffect.test {
            viewModel.sendRequest(before.items.first())
            advanceUntilIdle()
            val effect = awaitItem()
            assertIs<FriendsSearchUiEffect.ShowMessage>(effect)
            assertEquals("차단된 사용자입니다", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        val after = viewModel.uiState.value as FriendsSearchUiState.Result
        assertEquals(Relation.NONE, after.items.first().relation)
    }

    @Test
    fun `네트워크 실패 시 고정 문구 ShowMessage`() = runTest {
        fakeRepository.searchResultsFlow.value =
            listOf(sample(501, relation = Relation.NONE))
        fakeRepository.sendRequestError = "네트워크 오류가 발생하였습니다."

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()
        val before = viewModel.uiState.value as FriendsSearchUiState.Result

        viewModel.uiEffect.test {
            viewModel.sendRequest(before.items.first())
            advanceUntilIdle()
            val effect = awaitItem()
            assertIs<FriendsSearchUiEffect.ShowMessage>(effect)
            assertEquals("네트워크 오류가 발생하였습니다.", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // 401(세션 만료)은 Repository 가 흡수 → ViewModel 엔 성공으로 보임.
    // 401 은 더 이상 ViewModel 에 도달하지 않는다 (Ktor Auth 전담 + RepositoryImpl 이 Result.success 로 흡수).
    // ViewModel 관점에서 401 은 일반 성공 경로와 동일하므로 별도 테스트는 성공 케이스로 커버된다.

    @Test
    fun `한 행의 실패는 다른 행의 성공 결과에 영향을 주지 않는다`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(
            sample(700, relation = Relation.NONE),
            sample(701, relation = Relation.NONE),
        )

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()
        val result = viewModel.uiState.value as FriendsSearchUiState.Result
        val a = result.items.first { it.userId == 700L }
        val b = result.items.first { it.userId == 701L }

        fakeRepository.sendRequestError = null
        viewModel.sendRequest(b)
        advanceUntilIdle()

        fakeRepository.sendRequestError = "차단"
        viewModel.sendRequest(a)
        advanceUntilIdle()

        val after = viewModel.uiState.value as FriendsSearchUiState.Result
        val afterA = after.items.first { it.userId == 700L }
        val afterB = after.items.first { it.userId == 701L }
        assertEquals(Relation.NONE, afterA.relation)
        assertEquals(Relation.REQUEST_SENT, afterB.relation)
    }

    @Test
    fun `요청이 나가 있는 동안 그 행은 잠기고 중복 요청이 나가지 않는다`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(sample(800, relation = Relation.NONE))
        val gate = CompletableDeferred<Unit>()
        fakeRepository.mutationGate = gate

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()
        val item = (viewModel.uiState.value as FriendsSearchUiState.Result).items.first()

        viewModel.sendRequest(item)
        viewModel.sendRequest(item)
        advanceUntilIdle()

        val pending = viewModel.uiState.value as FriendsSearchUiState.Result
        assertFalse(pending.isActionEnabled(item))
        assertEquals(Relation.NONE, pending.items.first().relation)
        assertEquals(listOf(800L), fakeRepository.sendRequestCalls)

        gate.complete(Unit)
        advanceUntilIdle()

        val done = viewModel.uiState.value as FriendsSearchUiState.Result
        assertTrue(done.isActionEnabled(item))
        assertEquals(Relation.REQUEST_SENT, done.items.first().relation)
    }

    @Test
    fun `요청을 보낸 직후 취소하면 응답으로 받은 requestId 로 취소한다`() = runTest {
        fakeRepository.searchResultsFlow.value = listOf(sample(900, relation = Relation.NONE))
        fakeRepository.sentRequestId = 4242L

        val viewModel = createViewModel()
        viewModel.search("ab")
        advanceUntilIdle()

        viewModel.sendRequest((viewModel.uiState.value as FriendsSearchUiState.Result).items.first())
        advanceUntilIdle()
        viewModel.cancelRequest((viewModel.uiState.value as FriendsSearchUiState.Result).items.first())
        advanceUntilIdle()

        assertEquals(listOf(4242L), fakeRepository.cancelRequestCalls)
        assertEquals(Relation.NONE, (viewModel.uiState.value as FriendsSearchUiState.Result).items.first().relation)
    }
}
