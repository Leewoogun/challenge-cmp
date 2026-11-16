package com.lwg.challenge.domain.usecase

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendChallengePage
import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetFriendChallengesUseCaseTest {

    private val fakeRepository = FakeFriendsRepository()
    private val useCase = GetFriendChallengesUseCase(fakeRepository)

    private fun challenge(challengeId: Long, bet: String = "커피 사기") = FriendPastChallenge(
        challengeId = challengeId,
        challengeDate = LocalDate(2026, 11, 2),
        myMission = "운동 1시간",
        friendMission = "책 30페이지",
        bet = bet,
        myResult = ChallengeOutcome.WIN,
    )

    private fun page(ids: List<Long>, nextChallengeId: Long?) = FriendChallengePage(
        challenges = ids.map(::challenge),
        nextChallengeId = nextChallengeId,
        isPageEnd = nextChallengeId == null,
    )

    /** 마지막 항목 17, 그 - 1 인 16 과 다른 12 를 심어 앱이 키를 계산하면 깨진다. */
    @Test
    fun `다음 쪽은 응답의 nextChallengeId 를 그대로 싣고 20 개씩 요청한다`() = runTest {
        fakeRepository.pages += page(listOf(19L, 17L), nextChallengeId = 12L)
        fakeRepository.pages += page(listOf(12L, 9L), nextChallengeId = null)

        useCase(friendId = 42L, onError = {}).test {
            assertFalse(awaitItem().isPageEnd)

            useCase.nextPage()

            val result = awaitItem()
            assertEquals(listOf(19L, 17L, 12L, 9L), result.challenges.map { it.challengeId })
            assertTrue(result.isPageEnd)
            assertEquals(listOf(null, 12L), fakeRepository.requestedNextChallengeIds)
            assertEquals(listOf(20, 20), fakeRepository.requestedSizes)
            assertEquals(listOf(42L, 42L), fakeRepository.requestedFriendIds)
        }
    }

    @Test
    fun `isPageEnd 인 쪽 다음에는 더 요청하지 않는다`() = runTest {
        fakeRepository.pages += page(listOf(19L, 17L), nextChallengeId = null)

        useCase(friendId = 42L, onError = {}).test {
            assertTrue(awaitItem().isPageEnd)

            useCase.nextPage()

            expectNoEvents()
            assertEquals(listOf<Long?>(null), fakeRepository.requestedNextChallengeIds)
        }
    }

    @Test
    fun `쪽 사이에 같은 challengeId 가 겹치면 먼저 받은 것 하나만 담는다`() = runTest {
        fakeRepository.pages += page(listOf(19L, 17L), nextChallengeId = 12L)
        fakeRepository.pages += FriendChallengePage(
            challenges = listOf(challenge(17L, bet = "겹친 행"), challenge(12L)),
            nextChallengeId = null,
            isPageEnd = true,
        )

        useCase(friendId = 42L, onError = {}).test {
            awaitItem()
            useCase.nextPage()

            val challenges = awaitItem().challenges
            assertEquals(listOf(19L, 17L, 12L), challenges.map { it.challengeId })
            assertEquals("커피 사기", challenges[1].bet)
        }
    }

    private class FakeFriendsRepository : FriendsRepository {

        val pages = ArrayDeque<FriendChallengePage>()
        val requestedFriendIds = mutableListOf<Long>()
        val requestedNextChallengeIds = mutableListOf<Long?>()
        val requestedSizes = mutableListOf<Int>()

        override fun getFriendChallenges(
            friendId: Long,
            nextChallengeId: Long?,
            size: Int,
            onError: (String) -> Unit,
        ): Flow<FriendChallengePage> = flow {
            requestedFriendIds += friendId
            requestedNextChallengeIds += nextChallengeId
            requestedSizes += size
            emit(pages.removeFirst())
        }

        override fun getFriendRecord(friendId: Long, onError: (String) -> Unit): Flow<FriendRecord> = emptyFlow()

        override fun searchUsers(nickname: String, onError: (String) -> Unit): Flow<List<UserSearchResult>> =
            emptyFlow()

        override fun getFriendList(onError: (String) -> Unit): Flow<List<Friend>> = emptyFlow()

        override fun getReceivedRequestList(onError: (String) -> Unit): Flow<List<FriendRequest>> = emptyFlow()

        override suspend fun sendRequest(
            receiverId: Long,
            onSuccess: (requestId: Long) -> Unit,
            onError: (String) -> Unit,
        ) = Unit

        override suspend fun acceptRequest(
            requestId: Long,
            onSuccess: () -> Unit,
            onError: (String) -> Unit,
        ) = Unit

        override suspend fun rejectRequest(
            requestId: Long,
            onSuccess: () -> Unit,
            onError: (String) -> Unit,
        ) = Unit

        override suspend fun cancelRequest(
            requestId: Long,
            onSuccess: () -> Unit,
            onError: (String) -> Unit,
        ) = Unit
    }
}
