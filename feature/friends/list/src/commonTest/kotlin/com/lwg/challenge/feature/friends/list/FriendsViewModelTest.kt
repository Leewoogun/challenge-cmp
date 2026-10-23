package com.lwg.challenge.feature.friends.list

import app.cash.turbine.test
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.usecase.GetFriendsDataUseCase
import com.lwg.challenge.feature.friends.list.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.list.contract.FriendsUiState
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

    private lateinit var fakeRepository: FakeFriendsRepository
    private lateinit var fakeKakaoInviter: FakeKakaoInviter
    private lateinit var fakeUserInfoRepository: FakeUserInfoRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepository = FakeFriendsRepository()
        fakeKakaoInviter = FakeKakaoInviter()
        fakeUserInfoRepository = FakeUserInfoRepository().apply { seed(sampleUserInfo()) }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // uiState 가 WhileSubscribed 이므로 구독자가 있어야 파이프라인이 시작된다.
    private fun TestScope.subscribeUiState(viewModel: FriendsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun createViewModel(): FriendsViewModel =
        FriendsViewModel(
            GetFriendsDataUseCase(fakeRepository),
            fakeRepository,
            fakeKakaoInviter,
            fakeUserInfoRepository,
        )

    private fun sampleUserInfo(nickname: String = "우건") = UserInfo(
        id = 1,
        kakaoId = 1000,
        nickname = nickname,
        profileImageUrl = null,
    )

    private fun sampleFriend(id: Long, nickname: String = "친구$id") = Friend(
        userId = id,
        nickname = nickname,
        profileImageUrl = null,
        since = LocalDateTime.parse("2026-01-01T00:00:00"),
    )

    private fun sampleRequest(id: Long, fromUserId: Long = id, nickname: String = "요청$id") =
        FriendRequest(
            requestId = id,
            fromUser = UserBrief(
                userId = fromUserId,
                nickname = nickname,
                profileImageUrl = null,
            ),
            requestedAt = LocalDateTime.parse("2026-06-01T00:00:00"),
        )

    @Test
    fun `init 시 친구와 요청이 모두 없으면 Empty 상태다`() = runTest {
        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        assertIs<FriendsUiState.Empty>(viewModel.uiState.value)
    }

    @Test
    fun `init 시 받은 요청과 친구 목록을 둘 다 로드한다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(10))
        fakeRepository.friendsFlow.value = listOf(sampleFriend(100), sampleFriend(101))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = assertIs<FriendsUiState.Data>(viewModel.uiState.value)
        assertEquals(1, state.receivedRequests.size)
        assertEquals(10L, state.receivedRequests.first().requestId)
        assertEquals(2, state.friends.size)
        assertEquals(listOf(100L, 101L), state.friends.map { it.userId })
    }

    @Test
    fun `acceptRequest 호출 시 repository acceptRequest 가 호출된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(10))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = assertIs<FriendsUiState.Data>(viewModel.uiState.value)
        viewModel.acceptRequest(state.receivedRequests.first())

        assertEquals(listOf(10L), fakeRepository.acceptRequestCalls)
    }

    @Test
    fun `rejectRequest 호출 시 repository rejectRequest 가 호출된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(20))

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = assertIs<FriendsUiState.Data>(viewModel.uiState.value)
        viewModel.rejectRequest(state.receivedRequests.first())

        assertEquals(listOf(20L), fakeRepository.rejectRequestCalls)
    }

    @Test
    fun `acceptRequest 실패 시 ShowMessage 가 발행된다`() = runTest {
        fakeRepository.receivedRequestsFlow.value = listOf(sampleRequest(30))
        fakeRepository.acceptRequestError = "이미 친구입니다"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val item = assertIs<FriendsUiState.Data>(viewModel.uiState.value).receivedRequests.first()

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
        subscribeUiState(viewModel)
        val item = assertIs<FriendsUiState.Data>(viewModel.uiState.value).receivedRequests.first()

        viewModel.uiEffect.test {
            viewModel.rejectRequest(item)
            val effect = awaitItem()
            assertIs<FriendsUiEffect.ShowMessage>(effect)
            assertEquals("네트워크 오류가 발생하였습니다.", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `inviteFriend 성공 시 캐시 닉네임으로 초대하고 메시지를 노출하지 않는다`() = runTest {
        fakeUserInfoRepository.seed(sampleUserInfo(nickname = "도전왕"))
        fakeKakaoInviter.result = Result.success(Unit)

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.inviteFriend()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, fakeKakaoInviter.callCount)
        assertEquals("도전왕", fakeKakaoInviter.lastNickname)
    }

    @Test
    fun `inviteFriend 실패 시 ShowMessage 가 발행된다`() = runTest {
        fakeKakaoInviter.result = Result.failure(IllegalStateException("공유 실패"))

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.inviteFriend()
            val effect = awaitItem()
            assertIs<FriendsUiEffect.ShowMessage>(effect)
            assertEquals("친구 초대를 보낼 수 없어요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, fakeKakaoInviter.callCount)
    }

    @Test
    fun `캐시가 null 이면 inviteFriend 는 안내 메시지를 노출하고 초대하지 않는다`() = runTest {
        fakeUserInfoRepository.seed(null)

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.inviteFriend()
            val effect = awaitItem()
            assertIs<FriendsUiEffect.ShowMessage>(effect)
            assertEquals("사용자 정보를 불러오는 중이에요. 잠시 후 다시 시도해주세요.", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(0, fakeKakaoInviter.callCount)
    }
}
