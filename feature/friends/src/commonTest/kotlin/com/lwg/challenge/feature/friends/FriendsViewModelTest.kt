@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.friends

import app.cash.turbine.test
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.domain.usecase.GetFriendsDataUseCase
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
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

    private fun createViewModel(): FriendsViewModel =
        FriendsViewModel(GetFriendsDataUseCase(fakeRepository), fakeRepository)

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

    // ── 진입: 빈 상태 ──────────────────────────────────────────────────────────

    @Test
    fun `init 시 uiState 는 빈 상태로 진입한다`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
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

    // ── 진입: 받은 요청 + 친구 목록 둘 다 로드 ─────────────────────────────────

    @Test
    fun `init 시 받은 요청과 친구 목록을 둘 다 로드한다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(10))
        fakeRepository.friendsFlow.value = listOf(sampleFriend(100), sampleFriend(101))

        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(1, state.receivedRequests.size)
        assertEquals(10L, state.receivedRequests.first().requestId)
        assertEquals(2, state.friends.size)
        assertEquals(listOf(100L, 101L), state.friends.map { it.userId })
    }

    // ── 수락/거절: repository 호출 ─────────────────────────────────────────────

    @Test
    fun `acceptRequest 호출 시 repository acceptRequest 가 호출된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(10))

        val viewModel = createViewModel()
        viewModel.acceptRequest(viewModel.uiState.value.receivedRequests.first())

        assertEquals(listOf(10L), fakeRepository.acceptRequestCalls)
    }

    @Test
    fun `rejectRequest 호출 시 repository rejectRequest 가 호출된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(20))

        val viewModel = createViewModel()
        viewModel.rejectRequest(viewModel.uiState.value.receivedRequests.first())

        assertEquals(listOf(20L), fakeRepository.rejectRequestCalls)
    }

    // ── 수락/거절 실패: ShowMessage ───────────────────────────────────────────

    @Test
    fun `acceptRequest 실패 시 ShowMessage 가 발행된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(30))
        fakeRepository.acceptRequestError = "이미 친구입니다"

        val viewModel = createViewModel()
        val item = viewModel.uiState.value.receivedRequests.first()

        viewModel.uiEffect.test {
            viewModel.acceptRequest(item)
            val effect = awaitItem()
            assertIs<FriendsUiEffect.ShowMessage>(effect)
            assertEquals("이미 친구입니다", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rejectRequest 실패 시 ShowMessage 가 발행된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(40))
        fakeRepository.rejectRequestError = "네트워크 오류가 발생하였습니다."

        val viewModel = createViewModel()
        val item = viewModel.uiState.value.receivedRequests.first()

        viewModel.uiEffect.test {
            viewModel.rejectRequest(item)
            val effect = awaitItem()
            assertIs<FriendsUiEffect.ShowMessage>(effect)
            assertEquals("네트워크 오류가 발생하였습니다.", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
