@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.friends

import app.cash.turbine.test
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

    private lateinit var fakeRepository: FakeFriendsRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepository = FakeFriendsRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): FriendsViewModel = FriendsViewModel(fakeRepository)

    private fun sampleFriend(id: Long, nickname: String = "친구$id") = Friend(
        userId = id,
        nickname = nickname,
        profileImageUrl = null,
        since = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private fun sampleRequest(id: Long, fromUserId: Long = id, nickname: String = "요청$id") =
        FriendRequest(
            requestId = id,
            fromUser = UserBrief(
                userId = fromUserId,
                nickname = nickname,
                profileImageUrl = null,
            ),
            requestedAt = Instant.parse("2026-06-01T00:00:00Z"),
        )

    // ── 기존 (변형): init 시 uiState 는 Data 로 진입, 빈 두 리스트 ────────────────────────────

    @Test
    fun `init 시 uiState 는 빈 Data 로 진입한다`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertIs<FriendsUiState.Data>(state)
        assertTrue(state.receivedRequests.isEmpty())
        assertTrue(state.friends.isEmpty())
    }

    @Test
    fun `showMessage 호출 시 ShowMessage effect 가 발행된다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.showMessage("준비 중입니다")
            assertEquals(FriendsUiEffect.ShowMessage("준비 중입니다"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── 신규: 받은 요청 + 친구 목록 둘 다 로드 ─────────────────────────────────────────────

    @Test
    fun `init 시 받은 요청과 친구 목록을 둘 다 로드한다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(10))
        fakeRepository.friendsFlow.value = listOf(sampleFriend(100), sampleFriend(101))

        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertIs<FriendsUiState.Data>(state)
        assertEquals(1, state.receivedRequests.size)
        assertEquals(10L, state.receivedRequests.first().requestId)
        assertEquals(2, state.friends.size)
        assertEquals(listOf(100L, 101L), state.friends.map { it.userId })
    }

    // ── 신규: 수락 — 낙관적 갱신 (받은 요청 제거 + 친구 목록에 추가) ──────────────────────────

    @Test
    fun `acceptRequest 성공 시 받은 요청에서 제거되고 친구 목록에 추가된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(10, fromUserId = 999))
        fakeRepository.friendsFlow.value = emptyList()

        val viewModel = createViewModel()
        val initial = viewModel.uiState.value as FriendsUiState.Data
        viewModel.acceptRequest(initial.receivedRequests.first())

        val state = viewModel.uiState.value as FriendsUiState.Data
        assertTrue(state.receivedRequests.isEmpty())
        assertEquals(1, state.friends.size)
        assertEquals(999L, state.friends.first().userId)
        assertEquals(listOf(10L), fakeRepository.acceptRequestCalls)
    }

    // ── 신규: 거절 — 낙관적 갱신 (받은 요청에서 제거) ─────────────────────────────────────

    @Test
    fun `rejectRequest 성공 시 받은 요청에서 제거된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(20), sampleRequest(21))

        val viewModel = createViewModel()
        val initial = viewModel.uiState.value as FriendsUiState.Data
        viewModel.rejectRequest(initial.receivedRequests.first { it.requestId == 20L })

        val state = viewModel.uiState.value as FriendsUiState.Data
        assertEquals(listOf(21L), state.receivedRequests.map { it.requestId })
        assertEquals(listOf(20L), fakeRepository.rejectRequestCalls)
    }

    // ── 신규: 수락 실패 시 ShowMessage + 롤백 ──────────────────────────────────────────

    @Test
    fun `acceptRequest 실패 시 ShowMessage 발행 후 받은 요청을 롤백한다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(30))
        fakeRepository.acceptRequestError = "이미 친구입니다"

        val viewModel = createViewModel()
        val initial = viewModel.uiState.value as FriendsUiState.Data

        viewModel.uiEffect.test {
            viewModel.acceptRequest(initial.receivedRequests.first())
            val effect = awaitItem()
            assertIs<FriendsUiEffect.ShowMessage>(effect)
            assertEquals("이미 친구입니다", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        val state = viewModel.uiState.value as FriendsUiState.Data
        assertEquals(listOf(30L), state.receivedRequests.map { it.requestId })
        assertTrue(state.friends.isEmpty())
    }

    // ── 신규: 거절 실패 시 ShowMessage + 롤백 ──────────────────────────────────────────

    @Test
    fun `rejectRequest 실패 시 ShowMessage 발행 후 받은 요청을 롤백한다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(40))
        fakeRepository.rejectRequestError = "네트워크 오류가 발생하였습니다."

        val viewModel = createViewModel()
        val initial = viewModel.uiState.value as FriendsUiState.Data

        viewModel.uiEffect.test {
            viewModel.rejectRequest(initial.receivedRequests.first())
            val effect = awaitItem()
            assertIs<FriendsUiEffect.ShowMessage>(effect)
            assertEquals("네트워크 오류가 발생하였습니다.", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
        val state = viewModel.uiState.value as FriendsUiState.Data
        assertEquals(listOf(40L), state.receivedRequests.map { it.requestId })
    }

    // ── 신규: 401(세션 만료)은 Repository 가 흡수 → ViewModel 엔 성공으로 보임, ShowMessage 없음 ──────
    //
    // 401 은 더 이상 ViewModel 에 도달하지 않는다 (Ktor Auth 전담 + RepositoryImpl 이 Result.success 로 흡수).
    // 따라서 ViewModel 관점에서 401 은 일반 성공 경로와 동일하며, 별도 테스트는 성공 케이스로 커버된다.

    // ── 신규(race): accept 실패 롤백이 동시 진행된 reject 의 낙관 갱신을 덮어쓰지 않는다 ───────

    @Test
    fun `accept 실패 롤백은 동시 reject 의 낙관 갱신을 보존한다`() = runTest {
        // 요청 A(70) accept, 요청 B(71) reject 시나리오
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(70), sampleRequest(71))
        // accept 는 실패, reject 는 성공
        fakeRepository.acceptRequestError = "이미 친구입니다"

        val viewModel = createViewModel()
        val initial = viewModel.uiState.value as FriendsUiState.Data
        val a = initial.receivedRequests.first { it.requestId == 70L }
        val b = initial.receivedRequests.first { it.requestId == 71L }

        // A accept 낙관 → 받은요청에서 70 사라짐
        viewModel.acceptRequest(a)
        // 동시에 B reject 낙관 → 받은요청에서 71 사라짐
        viewModel.rejectRequest(b)

        // accept 실패 롤백 시 71 (이미 reject 로 사라진) 까지 되살아나면 안 됨
        val state = viewModel.uiState.value as FriendsUiState.Data
        // 70 만 복원되어야 함, 71 은 reject 낙관 그대로 사라진 상태
        assertEquals(listOf(70L), state.receivedRequests.map { it.requestId })
        // 친구 목록도 비어있어야 함 (optimisticFriend 도 1행 단위로 제거됨)
        assertTrue(state.friends.isEmpty())
    }

    @Test
    fun `reject 실패 롤백은 동시 accept 의 낙관 갱신을 보존한다`() = runTest {
        // 요청 A(80) accept (성공), 요청 B(81) reject (실패) 시나리오
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(80, fromUserId = 880), sampleRequest(81))
        fakeRepository.rejectRequestError = "timeout"

        val viewModel = createViewModel()
        val initial = viewModel.uiState.value as FriendsUiState.Data
        val a = initial.receivedRequests.first { it.requestId == 80L }
        val b = initial.receivedRequests.first { it.requestId == 81L }

        viewModel.acceptRequest(a)
        viewModel.rejectRequest(b)

        // reject 실패 롤백 시 80 (이미 accept 로 사라진) 까지 되살아나면 안 됨
        val state = viewModel.uiState.value as FriendsUiState.Data
        assertEquals(listOf(81L), state.receivedRequests.map { it.requestId })
        // accept 낙관 갱신으로 추가된 친구는 유지되어야 함
        assertEquals(listOf(880L), state.friends.map { it.userId })
    }
}
