package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModelStore
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ChallengeSide
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractUserInfo
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationResult
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailModalEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.DEADLINE_AWAITING_JUDGEMENT_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.DEADLINE_REMAINING_PREFIX
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_ME_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_OTHER_SUFFIX
import com.lwg.challenge.feature.challenge.detail.contract.HeadlineTone
import com.lwg.challenge.feature.challenge.detail.contract.MY_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.MyMissionSlot
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_NOT_SIGNED_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_UNREADABLE_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import com.lwg.challenge.feature.challenge.detail.contract.VERIFICATION_REJECTED_PREFIX
import com.lwg.challenge.feature.challenge.detail.contract.VerificationSection
import com.lwg.challenge.ui.components.RESULT_BOTH_LOSE_TEXT
import com.lwg.challenge.ui.components.RESULT_DRAW_TEXT
import com.lwg.challenge.ui.components.RESULT_LOSE_TEXT
import com.lwg.challenge.ui.components.RESULT_WIN_TEXT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val CHALLENGE_ID = 42L

private const val MY_NICKNAME = "우건"
private const val OPPONENT_NICKNAME = "지민"
private const val MY_MISSION = "오늘 운동 1시간 하기"
private const val OPPONENT_MISSION = "책 30페이지 읽기"

private val PAST_DEADLINE = LocalDateTime.parse("2026-01-01T00:00:00")

private val CHALLENGE_DATE = LocalDate.parse("2026-01-01")

private val EXCLUSIVE_END_DEADLINE = LocalDateTime.parse("2026-01-02T00:00:00")

private val FUTURE_DEADLINE = LocalDateTime.parse("2099-12-31T23:59:00")

private const val VIEW_MODEL_KEY = "detail"

private val VERIFIED_AT = LocalDateTime.parse("2026-01-01T09:30:00")

private val REPLACED_VERIFIED_AT = LocalDateTime.parse("2026-01-01T11:05:00")

private const val MY_PHOTO_URL = "https://api.example.com/verifications/1/photo"
private const val OPPONENT_PHOTO_URL = "https://api.example.com/verifications/2/photo"

private val SAMPLE_MY_SIGNATURE = Signature(
    strokes = listOf(SignatureStroke(listOf(SignaturePoint(1, 2), SignaturePoint(3, 4)))),
)

private val SAMPLE_OPPONENT_SIGNATURE = Signature(
    strokes = listOf(SignatureStroke(listOf(SignaturePoint(10, 20), SignaturePoint(30, 40)))),
)

private const val AI_REASON = "책상 위 커피잔 사진으로 미션 수행 증거로 보기 어렵습니다."
private const val OPPONENT_AI_REASON = "사진에서 미션 수행 장면을 확인할 수 없습니다."

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeDetailViewModelTest {

    private lateinit var fakeChallengeRepository: FakeChallengeRepository
    private lateinit var fakeVerificationRepository: FakeVerificationRepository

    private val viewModelStore = ViewModelStore()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeChallengeRepository = FakeChallengeRepository()
        fakeChallengeRepository.detail = sampleDetail()
        fakeVerificationRepository = FakeVerificationRepository()
        fakeVerificationRepository.verifications = sampleVerifications()
    }

    @AfterTest
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    private fun TestScope.createViewModel(): ChallengeDetailViewModel =
        newViewModel().also { subscribeUiState(it) }

    private fun newViewModel(): ChallengeDetailViewModel =
        ChallengeDetailViewModel(
            challengeId = CHALLENGE_ID,
            challengeRepository = fakeChallengeRepository,
            verificationRepository = fakeVerificationRepository,
        ).also { viewModelStore.put(VIEW_MODEL_KEY, it) }

    private fun TestScope.subscribeUiState(viewModel: ChallengeDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun ChallengeDetailViewModel.data(): ChallengeDetailUiState.Data =
        assertIs<ChallengeDetailUiState.Data>(uiState.value)

    private fun ChallengeDetailViewModel.signatures(): SignatureSection = assertNotNull(data().signatures)

    private fun ChallengeDetailViewModel.verifications(): VerificationSection =
        assertNotNull(data().verifications)

    private fun sampleDetail(
        status: String = "IN_PROGRESS",
        myResult: ChallengeOutcome? = null,
        forfeitedBy: ChallengeSide? = null,
        challengeDate: LocalDate? = CHALLENGE_DATE,
        deadline: LocalDateTime = PAST_DEADLINE,
        myMission: String? = MY_MISSION,
        opponentNickname: String = OPPONENT_NICKNAME,
        opponentMission: String? = OPPONENT_MISSION,
        mySignature: Signature? = SAMPLE_MY_SIGNATURE,
        mySignedAt: LocalDateTime? = PAST_DEADLINE,
        opponentSignature: Signature? = SAMPLE_OPPONENT_SIGNATURE,
        opponentSignedAt: LocalDateTime? = PAST_DEADLINE,
        isFinalized: Boolean = true,
        contract: Contract? = Contract(
            content = "영혼의 맹세",
            isFinalized = isFinalized,
            mySignature = mySignature,
            mySignedAt = mySignedAt,
            opponentSignature = opponentSignature,
            opponentSignedAt = opponentSignedAt,
        ),
    ) = ChallengeDetail(
        challengeId = CHALLENGE_ID,
        status = status,
        myResult = myResult,
        forfeitedBy = forfeitedBy,
        challengeDate = challengeDate,
        deadline = deadline,
        betContent = "커피 사기",
        me = ContractUserInfo(
            nickname = MY_NICKNAME,
            profileImageUrl = null,
            mission = myMission,
        ),
        opponent = ContractUserInfo(
            nickname = opponentNickname,
            profileImageUrl = null,
            mission = opponentMission,
        ),
        contract = contract,
    )

    private fun completedDetail(myResult: ChallengeOutcome) =
        sampleDetail(status = "COMPLETED", myResult = myResult)

    private fun sampleVerifications(
        myStatus: VerificationStatus = VerificationStatus.PENDING,
        myPhotoUrl: String? = null,
        myPhotoDeleted: Boolean = false,
        myVerifiedAt: LocalDateTime? = VERIFIED_AT,
        myAiReason: String? = null,
        opponentStatus: VerificationStatus = VerificationStatus.PENDING,
        opponentPhotoUrl: String? = null,
        opponentPhotoDeleted: Boolean = false,
        opponentVerifiedAt: LocalDateTime? = VERIFIED_AT,
        opponentAiReason: String? = null,
    ) = ChallengeVerifications(
        challengeId = CHALLENGE_ID,
        myVerification = party(
            status = myStatus,
            photoUrl = myPhotoUrl,
            photoDeleted = myPhotoDeleted,
            verifiedAt = myVerifiedAt,
            aiReason = myAiReason,
        ),
        opponentVerification = party(
            status = opponentStatus,
            photoUrl = opponentPhotoUrl,
            photoDeleted = opponentPhotoDeleted,
            verifiedAt = opponentVerifiedAt,
            aiReason = opponentAiReason,
        ),
    )

    private fun party(
        status: VerificationStatus,
        photoUrl: String?,
        photoDeleted: Boolean = false,
        verifiedAt: LocalDateTime? = VERIFIED_AT,
        aiReason: String? = null,
    ) = VerificationResult(
        status = status,
        photoUrl = photoUrl,
        verifiedAt = verifiedAt.takeIf {
            status == VerificationStatus.VERIFIED || photoUrl != null || photoDeleted
        },
        photoDeleted = photoDeleted,
        aiReason = aiReason,
    )

    @Test
    fun `구독이 끊겼다 다시 붙으면 재조회한다`() = runTest {
        val viewModel = newViewModel()

        val subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        assertEquals(1, fakeChallengeRepository.detailCallCount)

        subscription.cancelAndJoin()
        runCurrent()

        subscribeUiState(viewModel)
        runCurrent()

        assertEquals(2, fakeChallengeRepository.detailCallCount)
    }

    @Test
    fun `재구독 재조회 중에도 직전 Data 를 잃지 않는다`() = runTest {
        val viewModel = newViewModel()

        val subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val before = viewModel.data()

        subscription.cancelAndJoin()
        runCurrent()

        fakeChallengeRepository.detailDelayMillis = 1_000
        subscribeUiState(viewModel)
        runCurrent()

        assertEquals(before, viewModel.data())
    }

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
    fun `상대가 아직 서명하지 않았으면 서명이 없고 대기 중 문구가 붙는다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            opponentSignature = null,
            opponentSignedAt = null,
            isFinalized = false,
        )

        val signatures = createViewModel().signatures()

        assertNull(signatures.opponentSignature)
        assertEquals(SIGNATURE_NOT_SIGNED_HINT, signatures.opponentSignatureHint)
    }

    @Test
    fun `서명은 했는데 읽지 못했으면 대기 중이 아니라 불러오지 못했다고 알린다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            opponentSignature = null,
            opponentSignedAt = PAST_DEADLINE,
        )

        val signatures = createViewModel().signatures()

        assertNull(signatures.opponentSignature)
        assertEquals(SIGNATURE_UNREADABLE_HINT, signatures.opponentSignatureHint)
    }

    @Test
    fun `양측 서명이 완료되면 두 서명이 모두 있고 계약이 완결로 온다`() = runTest {
        val signatures = createViewModel().signatures()

        assertNotNull(signatures.mySignature)
        assertNotNull(signatures.opponentSignature)
        assertTrue(signatures.isFinalized)
    }

    @Test
    fun `headlineText 는 매핑 시점 시계 기준으로 만들어진다`() = runTest {
        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
    }

    @Test
    fun `마감 전이면 남은 시간에 접두어가 붙고 톤이 URGENT 다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(deadline = FUTURE_DEADLINE)

        val state = createViewModel().data()

        assertTrue(
            state.headlineText.startsWith(DEADLINE_REMAINING_PREFIX),
            "접두어가 빠졌다: ${state.headlineText}",
        )
        assertEquals(HeadlineTone.URGENT, state.headlineTone)

        viewModelStore.clear()
    }

    @Test
    fun `마감 후면 판정 대기 중이고 톤이 NEUTRAL 이다`() = runTest {
        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
        assertEquals(HeadlineTone.NEUTRAL, state.headlineTone)
    }

    @Test
    fun `myResult 가 WIN 이면 승리로 나온다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeOutcome.WIN)

        val state = createViewModel().data()

        assertEquals(RESULT_WIN_TEXT, state.headlineText)
        assertEquals(HeadlineTone.WIN, state.headlineTone)
    }

    @Test
    fun `myResult 가 LOSE 면 패배로 나온다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeOutcome.LOSE)

        val state = createViewModel().data()

        assertEquals(RESULT_LOSE_TEXT, state.headlineText)
        assertEquals(HeadlineTone.LOSE, state.headlineTone)
    }

    @Test
    fun `DRAW 는 무승부로 나온다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeOutcome.DRAW)

        val state = createViewModel().data()

        assertEquals(RESULT_DRAW_TEXT, state.headlineText)
        assertEquals(HeadlineTone.DRAW, state.headlineTone)
    }

    @Test
    fun `BOTH_LOSE 는 양측 패배로 나오고 톤이 LOSE 다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeOutcome.BOTH_LOSE)

        val state = createViewModel().data()

        assertEquals(RESULT_BOTH_LOSE_TEXT, state.headlineText)
        assertEquals(HeadlineTone.LOSE, state.headlineTone)
    }

    @Test
    fun `COMPLETED 인데 result 가 null 이면 판정 대기 중으로 남는다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")

        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
        assertEquals(HeadlineTone.NEUTRAL, state.headlineTone)
    }

    @Test
    fun `마감 전이라도 결과가 있으면 결과 표기가 이긴다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            myResult = ChallengeOutcome.WIN,
            deadline = FUTURE_DEADLINE,
        )

        val state = createViewModel().data()

        assertEquals(RESULT_WIN_TEXT, state.headlineText)
        assertEquals(HeadlineTone.WIN, state.headlineTone)

        viewModelStore.clear()
    }

    @Test
    fun `절대 마감 시각은 challengeDate 기준이라 하루 밀리지 않는다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(deadline = EXCLUSIVE_END_DEADLINE)

        assertEquals("1/1 24:00", createViewModel().data().deadlineAbsoluteText)
    }

    @Test
    fun `challengeDate 가 없으면 절대 마감 시각을 deadline 에서 역산한다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            challengeDate = null,
            deadline = EXCLUSIVE_END_DEADLINE,
        )

        assertEquals("1/1 24:00", createViewModel().data().deadlineAbsoluteText)
    }

    @Test
    fun `계약서가 없으면 서명 묶음만 빠지고 계약 조건은 그대로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(contract = null)

        val state = createViewModel().data()

        assertNull(
            state.signatures,
            "없는 맹세를 '서명 대기 중' 으로 그리지 않도록 묶음 자체가 없어야 한다",
        )
        assertEquals(MY_MISSION, state.myMission)
        assertEquals(OPPONENT_MISSION, state.opponentMission)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
        assertEquals("커피 사기", state.betContent)
        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
    }

    @Test
    fun `계약서가 있으면 서명 묶음이 채워진다`() = runTest {
        val signatures = createViewModel().signatures()

        assertEquals(SAMPLE_MY_SIGNATURE, signatures.mySignature)
        assertEquals(SAMPLE_OPPONENT_SIGNATURE, signatures.opponentSignature)
        assertEquals(SIGNATURE_NOT_SIGNED_HINT, signatures.mySignatureHint)
        assertTrue(signatures.isFinalized)
    }

    @Test
    fun `IN_PROGRESS 면 인증 진입점이 열린다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "IN_PROGRESS")

        assertTrue(createViewModel().data().canVerify)
    }

    @Test
    fun `IN_PROGRESS 가 아니면 인증 진입점이 닫힌다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        assertFalse(createViewModel().data().canVerify)

        fakeChallengeRepository.detail = sampleDetail(status = "PENDING")
        assertFalse(createViewModel().data().canVerify)
    }

    @Test
    fun `내가 이미 인증했어도 재촬영 진입점이 열린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
        )

        val state = createViewModel().data()

        assertTrue(state.canVerify)
        assertEquals(MyMissionSlot.MY_PHOTO_WITH_RETAKE, state.myMissionSlot)
    }

    @Test
    fun `내 인증이 PENDING 이면 인증 진입점이 열린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.PENDING,
        )

        assertTrue(createViewModel().data().canVerify)
    }

    @Test
    fun `IN_PROGRESS 가 아니면 내 인증 상태와 무관하게 진입점이 닫힌다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")

        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.PENDING,
        )
        val pendingState = createViewModel().data()
        assertFalse(pendingState.canVerify)
        assertEquals(MyMissionSlot.NONE, pendingState.myMissionSlot)

        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
        )
        val verifiedState = createViewModel().data()
        assertFalse(verifiedState.canVerify)
        assertEquals(
            MyMissionSlot.MY_PHOTO,
            verifiedState.myMissionSlot,
            "마감 뒤에는 인증 완료 표시만 남고 재촬영은 붙지 않는다",
        )
    }

    @Test
    fun `내 미션 슬롯이 챌린지 상태와 내 인증 상태의 조합으로 갈린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.PENDING,
        )
        assertEquals(MyMissionSlot.VERIFY_CTA, createViewModel().data().myMissionSlot)

        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
        )
        assertEquals(MyMissionSlot.MY_PHOTO_WITH_RETAKE, createViewModel().data().myMissionSlot)

        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.FAILED,
        )
        assertEquals(MyMissionSlot.NONE, createViewModel().data().myMissionSlot)

        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
        )
        assertEquals(MyMissionSlot.MY_PHOTO, createViewModel().data().myMissionSlot)
    }

    @Test
    fun `AI 가 반려한 인증은 사진이 남아 내 카드에 그대로 그려진다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.FAILED,
            myPhotoUrl = MY_PHOTO_URL,
            myAiReason = AI_REASON,
        )

        val state = createViewModel().data()

        assertEquals(VerificationStatus.FAILED, state.myVerificationStatus)
        assertEquals(MyMissionSlot.MY_PHOTO, state.myMissionSlot)
        assertEquals(MY_PHOTO_URL, state.myPhotoUrl, "반려돼도 낸 사진은 남는다")
        assertFalse(state.canVerify, "판정은 마감 시점에 내려지므로 재촬영 창이 없다")
    }

    @Test
    fun `사진 없는 FAILED 는 미제출이라 슬롯이 비어 있다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.FAILED,
        )

        val state = createViewModel().data()

        assertEquals(MyMissionSlot.NONE, state.myMissionSlot)
        assertNull(state.myPhotoUrl)
        assertNull(state.verifications?.myVerifiedAt, "낸 적이 없으니 확정 시각도 없다")
    }

    @Test
    fun `AI 판정 사유가 양측 모두 State 까지 도달한다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.FAILED,
            myPhotoUrl = MY_PHOTO_URL,
            myAiReason = AI_REASON,
            opponentStatus = VerificationStatus.FAILED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
            opponentAiReason = OPPONENT_AI_REASON,
        )

        val state = createViewModel().data()

        assertEquals(AI_REASON, state.myAiReason)
        assertEquals(OPPONENT_AI_REASON, state.opponentAiReason)
    }

    @Test
    fun `AI 판정 사유가 없으면 State 에서도 null 이다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
        )

        val state = createViewModel().data()

        assertNull(state.myAiReason)
        assertNull(state.opponentAiReason)
        assertNull(state.myAiReasonText, "그릴 사유가 없으면 접두어만 남은 문구를 만들지 않는다")
        assertNull(state.opponentAiReasonText)
    }

    @Test
    fun `AI 판정 사유는 접두어가 붙은 표시 문구로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.FAILED,
            myPhotoUrl = MY_PHOTO_URL,
            myAiReason = AI_REASON,
            opponentStatus = VerificationStatus.FAILED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
            opponentAiReason = OPPONENT_AI_REASON,
        )

        val state = createViewModel().data()

        assertEquals("$VERIFICATION_REJECTED_PREFIX$AI_REASON", state.myAiReasonText)
        assertEquals("$VERIFICATION_REJECTED_PREFIX$OPPONENT_AI_REASON", state.opponentAiReasonText)
    }

    @Test
    fun `인증 현황 조회에 실패해도 Data 를 유지하고 진입점을 열어 둔다`() = runTest {
        fakeVerificationRepository.getVerificationsError = "네트워크 오류가 발생하였습니다."

        val state = createViewModel().data()

        assertNull(state.verifications)
        assertTrue(state.canVerify)
        assertEquals(MY_MISSION, state.myMission, "계약서는 그대로 보여야 한다")
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
    }

    @Test
    fun `양측 인증 사진 URL 이 나와 상대 기준으로 실린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
        )

        val verifications = createViewModel().verifications()

        assertEquals(MY_PHOTO_URL, verifications.myPhotoUrl)
        assertEquals(VerificationStatus.VERIFIED, verifications.myStatus)
        assertEquals(OPPONENT_PHOTO_URL, verifications.opponentPhotoUrl)
        assertEquals(VerificationStatus.VERIFIED, verifications.opponentStatus)
    }

    @Test
    fun `탈퇴한 상대의 사진 삭제 표시가 상대 칸으로 실린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = null,
            opponentPhotoDeleted = true,
        )

        val verifications = createViewModel().verifications()

        assertTrue(verifications.opponentPhotoDeleted)
        assertNull(verifications.opponentPhotoUrl)
        assertEquals(VerificationStatus.VERIFIED, verifications.opponentStatus)
        assertFalse(verifications.myPhotoDeleted)
    }

    @Test
    fun `인증했는데 URL 만 없는 칸은 삭제로 읽지 않는다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = null,
            opponentPhotoDeleted = false,
        )

        val verifications = createViewModel().verifications()

        assertFalse(verifications.opponentPhotoDeleted, "URL 유실은 탈퇴와 다른 신호로 남아야 한다")
        assertNull(verifications.opponentPhotoUrl)
    }

    @Test
    fun `인증 현황을 모르면 사진이 삭제됐다고 단정하지 않는다`() = runTest {
        fakeVerificationRepository.getVerificationsError = "네트워크 오류가 발생하였습니다."

        val data = createViewModel().data()

        assertNull(data.verifications)
        assertFalse(data.myPhotoDeleted)
        assertFalse(data.opponentPhotoDeleted)
    }

    @Test
    fun `아직 아무도 인증하지 않았으면 두 칸이 모두 PENDING 으로 채워진다`() = runTest {
        val verifications = createViewModel().verifications()

        assertEquals(VerificationStatus.PENDING, verifications.myStatus)
        assertEquals(VerificationStatus.PENDING, verifications.opponentStatus)
        assertNull(verifications.myPhotoUrl)
        assertNull(verifications.opponentPhotoUrl)
    }

    @Test
    fun `사진을 교체하면 URL 이 같아도 캐시 키가 달라진다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
            myVerifiedAt = VERIFIED_AT,
        )
        val viewModel = createViewModel()
        val before = viewModel.data().myPhotoCacheKey

        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
            myVerifiedAt = REPLACED_VERIFIED_AT,
        )
        viewModel.retry()
        val replaced = viewModel.data()

        assertEquals(MY_PHOTO_URL, replaced.myPhotoUrl, "교체돼도 사진 경로는 그대로다")
        assertNotEquals(before, replaced.myPhotoCacheKey, "경로가 같아도 키는 갈려야 한다")
    }

    @Test
    fun `인증 시각이 그대로면 캐시 키도 그대로다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
        )
        val viewModel = createViewModel()
        val before = viewModel.data().myPhotoCacheKey

        viewModel.retry()

        assertEquals(before, viewModel.data().myPhotoCacheKey, "안 바뀐 사진을 다시 받게 만들면 안 된다")
    }

    @Test
    fun `인증 시각이 없으면 캐시 키가 URL 로만 떨어진다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
            myVerifiedAt = null,
        )

        val state = createViewModel().data()

        assertEquals(MY_PHOTO_URL, state.myPhotoCacheKey)
    }

    @Test
    fun `사진 URL 이 없으면 캐시 키도 null 이다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = null,
            myPhotoDeleted = true,
        )

        val state = createViewModel().data()

        assertNull(state.myPhotoCacheKey, "탈퇴로 지워진 사진에는 캐시에서 집을 대상이 없다")
        assertNull(state.opponentPhotoCacheKey)
    }

    @Test
    fun `나와 상대의 캐시 키가 서로 섞이지 않는다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
            myVerifiedAt = VERIFIED_AT,
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
            opponentVerifiedAt = REPLACED_VERIFIED_AT,
        )

        val data = createViewModel().data()

        assertEquals("$MY_PHOTO_URL|$VERIFIED_AT", data.myPhotoCacheKey)
        assertEquals("$OPPONENT_PHOTO_URL|$REPLACED_VERIFIED_AT", data.opponentPhotoCacheKey)
    }

    @Test
    fun `인증 현황을 모르면 캐시 키도 null 이다`() = runTest {
        fakeVerificationRepository.getVerificationsError = "네트워크 오류가 발생하였습니다."

        val state = createViewModel().data()

        assertNull(state.verifications)
        assertNull(state.myPhotoCacheKey)
        assertNull(state.opponentPhotoCacheKey)
    }

    @Test
    fun `IN_PROGRESS 이고 내 인증이 없으면 기권 진입점이 열린다`() = runTest {
        val state = createViewModel().data()

        assertTrue(state.canForfeit)
        assertEquals(MyMissionSlot.VERIFY_CTA, state.myMissionSlot)
    }

    @Test
    fun `내가 인증하면 기권 진입점이 닫히고 그 자리가 다시 찍기로 바뀐다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            myStatus = VerificationStatus.VERIFIED,
            myPhotoUrl = MY_PHOTO_URL,
        )

        val state = createViewModel().data()

        assertFalse(state.canForfeit)
        assertEquals(MyMissionSlot.MY_PHOTO_WITH_RETAKE, state.myMissionSlot)
    }

    @Test
    fun `기권 진입점과 다시 찍기는 어떤 상태에서도 동시에 보이지 않는다`() = runTest {
        val myStatuses = listOf(
            VerificationStatus.PENDING,
            VerificationStatus.VERIFIED,
            VerificationStatus.FAILED,
            null,
        )

        for (challengeStatus in listOf("IN_PROGRESS", "COMPLETED")) {
            for (myStatus in myStatuses) {
                fakeChallengeRepository.detail = sampleDetail(status = challengeStatus)
                fakeVerificationRepository.getVerificationsError =
                    if (myStatus == null) "네트워크 오류가 발생하였습니다." else null
                fakeVerificationRepository.verifications = sampleVerifications(
                    myStatus = myStatus ?: VerificationStatus.PENDING,
                    myPhotoUrl = MY_PHOTO_URL
                        .takeIf { myStatus == VerificationStatus.VERIFIED },
                )

                val state = createViewModel().data()

                assertFalse(
                    state.canForfeit && state.myMissionSlot == MyMissionSlot.MY_PHOTO_WITH_RETAKE,
                    "challengeStatus=$challengeStatus / myStatus=$myStatus 에서 둘이 함께 열렸다",
                )
            }
        }
    }

    @Test
    fun `COMPLETED 면 기권 진입점이 닫힌다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")

        assertFalse(createViewModel().data().canForfeit)
    }

    @Test
    fun `인증 현황을 몰라도 기권 진입점이 인증 CTA 와 함께 열린다`() = runTest {
        fakeVerificationRepository.getVerificationsError = "네트워크 오류가 발생하였습니다."

        val state = createViewModel().data()

        assertNull(state.verifications)
        assertTrue(state.canVerify)
        assertTrue(state.canForfeit)
    }

    @Test
    fun `기권 진입점을 눌러도 확인 전에는 요청이 나가지 않는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.requestForfeit()

        assertEquals(ChallengeDetailModalEffect.ConfirmForfeit, viewModel.modalEffect.value)
        assertEquals(0, fakeChallengeRepository.forfeitCallCount)
    }

    @Test
    fun `확인하면 기권 요청이 나가고 다이얼로그가 닫힌다`() = runTest {
        val viewModel = createViewModel()
        viewModel.requestForfeit()

        viewModel.forfeit()

        assertEquals(1, fakeChallengeRepository.forfeitCallCount)
        assertEquals(ChallengeDetailModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `다이얼로그를 닫으면 요청이 나가지 않는다`() = runTest {
        val viewModel = createViewModel()
        viewModel.requestForfeit()

        viewModel.dismiss()

        assertEquals(ChallengeDetailModalEffect.Hidden, viewModel.modalEffect.value)
        assertEquals(0, fakeChallengeRepository.forfeitCallCount)
    }

    @Test
    fun `요청이 나가 있는 동안 다시 확정해도 요청은 한 번만 나간다`() = runTest {
        fakeChallengeRepository.forfeitDelayMillis = 1_000
        val viewModel = createViewModel()

        viewModel.forfeit()
        viewModel.forfeit()

        assertEquals(1, fakeChallengeRepository.forfeitCallCount)
    }

    @Test
    fun `기권에 성공하면 상세를 다시 조회한다`() = runTest {
        val viewModel = createViewModel()
        assertEquals(1, fakeChallengeRepository.detailCallCount)

        viewModel.forfeit()

        assertEquals(2, fakeChallengeRepository.detailCallCount)
    }

    @Test
    fun `기권에 실패하면 메시지를 흘리고 그래도 상세를 다시 조회한다`() = runTest {
        fakeChallengeRepository.forfeitError = "이미 인증을 완료한 챌린지입니다."
        val viewModel = createViewModel()

        val messages = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect { effect ->
                messages += assertIs<ChallengeDetailUiEffect.ShowMessage>(effect).message
            }
        }

        viewModel.forfeit()

        assertEquals(listOf("이미 인증을 완료한 챌린지입니다."), messages)
        assertEquals(2, fakeChallengeRepository.detailCallCount)
    }

    @Test
    fun `기권 요청이 나가 있는 동안 진입점이 잠긴다`() = runTest {
        fakeChallengeRepository.forfeitDelayMillis = 1_000
        val viewModel = createViewModel()

        viewModel.forfeit()

        val forfeiting = viewModel.data()
        assertTrue(forfeiting.isForfeiting)
        assertEquals(MY_MISSION, forfeiting.myMission, "계약서는 그대로 보여야 한다")

        advanceUntilIdle()
        assertFalse(viewModel.data().isForfeiting)
    }

    @Test
    fun `내가 기권했으면 안내가 내 시점으로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            myResult = ChallengeOutcome.LOSE,
            forfeitedBy = ChallengeSide.ME,
        )

        assertEquals(FORFEIT_BY_ME_TEXT, createViewModel().data().forfeitNoticeText)
    }

    @Test
    fun `상대가 기권했으면 안내가 상대 실명으로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            myResult = ChallengeOutcome.WIN,
            forfeitedBy = ChallengeSide.OPPONENT,
        )

        assertEquals(
            "$OPPONENT_NICKNAME$FORFEIT_BY_OTHER_SUFFIX",
            createViewModel().data().forfeitNoticeText,
        )
    }

    @Test
    fun `기권이 아니면 안내가 붙지 않는다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeOutcome.WIN)

        assertNull(createViewModel().data().forfeitNoticeText)
    }
}
