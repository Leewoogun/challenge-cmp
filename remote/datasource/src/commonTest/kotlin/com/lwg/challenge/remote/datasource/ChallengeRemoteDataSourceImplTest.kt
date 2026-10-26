package com.lwg.challenge.remote.datasource

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.challenge.ReceivedOpponentInfo
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.remote.api.ChallengeApi
import com.lwg.challenge.remote.mapper.SignatureCodec
import com.lwg.challenge.remote.model.challenge.AcceptChallengeBody
import com.lwg.challenge.remote.model.challenge.AcceptChallengeData
import com.lwg.challenge.remote.model.challenge.AcceptChallengeResponse
import com.lwg.challenge.remote.model.challenge.CancelChallengeData
import com.lwg.challenge.remote.model.challenge.CancelChallengeResponse
import com.lwg.challenge.remote.model.challenge.ChallengeCreateBody
import com.lwg.challenge.remote.model.challenge.ChallengeCreateData
import com.lwg.challenge.remote.model.challenge.ChallengeCreateResponse
import com.lwg.challenge.remote.model.challenge.ChallengeDetailData
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryDto
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryListData
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryResponse
import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import com.lwg.challenge.remote.model.challenge.ContractDto
import com.lwg.challenge.remote.model.challenge.ContractUserInfoDto
import com.lwg.challenge.remote.model.challenge.DeadlineTypeDto
import com.lwg.challenge.remote.model.challenge.ForfeitChallengeData
import com.lwg.challenge.remote.model.challenge.ForfeitChallengeResponse
import com.lwg.challenge.remote.model.challenge.ReceivedChallengeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengeOpponentDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesData
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse
import com.lwg.challenge.remote.model.challenge.RejectChallengeData
import com.lwg.challenge.remote.model.challenge.RejectChallengeResponse
import com.lwg.challenge.remote.network.util.ApiResult
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChallengeRemoteDataSourceImplTest {

    private val receivedDto = ReceivedChallengeDto(
        challengeId = 7L,
        betContent = "커피 사기",
        challengeDate = LocalDate(2026, 7, 28),
        deadline = LocalDateTime(2026, 7, 28, 15, 0, 0),
        createdAt = LocalDateTime(2026, 7, 28, 2, 11, 0),
        opponent = ReceivedChallengeOpponentDto(
            id = 42L,
            nickname = "민수",
            profileImageUrl = null,
            mission = "오늘 운동 1시간 하기",
        ),
    )

    private val expectedReceived = ReceivedChallenge(
        challengeId = 7L,
        opponent = ReceivedOpponentInfo(
            id = 42L,
            nickname = "민수",
            profileImageUrl = null,
            mission = "오늘 운동 1시간 하기",
        ),
        penalty = "커피 사기",
        deadline = LocalDateTime(2026, 7, 28, 15, 0, 0),
        createdAt = LocalDateTime(2026, 7, 28, 2, 11, 0),
    )

    private val signature = Signature(
        listOf(SignatureStroke(listOf(SignaturePoint(123, 456), SignaturePoint(130, 460)))),
    )

    private val encodedSignature = SignatureCodec.encode(signature)

    private val createInput = ChallengeCreateInput(
        opponentId = 42L,
        myMission = "오늘 운동 1시간 하기",
        penalty = "커피 사기",
        deadlineType = DeadlineType.TODAY,
        signature = signature,
    )

    private val detailData = ChallengeDetailData(
        challengeId = 7L,
        status = "IN_PROGRESS",
        challengeDate = LocalDate(2026, 8, 3),
        deadline = LocalDateTime(2026, 8, 4, 0, 0, 0),
        betContent = "커피 사기",
        me = ContractUserInfoDto(
            nickname = "이우건",
            profileImageUrl = null,
            mission = "운동 1시간",
        ),
        opponent = ContractUserInfoDto(
            nickname = "테스터1",
            profileImageUrl = null,
            mission = "책 30페이지",
        ),
        contract = ContractDto(
            content = "계약서 본문",
            isFinalized = false,
            mySignature = SignatureCodec.encode(
                Signature(listOf(SignatureStroke(listOf(SignaturePoint(123, 456))))),
            ),
            mySignedAt = LocalDateTime(2026, 8, 3, 11, 20, 0),
        ),
    )

    private val historyDto = ChallengeHistoryDto(
        challengeId = 42L,
        challengeDate = LocalDate(2026, 8, 14),
        opponentNickname = "준혁",
        myMission = "헬스장 가기",
        opponentMission = "금주",
        bet = "치킨 한 마리",
        myResult = ChallengeOutcomeDto.WIN,
    )

    private fun receivedSuccess(vararg dto: ReceivedChallengeDto) = ApiResult.Success(
        ReceivedChallengesResponse(ReceivedChallengesData(challenges = dto.toList())),
    )

    private fun historySuccess(
        firstArchivedMonth: YearMonth? = YearMonth(2026, 3),
        histories: List<ChallengeHistoryDto> = listOf(historyDto),
    ) = ApiResult.Success(
        ChallengeHistoryResponse(ChallengeHistoryListData(firstArchivedMonth, histories)),
    )

    @Test
    fun `getReceivedChallenges 성공 - DTO 가 도메인으로 매핑돼 1회 emit`() = runTest {
        val api = FakeChallengeApi(receivedResult = receivedSuccess(receivedDto))
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.getReceivedChallenges(onError = { errors.add(it) }).toList()

        assertEquals(listOf(listOf(expectedReceived)), emitted)
        assertNull(emitted.single().single().opponent.profileImageUrl)
        assertEquals(emptyList(), errors)
        assertEquals(1, api.getReceivedCallCount)
    }

    @Test
    fun `getReceivedChallenges 실패 - onError 1회, emit 0건`() = runTest {
        val api = FakeChallengeApi(
            receivedResult = ApiResult.Failure.CustomError(code = 700, message = "챌린지를 찾을 수 없어요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.getReceivedChallenges(onError = { errors.add(it) }).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(listOf("챌린지를 찾을 수 없어요"), errors)
    }

    @Test
    fun `getReceivedChallenges 빈 배열 - emptyList emit`() = runTest {
        val api = FakeChallengeApi(receivedResult = receivedSuccess())
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val emitted = dataSource.getReceivedChallenges(onError = {}).toList()

        assertEquals(listOf(emptyList<ReceivedChallenge>()), emitted)
    }

    @Test
    fun `getReceivedChallenges - deadline 이 null 인 항목은 제외되고 onError 1회`() = runTest {
        val api = FakeChallengeApi(
            receivedResult = receivedSuccess(
                receivedDto,
                receivedDto.copy(challengeId = 8L, deadline = null),
            ),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.getReceivedChallenges(onError = { errors.add(it) }).toList()

        assertEquals(listOf(listOf(expectedReceived)), emitted)
        assertEquals(listOf("일부 항목을 불러오지 못했어요"), errors)
    }

    @Test
    fun `getReceivedChallenges - createdAt 이 null 이어도 항목 유지하고 onError 미발화`() = runTest {
        val api = FakeChallengeApi(
            receivedResult = receivedSuccess(receivedDto.copy(createdAt = null)),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.getReceivedChallenges(onError = { errors.add(it) }).toList()

        val challenge = emitted.single().single()
        assertNull(challenge.createdAt)
        assertEquals(expectedReceived.deadline, challenge.deadline)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `getChallengeArchive 성공 - 달 정보와 항목이 함께 1회 emit`() = runTest {
        val api = FakeChallengeApi(historiesResult = historySuccess())
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.getChallengeArchive(YearMonth(2026, 8), onError = { errors.add(it) }).toList()

        val archive = emitted.single()
        assertEquals(YearMonth(2026, 3), archive.firstArchivedMonth)
        assertEquals(listOf(42L), archive.histories.map { it.challengeId })
        assertEquals(ChallengeOutcome.WIN, archive.histories.single().myResult)
        assertEquals(emptyList(), errors)
    }

    /** `YearMonth.toString()` 이 곧 wire 포맷이라는 것을 고정한다 — zero-padding 이 빠지면 서버가 700 으로 거부한다. */
    @Test
    fun `getChallengeArchive - month 는 zero-padded yyyy-MM 문자열로 API 에 전달된다`() = runTest {
        val api = FakeChallengeApi(historiesResult = historySuccess())
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        dataSource.getChallengeArchive(YearMonth(2026, 3), onError = {}).toList()

        assertEquals("2026-03", api.capturedHistoryMonth)
    }

    /** `null` 은 결측이 아니라 "종료한 챌린지가 하나도 없다" 는 뜻이라, 그대로 화면까지 전달돼야 한다. */
    @Test
    fun `getChallengeArchive - firstArchivedMonth 가 null 이면 그대로 emit 된다`() = runTest {
        val api = FakeChallengeApi(
            historiesResult = historySuccess(firstArchivedMonth = null, histories = emptyList()),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.getChallengeArchive(YearMonth(2026, 8), onError = { errors.add(it) }).toList()

        val archive = emitted.single()
        assertNull(archive.firstArchivedMonth)
        assertTrue(archive.histories.isEmpty())
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `getChallengeArchive 실패 - onError 1회, emit 0건`() = runTest {
        val api = FakeChallengeApi(
            historiesResult = ApiResult.Failure.CustomError(code = 700, message = "잘못된 월 형식이에요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.getChallengeArchive(YearMonth(2027, 1), onError = { errors.add(it) }).toList()

        assertEquals(emptyList<ChallengeArchive>(), emitted)
        assertEquals(listOf("잘못된 월 형식이에요"), errors)
    }

    @Test
    fun `createChallenge 성공 - 서버가 준 challengeId 를 onSuccess 로 전달`() = runTest {
        val api = FakeChallengeApi(
            createResult = ApiResult.Success(
                ChallengeCreateResponse(
                    ChallengeCreateData(
                        challengeId = 7L,
                        status = "PENDING",
                        challengeDate = LocalDate(2026, 7, 28),
                        deadline = LocalDateTime(2026, 7, 28, 15, 0, 0),
                    ),
                ),
            ),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val successIds = mutableListOf<Long>()
        val errors = mutableListOf<String>()
        dataSource.createChallenge(createInput, onSuccess = { successIds.add(it) }, onError = { errors.add(it) })

        assertEquals(listOf(7L), successIds)
        assertEquals(emptyList(), errors)
        assertEquals(
            ChallengeCreateBody(
                opponentId = 42L,
                myMission = "오늘 운동 1시간 하기",
                betContent = "커피 사기",
                deadlineType = DeadlineTypeDto.TODAY,
                signature = encodedSignature,
            ),
            api.capturedCreateBody,
        )
    }

    @Test
    fun `createChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            createResult = ApiResult.Failure.CustomError(code = 700, message = "친구에게만 챌린지를 걸 수 있어요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        dataSource.createChallenge(createInput, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("친구에게만 챌린지를 걸 수 있어요"), errors)
    }

    @Test
    fun `acceptChallenge 성공 - onSuccess 호출, myMission 과 서명이 body 로 전달`() = runTest {
        val api = FakeChallengeApi(
            acceptResult = ApiResult.Success(
                AcceptChallengeResponse(AcceptChallengeData(challengeId = 7L, status = "IN_PROGRESS")),
            ),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCount = 0
        dataSource.acceptChallenge(7L, "책 30페이지 읽기", signature, onSuccess = { successCount++ }, onError = {})

        assertEquals(1, successCount)
        assertEquals(7L, api.capturedAcceptId)
        assertEquals(AcceptChallengeBody("책 30페이지 읽기", encodedSignature), api.capturedAcceptBody)
    }

    @Test
    fun `acceptChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            acceptResult = ApiResult.Failure.CustomError(code = 705, message = "이미 처리된 챌린지예요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        dataSource.acceptChallenge(
            challengeId = 7L,
            myMission = "책 30페이지 읽기",
            signature = signature,
            onSuccess = { successCalled = true },
            onError = { errors.add(it) },
        )

        assertTrue(!successCalled)
        assertEquals(listOf("이미 처리된 챌린지예요"), errors)
    }

    @Test
    fun `getChallengeDetail 성공 - 도메인으로 매핑돼 emit`() = runTest {
        val api = FakeChallengeApi(detailResult = ApiResult.Success(ChallengeDetailResponse(detailData)))
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val details = dataSource.getChallengeDetail(7L, onError = { errors.add(it) }).toList()

        assertEquals(7L, api.capturedDetailId)
        assertEquals(emptyList(), errors)
        val detail = details.single()
        assertEquals(7L, detail.challengeId)
        assertEquals("이우건", detail.me.nickname)
        assertEquals("테스터1", detail.opponent.nickname)
        assertEquals(LocalDateTime(2026, 8, 3, 11, 20, 0), assertNotNull(detail.contract).mySignedAt)
    }

    @Test
    fun `getChallengeDetail - deadline 이 null 이면 onError 호출하고 emit 없음`() = runTest {
        val api = FakeChallengeApi(
            detailResult = ApiResult.Success(ChallengeDetailResponse(detailData.copy(deadline = null))),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val details = dataSource.getChallengeDetail(7L, onError = { errors.add(it) }).toList()

        assertEquals(emptyList(), details)
        assertEquals(listOf("챌린지 정보를 불러오지 못했어요"), errors)
    }

    @Test
    fun `getChallengeDetail 실패 - onError 호출, emit 없음`() = runTest {
        val api = FakeChallengeApi(
            detailResult = ApiResult.Failure.CustomError(code = 700, message = "챌린지를 찾을 수 없어요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val details = dataSource.getChallengeDetail(7L, onError = { errors.add(it) }).toList()

        assertEquals(emptyList(), details)
        assertEquals(listOf("챌린지를 찾을 수 없어요"), errors)
    }

    @Test
    fun `rejectChallenge 성공 - onSuccess 호출`() = runTest {
        val api = FakeChallengeApi(
            rejectResult = ApiResult.Success(
                RejectChallengeResponse(RejectChallengeData(challengeId = 7L, status = "REJECTED")),
            ),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCount = 0
        dataSource.rejectChallenge(7L, onSuccess = { successCount++ }, onError = {})

        assertEquals(1, successCount)
        assertEquals(7L, api.capturedRejectId)
    }

    @Test
    fun `rejectChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            rejectResult = ApiResult.Failure.CustomError(code = 705, message = "마감이 지난 챌린지예요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        dataSource.rejectChallenge(7L, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("마감이 지난 챌린지예요"), errors)
    }

    @Test
    fun `forfeitChallenge 성공 - onSuccess 호출`() = runTest {
        val api = FakeChallengeApi(
            forfeitResult = ApiResult.Success(
                ForfeitChallengeResponse(
                    ForfeitChallengeData(
                        challengeId = 7L,
                        status = "COMPLETED",
                        result = "OPPONENT_WIN",
                        myResult = "LOSE",
                        completedAt = LocalDateTime(2026, 9, 5, 14, 23, 11),
                    ),
                ),
            ),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCount = 0
        dataSource.forfeitChallenge(7L, onSuccess = { successCount++ }, onError = {})

        assertEquals(1, successCount)
        assertEquals(7L, api.capturedForfeitId)
    }

    @Test
    fun `forfeitChallenge 실패 - onError 를 호출하고 onSuccess 는 부르지 않는다`() = runTest {
        val api = FakeChallengeApi(
            forfeitResult = ApiResult.Failure.CustomError(code = 705, message = "이미 인증해서 기권할 수 없어요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        dataSource.forfeitChallenge(7L, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("이미 인증해서 기권할 수 없어요"), errors)
    }

    @Test
    fun `cancelChallenge 성공 - onSuccess 호출`() = runTest {
        val api = FakeChallengeApi(
            cancelResult = ApiResult.Success(CancelChallengeResponse(CancelChallengeData(challengeId = 7L))),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCount = 0
        dataSource.cancelChallenge(7L, onSuccess = { successCount++ }, onError = {})

        assertEquals(1, successCount)
        assertEquals(7L, api.capturedCancelId)
    }

    @Test
    fun `cancelChallenge 실패 - onError 호출, onSuccess 미호출`() = runTest {
        val api = FakeChallengeApi(
            cancelResult = ApiResult.Failure.CustomError(code = 700, message = "내가 보낸 도전장이 아니에요"),
        )
        val dataSource = ChallengeRemoteDataSourceImpl(api)

        var successCalled = false
        val errors = mutableListOf<String>()
        dataSource.cancelChallenge(7L, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("내가 보낸 도전장이 아니에요"), errors)
    }
}

private class FakeChallengeApi(
    private val createResult: ApiResult<ChallengeCreateResponse> = ApiResult.Success(ChallengeCreateResponse()),
    private val receivedResult: ApiResult<ReceivedChallengesResponse> = ApiResult.Success(ReceivedChallengesResponse()),
    private val detailResult: ApiResult<ChallengeDetailResponse> = ApiResult.Success(
        ChallengeDetailResponse(ChallengeDetailData(me = ContractUserInfoDto(), opponent = ContractUserInfoDto())),
    ),
    private val historiesResult: ApiResult<ChallengeHistoryResponse> = ApiResult.Success(ChallengeHistoryResponse()),
    private val acceptResult: ApiResult<AcceptChallengeResponse> = ApiResult.Success(AcceptChallengeResponse()),
    private val rejectResult: ApiResult<RejectChallengeResponse> = ApiResult.Success(RejectChallengeResponse()),
    private val forfeitResult: ApiResult<ForfeitChallengeResponse> = ApiResult.Success(ForfeitChallengeResponse()),
    private val cancelResult: ApiResult<CancelChallengeResponse> = ApiResult.Success(CancelChallengeResponse()),
) : ChallengeApi {

    var getReceivedCallCount = 0
        private set
    var capturedHistoryMonth: String? = null
        private set
    var capturedCreateBody: ChallengeCreateBody? = null
        private set
    var capturedDetailId: Long? = null
        private set
    var capturedAcceptId: Long? = null
        private set
    var capturedAcceptBody: AcceptChallengeBody? = null
        private set
    var capturedRejectId: Long? = null
        private set
    var capturedForfeitId: Long? = null
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

    override suspend fun getChallengeDetail(challengeId: Long): ApiResult<ChallengeDetailResponse> {
        capturedDetailId = challengeId
        return detailResult
    }

    override suspend fun getChallengeHistories(month: String): ApiResult<ChallengeHistoryResponse> {
        capturedHistoryMonth = month
        return historiesResult
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

    override suspend fun forfeitChallenge(challengeId: Long): ApiResult<ForfeitChallengeResponse> {
        capturedForfeitId = challengeId
        return forfeitResult
    }

    override suspend fun cancelChallenge(challengeId: Long): ApiResult<CancelChallengeResponse> {
        capturedCancelId = challengeId
        return cancelResult
    }
}
