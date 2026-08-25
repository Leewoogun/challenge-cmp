package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModelStore
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractParty
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.PartyVerification
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.DEADLINE_AWAITING_JUDGEMENT_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.DEADLINE_REMAINING_PREFIX
import com.lwg.challenge.feature.challenge.detail.contract.DeadlineTone
import com.lwg.challenge.feature.challenge.detail.contract.MY_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.MY_MISSION_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_NOT_SIGNED_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_UNREADABLE_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import com.lwg.challenge.feature.challenge.detail.contract.VerificationSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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

private val CHALLENGE_DATE = LocalDate.parse("2026-01-01")

/** 서버가 주는 실제 모양 — 챌린지 당일의 끝을 **익일 00:00** 이라는 배타적 끝점으로 내려준다. */
private val EXCLUSIVE_END_DEADLINE = LocalDateTime.parse("2026-01-02T00:00:00")

/** 실행 시점과 무관하게 항상 미래. 마감 전 분기를 실제 시계로도 성립시키려면 고정 미래여야 한다. */
private val FUTURE_DEADLINE = LocalDateTime.parse("2099-12-31T23:59:00")

private const val VIEW_MODEL_KEY = "detail"

private val VERIFIED_AT = LocalDateTime.parse("2026-01-01T09:30:00")

private const val CHALLENGER_PHOTO_URL = "https://api.example.com/verifications/1/photo"
private const val OPPONENT_PHOTO_URL = "https://api.example.com/verifications/2/photo"

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeDetailViewModelTest {

    private lateinit var fakeChallengeRepository: FakeChallengeRepository
    private lateinit var fakeUserInfoRepository: FakeUserInfoRepository
    private lateinit var fakeVerificationRepository: FakeVerificationRepository

    /**
     * 마감 티커를 멈출 유일한 손잡이.
     *
     * `ViewModel.clear()` 가 lifecycle 모듈 internal 이라 스토어를 거쳐야만 `viewModelScope` 를
     * 끊을 수 있다. 마감이 남은 픽스처에서 이걸 빼면 티커의 1분 `delay` 가 runTest 의 스케줄러에
     * 영원히 남아 테스트가 끝나지 않는다.
     */
    private val viewModelStore = ViewModelStore()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeChallengeRepository = FakeChallengeRepository()
        fakeChallengeRepository.detail = sampleDetail()
        fakeUserInfoRepository = FakeUserInfoRepository()
        fakeUserInfoRepository.userInfo = userInfo(CHALLENGER_ID)
        fakeVerificationRepository = FakeVerificationRepository()
        fakeVerificationRepository.verifications = sampleVerifications()
    }

    @AfterTest
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    /** uiState 가 WhileSubscribed 이므로 구독자가 있어야 조회가 시작된다. */
    private fun TestScope.createViewModel(): ChallengeDetailViewModel =
        newViewModel().also { subscribeUiState(it) }

    /** 구독 시점을 직접 다루는 테스트용 — 그 외에는 [createViewModel] 을 쓴다. */
    private fun newViewModel(): ChallengeDetailViewModel =
        ChallengeDetailViewModel(
            challengeId = CHALLENGE_ID,
            challengeRepository = fakeChallengeRepository,
            userInfoRepository = fakeUserInfoRepository,
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
        status: String = "IN_PROGRESS",
        challengeDate: LocalDate? = CHALLENGE_DATE,
        deadline: LocalDateTime = PAST_DEADLINE,
        challengerSignature: Signature? = challengerSignature(),
        challengerSignedAt: LocalDateTime? = PAST_DEADLINE,
        opponentSignature: Signature? = opponentSignature(),
        opponentSignedAt: LocalDateTime? = PAST_DEADLINE,
        isFinalized: Boolean = true,
        contract: Contract? = Contract(
            content = "영혼의 맹세",
            isFinalized = isFinalized,
            challengerSignature = challengerSignature,
            challengerSignedAt = challengerSignedAt,
            opponentSignature = opponentSignature,
            opponentSignedAt = opponentSignedAt,
        ),
    ) = ChallengeDetail(
        challengeId = CHALLENGE_ID,
        status = status,
        challengeDate = challengeDate,
        deadline = deadline,
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
        contract = contract,
    )

    /** 서버 응답 그대로 **역할 기준**이다. '나 / 상대' 로 세우는 건 ViewModel 의 몫이다. */
    private fun sampleVerifications(
        challengerStatus: VerificationStatus = VerificationStatus.PENDING,
        challengerPhotoUrl: String? = null,
        opponentStatus: VerificationStatus = VerificationStatus.PENDING,
        opponentPhotoUrl: String? = null,
    ) = ChallengeVerifications(
        challengeId = CHALLENGE_ID,
        challenger = party(CHALLENGER_ID, challengerStatus, challengerPhotoUrl),
        opponent = party(OPPONENT_ID, opponentStatus, opponentPhotoUrl),
    )

    private fun party(
        userId: Long,
        status: VerificationStatus,
        photoUrl: String?,
    ) = PartyVerification(
        userId = userId,
        status = status,
        photoUrl = photoUrl,
        verifiedAt = VERIFIED_AT.takeIf { status == VerificationStatus.VERIFIED },
    )

    /** 인증을 마치고 상세로 돌아오는 경로. 백스택에 남아 있던 ViewModel 이 그대로 재구독된다. */
    @Test
    fun `구독이 끊겼다 다시 붙으면 재조회한다`() = runTest {
        val viewModel = newViewModel()

        val subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        assertEquals(1, fakeChallengeRepository.detailCallCount)

        // 인증 화면 push 로 상세 컴포지션이 dispose 되는 상황. stopTimeout 이 0이라도 STOP 은
        // 스케줄러를 한 번 돌려야 실제로 발화하므로 runCurrent 가 필요하다.
        subscription.cancelAndJoin()
        runCurrent()

        subscribeUiState(viewModel)
        runCurrent()

        assertEquals(2, fakeChallengeRepository.detailCallCount)
    }

    /** 재조회 동안 스피너로 비우면 앱을 잠깐 벗어났다 돌아오는 것만으로 읽던 계약서가 사라진다. */
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
    fun `내가 challenger 면 challenger 쪽이 나로 매핑된다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(CHALLENGER_ID)

        val viewModel = createViewModel()
        val state = viewModel.data()
        val signatures = viewModel.signatures()

        assertEquals(MY_LABEL, state.myLabel)
        assertEquals(CHALLENGER_MISSION, state.myMission)
        assertEquals(challengerSignature(), signatures.mySignature)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
        assertEquals(OPPONENT_MISSION, state.opponentMission)
        assertEquals(opponentSignature(), signatures.opponentSignature)
    }

    @Test
    fun `내가 opponent 면 양쪽이 정확히 뒤집힌다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)

        val viewModel = createViewModel()
        val state = viewModel.data()
        val signatures = viewModel.signatures()

        assertEquals(MY_LABEL, state.myLabel)
        assertEquals(OPPONENT_MISSION, state.myMission)
        assertEquals(opponentSignature(), signatures.mySignature)
        assertEquals(CHALLENGER_NICKNAME, state.opponentNickname)
        assertEquals(CHALLENGER_MISSION, state.opponentMission)
        assertEquals(challengerSignature(), signatures.opponentSignature)
    }

    @Test
    fun `userInfo 조회가 실패하면 나로 추측하지 않고 challenger 를 실명으로 왼쪽에 둔다`() = runTest {
        fakeUserInfoRepository.error = "사용자 정보를 불러오지 못했습니다."

        val viewModel = createViewModel()
        val state = viewModel.data()
        val signatures = viewModel.signatures()

        assertEquals(CHALLENGER_NICKNAME, state.myLabel)
        assertEquals(CHALLENGER_MISSION, state.myMission)
        assertEquals(challengerSignature(), signatures.mySignature)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
        assertEquals(opponentSignature(), signatures.opponentSignature)
    }

    @Test
    fun `userId 가 양쪽 어디와도 맞지 않아도 관점 미상으로 처리한다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(id = 999L)

        val viewModel = createViewModel()
        val state = viewModel.data()

        assertEquals(CHALLENGER_NICKNAME, state.myLabel)
        assertEquals(CHALLENGER_MISSION, state.myMission)
        assertEquals(challengerSignature(), viewModel.signatures().mySignature)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
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
    fun `deadlineText 는 매핑 시점 시계 기준으로 만들어진다`() = runTest {
        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.deadlineText)
    }

    // 티커가 실제로 값을 갱신하는지는 여기서 검증하지 못한다. ViewModel 이 nowKst() 로 실제 시계를
    // 직접 읽는데 runTest 의 가상 시간은 그 시계를 움직이지 못해서, delay 를 얼마나 진행시키든 티커가
    // 다시 만드는 문자열이 직전 값과 같다 — 통과해도 갱신을 증명하지 못하는 테스트가 된다.
    // 검증하려면 ViewModel 에 시계를 주입할 수 있어야 한다.

    /** "남은 시간: 마감" 처럼 접두어와 내용이 서로 어긋나지 않아야 한다. */
    @Test
    fun `마감 전이면 남은 시간에 접두어가 붙고 톤이 URGENT 다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(deadline = FUTURE_DEADLINE)

        val state = createViewModel().data()

        assertTrue(
            state.deadlineText.startsWith(DEADLINE_REMAINING_PREFIX),
            "접두어가 빠졌다: ${state.deadlineText}",
        )
        assertEquals(DeadlineTone.URGENT, state.deadlineTone)

        // 마감이 남아 티커가 계속 돈다. 여기서 스코프를 닫지 않으면 runTest 종료 정리가 1분 delay 를
        // 무한히 돌려 테스트가 끝나지 않는다.
        viewModelStore.clear()
    }

    /** 마감 후엔 사용자가 할 수 있는 게 없어 촉구색을 거둔다. */
    @Test
    fun `마감 후면 판정 대기 중이고 톤이 NEUTRAL 이다`() = runTest {
        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.deadlineText)
        assertEquals(DeadlineTone.NEUTRAL, state.deadlineTone)
    }

    /** 서버 deadline 을 그대로 찍으면 "1/2" 가 나온다 — challengeDate 기준이어야 하루 밀리지 않는다. */
    @Test
    fun `절대 마감 시각은 challengeDate 기준이라 하루 밀리지 않는다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(deadline = EXCLUSIVE_END_DEADLINE)

        assertEquals("1/1 24:00", createViewModel().data().deadlineAbsoluteText)
    }

    /** challengeDate 가 없으면 배타적 끝점에서 하루를 되돌려 역산한다. */
    @Test
    fun `challengeDate 가 없으면 절대 마감 시각을 deadline 에서 역산한다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            challengeDate = null,
            deadline = EXCLUSIVE_END_DEADLINE,
        )

        assertEquals("1/1 24:00", createViewModel().data().deadlineAbsoluteText)
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

    /** soul-oath 이전에 만들어진 챌린지. 계약서가 없는 건 오류가 아니라 정상 상태다. */
    @Test
    fun `계약서가 없으면 서명 묶음만 빠지고 계약 조건은 그대로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(contract = null)

        val state = createViewModel().data()

        assertNull(
            state.signatures,
            "없는 맹세를 '서명 대기 중' 으로 그리지 않도록 묶음 자체가 없어야 한다",
        )
        assertEquals(CHALLENGER_MISSION, state.myMission)
        assertEquals(OPPONENT_MISSION, state.opponentMission)
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
        assertEquals("커피 사기", state.betContent)
        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.deadlineText)
    }

    @Test
    fun `계약서가 있으면 서명 묶음이 채워진다`() = runTest {
        val signatures = createViewModel().signatures()

        assertEquals(challengerSignature(), signatures.mySignature)
        assertEquals(opponentSignature(), signatures.opponentSignature)
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

    /** 이미 인증한 사용자를 촬영까지 시킨 뒤 서버 700 으로 되돌리던 경로를 막는 게 이 게이트의 목적이다. */
    @Test
    fun `내가 이미 인증했으면 인증 진입점이 닫힌다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )

        assertFalse(createViewModel().data().canVerify)
    }

    @Test
    fun `내 인증이 PENDING 이면 인증 진입점이 열린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.PENDING,
        )

        assertTrue(createViewModel().data().canVerify)
    }

    @Test
    fun `IN_PROGRESS 가 아니면 내 인증 상태와 무관하게 진입점이 닫힌다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.PENDING,
        )
        assertFalse(createViewModel().data().canVerify)

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )
        assertFalse(createViewModel().data().canVerify)
    }

    /** 막는 실패보다 중복 시도 실패가 낫다 — 모를 땐 주 액션을 열어 둔다. */
    @Test
    fun `인증 현황 조회에 실패해도 Data 를 유지하고 진입점을 열어 둔다`() = runTest {
        fakeVerificationRepository.getVerificationsError = "네트워크 오류가 발생하였습니다."

        val state = createViewModel().data()

        assertNull(state.verifications)
        assertTrue(state.canVerify)
        assertEquals(CHALLENGER_MISSION, state.myMission, "계약서는 그대로 보여야 한다")
        assertEquals(OPPONENT_NICKNAME, state.opponentNickname)
    }

    @Test
    fun `관점 미상이면 인증 현황을 모른 채 진입점을 열어 둔다`() = runTest {
        fakeUserInfoRepository.error = "사용자 정보를 불러오지 못했습니다."
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )

        val state = createViewModel().data()

        assertNull(state.verifications, "어느 칸이 내 것인지 모르면 '나/상대' 로 세울 수 없다")
        assertTrue(state.canVerify)
    }

    @Test
    fun `내가 opponent 여도 내 인증 칸을 정확히 집는다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)

        fakeVerificationRepository.verifications = sampleVerifications(
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
        )
        assertFalse(createViewModel().data().canVerify)

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )
        assertTrue(
            createViewModel().data().canVerify,
            "상대가 인증했다고 내 진입점이 닫히면 안 된다",
        )
    }

    @Test
    fun `양측 인증 사진 URL 이 나와 상대 기준으로 실린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
        )

        val verifications = createViewModel().verifications()

        assertEquals(CHALLENGER_PHOTO_URL, verifications.myPhotoUrl)
        assertEquals(VerificationStatus.VERIFIED, verifications.myStatus)
        assertEquals(OPPONENT_PHOTO_URL, verifications.opponentPhotoUrl)
        assertEquals(VerificationStatus.VERIFIED, verifications.opponentStatus)
    }

    @Test
    fun `내가 opponent 면 사진 URL 도 함께 뒤집힌다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
        )

        val verifications = createViewModel().verifications()

        assertEquals(OPPONENT_PHOTO_URL, verifications.myPhotoUrl)
        assertEquals(CHALLENGER_PHOTO_URL, verifications.opponentPhotoUrl)
    }

    @Test
    fun `아직 아무도 인증하지 않았으면 두 칸이 모두 PENDING 으로 채워진다`() = runTest {
        val verifications = createViewModel().verifications()

        assertEquals(VerificationStatus.PENDING, verifications.myStatus)
        assertEquals(VerificationStatus.PENDING, verifications.opponentStatus)
        assertNull(verifications.myPhotoUrl)
        assertNull(verifications.opponentPhotoUrl)
    }
}
