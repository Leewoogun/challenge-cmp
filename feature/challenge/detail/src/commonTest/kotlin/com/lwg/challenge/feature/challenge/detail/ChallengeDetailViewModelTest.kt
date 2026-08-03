package com.lwg.challenge.feature.challenge.detail

import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractParty
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.MY_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.MY_MISSION_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_NOT_SIGNED_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_UNREADABLE_HINT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val CHALLENGE_ID = 42L
private const val CHALLENGER_ID = 1L
private const val OPPONENT_ID = 2L

private const val CHALLENGER_NICKNAME = "우건"
private const val OPPONENT_NICKNAME = "지민"
private const val CHALLENGER_MISSION = "오늘 운동 1시간 하기"
private const val OPPONENT_MISSION = "책 30페이지 읽기"

/** 마감이 이미 지난 픽스처. 상대 시간 문자열이 실행 시점 시계에 흔들리지 않게 고정한다. */
private val PAST_DEADLINE = LocalDateTime.parse("2026-01-01T00:00:00")

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeDetailViewModelTest {

    private lateinit var fakeChallengeRepository: FakeChallengeRepository
    private lateinit var fakeUserInfoRepository: FakeUserInfoRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeChallengeRepository = FakeChallengeRepository()
        fakeChallengeRepository.detail = sampleDetail()
        fakeUserInfoRepository = FakeUserInfoRepository()
        fakeUserInfoRepository.userInfo = userInfo(CHALLENGER_ID)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ChallengeDetailViewModel =
        ChallengeDetailViewModel(
            challengeId = CHALLENGE_ID,
            challengeRepository = fakeChallengeRepository,
            userInfoRepository = fakeUserInfoRepository,
        )

    private fun ChallengeDetailViewModel.data(): ChallengeDetailUiState.Data =
        assertIs<ChallengeDetailUiState.Data>(uiState.value)

    private fun userInfo(id: Long) = UserInfo(
        id = id,
        kakaoId = 100L + id,
        nickname = "user$id",
        profileImageUrl = null,
    )

    private fun challengerSignature() = Signature(
        strokes = listOf(SignatureStroke(listOf(SignaturePoint(1, 2), SignaturePoint(3, 4)))),
    )

    private fun opponentSignature() = Signature(
        strokes = listOf(SignatureStroke(listOf(SignaturePoint(10, 20), SignaturePoint(30, 40)))),
    )

    private fun sampleDetail(
        challengerSignature: Signature? = challengerSignature(),
        challengerSignedAt: LocalDateTime? = PAST_DEADLINE,
        opponentSignature: Signature? = opponentSignature(),
        opponentSignedAt: LocalDateTime? = PAST_DEADLINE,
        isFinalized: Boolean = true,
    ) = ChallengeDetail(
        challengeId = CHALLENGE_ID,
        status = "IN_PROGRESS",
        challengeDate = LocalDate.parse("2026-01-01"),
        deadline = PAST_DEADLINE,
        betContent = "커피 사기",
        challenger = ContractParty(
            userId = CHALLENGER_ID,
            nickname = CHALLENGER_NICKNAME,
            profileImageUrl = null,
            mission = CHALLENGER_MISSION,
        ),
        opponent = ContractParty(
            userId = OPPONENT_ID,
            nickname = OPPONENT_NICKNAME,
            profileImageUrl = null,
            mission = OPPONENT_MISSION,
        ),
        contract = Contract(
            content = "영혼의 맹세",
            isFinalized = isFinalized,
            challengerSignature = challengerSignature,
            challengerSignedAt = challengerSignedAt,
            opponentSignature = opponentSignature,
            opponentSignedAt = opponentSignedAt,
        ),
    )

    @Test
    fun `init 시 상세를 조회하고 Data 로 전이한다`() = runTest {
        val viewModel = createViewModel()

        assertEquals(1, fakeChallengeRepository.detailCallCount)
        val state = viewModel.data()
        assertEquals("커피 사기", state.betContent)
        assertEquals(MY_LABEL, state.myLabel)
        assertEquals(OPPONENT_NICKNAME, state.opponentLabel)
    }

    @Test
    fun `상세 조회에 실패하면 Error 로 전이하고 retry 가 재조회한다`() = runTest {
        fakeChallengeRepository.detailError = "네트워크 오류가 발생하였습니다."
        val viewModel = createViewModel()

        val error = assertIs<ChallengeDetailUiState.Error>(viewModel.uiState.value)
        assertEquals("네트워크 오류가 발생하였습니다.", error.message)

        fakeChallengeRepository.detailError = null
        viewModel.retry()

        assertEquals(2, fakeChallengeRepository.detailCallCount)
        assertEquals(OPPONENT_NICKNAME, viewModel.data().opponentNickname)
    }

    @Test
    fun `내가 challenger 면 challenger 쪽이 나로 매핑된다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(CHALLENGER_ID)

        val state = createViewModel().data()

        assertEquals(MY_LABEL, state.myLabel)
        assertEquals(CHALLENGER_MISSION, state.myMission)
        assertEquals(challengerSignature(), state.mySignature)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
        assertEquals(OPPONENT_MISSION, state.opponentMission)
        assertEquals(opponentSignature(), state.opponentSignature)
    }

    @Test
    fun `내가 opponent 면 양쪽이 정확히 뒤집힌다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)

        val state = createViewModel().data()

        assertEquals(MY_LABEL, state.myLabel)
        assertEquals(OPPONENT_MISSION, state.myMission)
        assertEquals(opponentSignature(), state.mySignature)
        assertEquals(CHALLENGER_NICKNAME, state.opponentNickname)
        assertEquals(CHALLENGER_MISSION, state.opponentMission)
        assertEquals(challengerSignature(), state.opponentSignature)
    }

    @Test
    fun `userInfo 조회가 실패하면 나로 추측하지 않고 challenger 를 실명으로 왼쪽에 둔다`() = runTest {
        fakeUserInfoRepository.error = "사용자 정보를 불러오지 못했습니다."

        val state = createViewModel().data()

        assertEquals(CHALLENGER_NICKNAME, state.myLabel)
        assertEquals(CHALLENGER_MISSION, state.myMission)
        assertEquals(challengerSignature(), state.mySignature)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
        assertEquals(opponentSignature(), state.opponentSignature)
    }

    @Test
    fun `userId 가 양쪽 어디와도 맞지 않아도 관점 미상으로 처리한다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(id = 999L)

        val state = createViewModel().data()

        assertEquals(CHALLENGER_NICKNAME, state.myLabel)
        assertEquals(CHALLENGER_MISSION, state.myMission)
        assertEquals(challengerSignature(), state.mySignature)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
    }

    @Test
    fun `상대가 아직 서명하지 않았으면 서명이 없고 대기 중 문구가 붙는다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            opponentSignature = null,
            opponentSignedAt = null,
            isFinalized = false,
        )

        val state = createViewModel().data()

        assertNull(state.opponentSignature)
        assertEquals(SIGNATURE_NOT_SIGNED_HINT, state.opponentSignatureHint)
    }

    @Test
    fun `서명은 했는데 읽지 못했으면 대기 중이 아니라 불러오지 못했다고 알린다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            opponentSignature = null,
            opponentSignedAt = PAST_DEADLINE,
        )

        val state = createViewModel().data()

        assertNull(state.opponentSignature)
        assertEquals(SIGNATURE_UNREADABLE_HINT, state.opponentSignatureHint)
    }

    @Test
    fun `양측 서명이 완료되면 두 서명이 모두 있고 계약이 완결로 온다`() = runTest {
        val state = createViewModel().data()

        assertNotNull(state.mySignature)
        assertNotNull(state.opponentSignature)
        assertTrue(state.isFinalized)
    }

    @Test
    fun `deadlineText 는 매핑 시점 시계 기준으로 만들어진다`() = runTest {
        val state = createViewModel().data()

        assertEquals("마감", state.deadlineText)
    }

    @Test
    fun `관점을 알면 계약서 미션 행이 나의 미션으로 나온다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)

        val state = createViewModel().data()

        assertEquals(MY_MISSION_LABEL, state.myMissionLabel)
    }

    /** 서명 라벨만 실명으로 낮추고 미션 행은 "나" 라고 주장하면 한 카드가 스스로 모순된다. */
    @Test
    fun `관점 미상이면 미션 행과 서명 라벨이 함께 실명으로 떨어진다`() = runTest {
        fakeUserInfoRepository.error = "사용자 정보를 불러오지 못했습니다."

        val state = createViewModel().data()

        assertEquals("${CHALLENGER_NICKNAME}의 미션", state.myMissionLabel)
        assertEquals(CHALLENGER_NICKNAME, state.myLabel)
    }
}
