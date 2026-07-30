@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.remote.api.ChallengeApi
import com.lwg.challenge.remote.model.challenge.AcceptChallengeBody
import com.lwg.challenge.remote.model.challenge.AcceptChallengeData
import com.lwg.challenge.remote.model.challenge.AcceptChallengeResponse
import com.lwg.challenge.remote.model.challenge.CancelChallengeData
import com.lwg.challenge.remote.model.challenge.CancelChallengeResponse
import com.lwg.challenge.remote.model.challenge.ChallengeCreateBody
import com.lwg.challenge.remote.model.challenge.ChallengeCreateData
import com.lwg.challenge.remote.model.challenge.ChallengeCreateResponse
import com.lwg.challenge.remote.model.challenge.DeadlineTypeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesData
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse
import com.lwg.challenge.remote.model.challenge.RejectChallengeData
import com.lwg.challenge.remote.model.challenge.RejectChallengeResponse
import com.lwg.challenge.remote.network.util.ApiResult
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ChallengeRepositoryImplTest {

    private val receivedDto = ReceivedChallengeDto(
        challengeId = 7L,
        challengerId = 42L,
        challengerNickname = "민수",
        challengerProfileImageUrl = null,
        challengerMission = "오늘 운동 1시간 하기",
        betContent = "커피 사기",
        challengeDate = "2026-07-28",
        deadline = "2026-07-28T15:00:00Z",
        createdAt = "2026-07-28T02:11:00Z",
    )

    private val expectedReceived = ReceivedChallenge(
        challengeId = 7L,
        challengerId = 42L,
        challengerNickname = "민수",
        challengerProfileImageUrl = null,
        challengerMission = "오늘 운동 1시간 하기",
        betContent = "커피 사기",
        deadline = Instant.parse("2026-07-28T15:00:00Z"),
        createdAt = Instant.parse("2026-07-28T02:11:00Z"),
    )

    private val createInput = ChallengeCreateInput(
        opponentId = 42L,
        myMission = "오늘 운동 1시간 하기",
        betContent = "커피 사기",
        deadlineType = DeadlineType.TODAY,
    )

    private fun receivedSuccess(vararg dto: ReceivedChallengeDto) = ApiResult.Success(
        ReceivedChallengesResponse(ReceivedChallengesData(challenges = dto.toList())),
    )

    @Test
    fun `getReceivedChallenges 성공 - DTO 가 도메인으로 매핑돼 1회 emit`() = runTest {
        val api = FakeChallengeApi(receivedResult = receivedSuccess(receivedDto))
        val repository = ChallengeRepositoryImpl(api)

        val errors = mutableListOf<String>()
        val emitted = repository.getReceivedChallenges(onError = { errors.add(it) }).toList()

        assertEquals(listOf(listOf(expectedReceived)), emitted)
        assertNull(emitted.single().single().challengerProfileImageUrl)
        assertEquals(emptyList(), errors)
        assertEquals(1, api.getReceivedCallCount)
    }

    @Test
    fun `getReceivedChallenges 실패 - onError 1회, emit 0건`() = runTest {
        val api = FakeChallengeApi(
            receivedResult = ApiResult.Failure.CustomError(code = 700, message = "챌린지를 찾을 수 없어요"),
        )
        val repository = ChallengeRepositoryImpl(api)

        val errors = mutableListOf<String>()
        val emitted = repository.getReceivedChallenges(onError = { errors.add(it) }).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(listOf("챌린지를 찾을 수 없어요"), errors)
    }

    @Test
    fun `getReceivedChallenges 빈 배열 - emptyList emit`() = runTest {
        val api = FakeChallengeApi(receivedResult = receivedSuccess())
        val repository = ChallengeRepositoryImpl(api)

        val emitted = repository.getReceivedChallenges(onError = {}).toList()

        assertEquals(listOf(emptyList<ReceivedChallenge>()), emitted)
    }

    @Test
    fun `getReceivedChallenges - 시간 파싱 실패 시 DISTANT_PAST 폴백`() = runTest {
        val api = FakeChallengeApi(
            receivedResult = receivedSuccess(
                receivedDto.copy(deadline = "2026-07-28 15:00", createdAt = ""),
            ),
        )
        val repository = ChallengeRepositoryImpl(api)

        val emitted = repository.getReceivedChallenges(onError = {}).toList()

        val challenge = emitted.single().single()
        assertEquals(Instant.DISTANT_PAST, challenge.deadline)
        assertEquals(Instant.DISTANT_PAST, challenge.createdAt)
    }

    @Test
    fun `createChallenge 성공 - 서버가 준 challengeId 를 onSuccess 로 전달`() = runTest {
        val api = FakeChallengeApi(
            createResult = ApiResult.Success(
                ChallengeCreateResponse(
                    ChallengeCreateData(
                        challengeId = 7L,
                        status = "PENDING",
                        challengeDate = "2026-07-28",
                        deadline = "2026-07-28T15:00:00Z",
                    ),
                ),
            ),
        )
        val repository = ChallengeRepositoryImpl(api)

        val successIds = mutableListOf<Long>()
        val errors = mutableListOf<String>()
        repository.createChallenge(createInput, onSuccess = { successIds.add(it) }, onError = { errors.add(it) })

        assertEquals(listOf(7L), successIds)
        assertEquals(emptyList(), errors)
        assertEquals(
            ChallengeCreateBody(
                opponentId = 42L,
                myMission = "오늘 운동 1시간 하기",
                betContent = "커피 사기",
                deadlineType = DeadlineTypeDto.TODAY,
            ),
            api.capturedCreateBody,
        )
    }

    @Test
    fun `createChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            createResult = ApiResult.Failure.CustomError(code = 700, message = "친구에게만 챌린지를 걸 수 있어요"),
        )
        val repository = ChallengeRepositoryImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        repository.createChallenge(createInput, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("친구에게만 챌린지를 걸 수 있어요"), errors)
    }

    @Test
    fun `acceptChallenge 성공 - onSuccess 호출, myMission 이 body 로 전달`() = runTest {
        val api = FakeChallengeApi(
            acceptResult = ApiResult.Success(
                AcceptChallengeResponse(AcceptChallengeData(challengeId = 7L, status = "IN_PROGRESS")),
            ),
        )
        val repository = ChallengeRepositoryImpl(api)

        var successCount = 0
        repository.acceptChallenge(7L, "책 30페이지 읽기", onSuccess = { successCount++ }, onError = {})

        assertEquals(1, successCount)
        assertEquals(7L, api.capturedAcceptId)
        assertEquals(AcceptChallengeBody("책 30페이지 읽기"), api.capturedAcceptBody)
    }

    @Test
    fun `acceptChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            acceptResult = ApiResult.Failure.CustomError(code = 705, message = "이미 처리된 챌린지예요"),
        )
        val repository = ChallengeRepositoryImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        repository.acceptChallenge(
            challengeId = 7L,
            myMission = "책 30페이지 읽기",
            onSuccess = { successCalled = true },
            onError = { errors.add(it) },
        )

        assertTrue(!successCalled)
        assertEquals(listOf("이미 처리된 챌린지예요"), errors)
    }

    @Test
    fun `rejectChallenge 성공 - onSuccess 호출`() = runTest {
        val api = FakeChallengeApi(
            rejectResult = ApiResult.Success(
                RejectChallengeResponse(RejectChallengeData(challengeId = 7L, status = "REJECTED")),
            ),
        )
        val repository = ChallengeRepositoryImpl(api)

        var successCount = 0
        repository.rejectChallenge(7L, onSuccess = { successCount++ }, onError = {})

        assertEquals(1, successCount)
        assertEquals(7L, api.capturedRejectId)
    }

    @Test
    fun `rejectChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            rejectResult = ApiResult.Failure.CustomError(code = 705, message = "마감이 지난 챌린지예요"),
        )
        val repository = ChallengeRepositoryImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        repository.rejectChallenge(7L, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("마감이 지난 챌린지예요"), errors)
    }

    @Test
    fun `cancelChallenge 성공 - onSuccess 호출`() = runTest {
        val api = FakeChallengeApi(
            cancelResult = ApiResult.Success(CancelChallengeResponse(CancelChallengeData(challengeId = 7L))),
        )
        val repository = ChallengeRepositoryImpl(api)

        var successCount = 0
        repository.cancelChallenge(7L, onSuccess = { successCount++ }, onError = {})

        assertEquals(1, successCount)
        assertEquals(7L, api.capturedCancelId)
    }

    @Test
    fun `cancelChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            cancelResult = ApiResult.Failure.CustomError(code = 700, message = "내가 보낸 도전장이 아니에요"),
        )
        val repository = ChallengeRepositoryImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        repository.cancelChallenge(7L, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("내가 보낸 도전장이 아니에요"), errors)
    }
}

private class FakeChallengeApi(
    private val createResult: ApiResult<ChallengeCreateResponse> = ApiResult.Success(ChallengeCreateResponse()),
    private val receivedResult: ApiResult<ReceivedChallengesResponse> = ApiResult.Success(ReceivedChallengesResponse()),
    private val acceptResult: ApiResult<AcceptChallengeResponse> = ApiResult.Success(AcceptChallengeResponse()),
    private val rejectResult: ApiResult<RejectChallengeResponse> = ApiResult.Success(RejectChallengeResponse()),
    private val cancelResult: ApiResult<CancelChallengeResponse> = ApiResult.Success(CancelChallengeResponse()),
) : ChallengeApi {

    var getReceivedCallCount = 0
        private set
    var capturedCreateBody: ChallengeCreateBody? = null
        private set
    var capturedAcceptId: Long? = null
        private set
    var capturedAcceptBody: AcceptChallengeBody? = null
        private set
    var capturedRejectId: Long? = null
        private set
    var capturedCancelId: Long? = null
        private set

    override suspend fun createChallenge(body: ChallengeCreateBody): ApiResult<ChallengeCreateResponse> {
        capturedCreateBody = body
        return createResult
    }

    override suspend fun getReceivedChallenges(): ApiResult<ReceivedChallengesResponse> {
        getReceivedCallCount++
        return receivedResult
    }

    override suspend fun acceptChallenge(
        challengeId: Long,
        body: AcceptChallengeBody,
    ): ApiResult<AcceptChallengeResponse> {
        capturedAcceptId = challengeId
        capturedAcceptBody = body
        return acceptResult
    }

    override suspend fun rejectChallenge(challengeId: Long): ApiResult<RejectChallengeResponse> {
        capturedRejectId = challengeId
        return rejectResult
    }

    override suspend fun cancelChallenge(challengeId: Long): ApiResult<CancelChallengeResponse> {
        capturedCancelId = challengeId
        return cancelResult
    }
}
