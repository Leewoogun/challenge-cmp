package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModelStore
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ChallengeResult
import com.lwg.challenge.domain.model.challenge.ContractParty
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.PartyVerification
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailModalEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.DEADLINE_AWAITING_JUDGEMENT_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.DEADLINE_REMAINING_PREFIX
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_ME_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_OTHER_SUFFIX
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_UNKNOWN_ACTOR_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.HeadlineTone
import com.lwg.challenge.feature.challenge.detail.contract.MY_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.MY_MISSION_LABEL
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

/** 마감 전 사진을 교체했을 때 서버가 갱신해 주는 값. 사진 URL 은 그대로인 채 이 값만 움직인다. */
private val REPLACED_VERIFIED_AT = LocalDateTime.parse("2026-01-01T11:05:00")

private const val CHALLENGER_PHOTO_URL = "https://api.example.com/verifications/1/photo"
private const val OPPONENT_PHOTO_URL = "https://api.example.com/verifications/2/photo"

/**
 * 두 값을 가른 이유: 사유가 내 칸과 상대 칸에서 각각 나오는지 관점이 뒤집혀도 구분되어야 한다.
 *
 * 둘 다 **반려** 사유다 — 사유가 실린 칸은 곧 미인증 처리된 칸이라(계약 보장) 통과 문구가 이 자리에
 * 올 일이 없다. 통과처럼 읽히는 픽스처를 두면 계약이 배제한 조합을 테스트가 정상으로 가르친다.
 */
private const val AI_REASON = "책상 위 커피잔 사진으로 미션 수행 증거로 보기 어렵습니다."
private const val OPPONENT_AI_REASON = "사진에서 미션 수행 장면을 확인할 수 없습니다."

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
        result: ChallengeResult? = null,
        forfeitedBy: Long? = null,
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
        result = result,
        forfeitedBy = forfeitedBy,
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

    /**
     * 판정이 끝난 챌린지. 서버 불변식상 `status == "COMPLETED"` 와 `result != null` 은 함께 오므로
     * 결과 픽스처를 만들 때 둘을 따로 세우지 않는다.
     */
    private fun completedDetail(result: ChallengeResult) =
        sampleDetail(status = "COMPLETED", result = result)

    /** 서버 응답 그대로 **역할 기준**이다. '나 / 상대' 로 세우는 건 ViewModel 의 몫이다. */
    private fun sampleVerifications(
        challengerStatus: VerificationStatus = VerificationStatus.PENDING,
        challengerPhotoUrl: String? = null,
        challengerPhotoDeleted: Boolean = false,
        challengerVerifiedAt: LocalDateTime? = VERIFIED_AT,
        challengerAiReason: String? = null,
        opponentStatus: VerificationStatus = VerificationStatus.PENDING,
        opponentPhotoUrl: String? = null,
        opponentPhotoDeleted: Boolean = false,
        opponentVerifiedAt: LocalDateTime? = VERIFIED_AT,
        opponentAiReason: String? = null,
    ) = ChallengeVerifications(
        challengeId = CHALLENGE_ID,
        challenger = party(
            userId = CHALLENGER_ID,
            status = challengerStatus,
            photoUrl = challengerPhotoUrl,
            photoDeleted = challengerPhotoDeleted,
            verifiedAt = challengerVerifiedAt,
            aiReason = challengerAiReason,
        ),
        opponent = party(
            userId = OPPONENT_ID,
            status = opponentStatus,
            photoUrl = opponentPhotoUrl,
            photoDeleted = opponentPhotoDeleted,
            verifiedAt = opponentVerifiedAt,
            aiReason = opponentAiReason,
        ),
    )

    /**
     * 확정 시각은 **사진을 낸 적이 있을 때만** 있다는 서버 불변식을 픽스처에서도 깨지 않는다.
     *
     * `VERIFIED` 는 물론이고 AI 가 반려한 `FAILED`(사진이 남아 있거나 탈퇴로 지워진 칸)도 제출은
     * 이미 끝난 뒤라 시각이 남는다. 시각이 없는 `FAILED` 는 마감까지 미제출한 경우뿐이다.
     */
    private fun party(
        userId: Long,
        status: VerificationStatus,
        photoUrl: String?,
        photoDeleted: Boolean = false,
        verifiedAt: LocalDateTime? = VERIFIED_AT,
        aiReason: String? = null,
    ) = PartyVerification(
        userId = userId,
        status = status,
        photoUrl = photoUrl,
        verifiedAt = verifiedAt.takeIf {
            status == VerificationStatus.VERIFIED || photoUrl != null || photoDeleted
        },
        photoDeleted = photoDeleted,
        aiReason = aiReason,
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
    fun `headlineText 는 매핑 시점 시계 기준으로 만들어진다`() = runTest {
        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
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
            state.headlineText.startsWith(DEADLINE_REMAINING_PREFIX),
            "접두어가 빠졌다: ${state.headlineText}",
        )
        assertEquals(HeadlineTone.URGENT, state.headlineTone)

        // 마감이 남아 티커가 계속 돈다. 여기서 스코프를 닫지 않으면 runTest 종료 정리가 1분 delay 를
        // 무한히 돌려 테스트가 끝나지 않는다.
        viewModelStore.clear()
    }

    /** 마감 후엔 사용자가 할 수 있는 게 없어 촉구색을 거둔다. */
    @Test
    fun `마감 후면 판정 대기 중이고 톤이 NEUTRAL 이다`() = runTest {
        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
        assertEquals(HeadlineTone.NEUTRAL, state.headlineTone)
    }

    @Test
    fun `내가 challenger 이고 CHALLENGER_WIN 이면 승리로 나온다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(CHALLENGER_ID)
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.CHALLENGER_WIN)

        val state = createViewModel().data()

        assertEquals(RESULT_WIN_TEXT, state.headlineText)
        assertEquals(HeadlineTone.WIN, state.headlineTone)
    }

    /** 같은 CHALLENGER_WIN 이 관점만 바뀌면 정반대 사실이 된다. 한 방향만 덮으면 뒤집기가 증명되지 않는다. */
    @Test
    fun `내가 opponent 이고 CHALLENGER_WIN 이면 패배로 뒤집힌다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.CHALLENGER_WIN)

        val state = createViewModel().data()

        assertEquals(RESULT_LOSE_TEXT, state.headlineText)
        assertEquals(HeadlineTone.LOSE, state.headlineTone)
    }

    @Test
    fun `내가 opponent 이고 OPPONENT_WIN 이면 승리로 나온다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.OPPONENT_WIN)

        val state = createViewModel().data()

        assertEquals(RESULT_WIN_TEXT, state.headlineText)
        assertEquals(HeadlineTone.WIN, state.headlineTone)
    }

    @Test
    fun `내가 challenger 이고 OPPONENT_WIN 이면 패배로 뒤집힌다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(CHALLENGER_ID)
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.OPPONENT_WIN)

        val state = createViewModel().data()

        assertEquals(RESULT_LOSE_TEXT, state.headlineText)
        assertEquals(HeadlineTone.LOSE, state.headlineTone)
    }

    @Test
    fun `DRAW 는 무승부로 나온다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.DRAW)

        val state = createViewModel().data()

        assertEquals(RESULT_DRAW_TEXT, state.headlineText)
        assertEquals(HeadlineTone.DRAW, state.headlineTone)
    }

    /** 둘 다 못 한 것과 내가 진 것은 다른 사실이라 문구를 가르고, 톤만 패배와 같이 간다. */
    @Test
    fun `BOTH_LOSE 는 양측 패배로 나오고 톤이 LOSE 다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.BOTH_LOSE)

        val state = createViewModel().data()

        assertEquals(RESULT_BOTH_LOSE_TEXT, state.headlineText)
        assertEquals(HeadlineTone.LOSE, state.headlineTone)
    }

    /**
     * 누가 이겼는지는 알아도 그게 보는 사람에게 좋은 소식인지는 모른다 — 승리색을 칠하면 진 사람에게
     * 축하색을 보여주게 되므로 톤을 중립으로 낮춘다.
     */
    @Test
    fun `관점 미상이면 결과를 실명으로만 말하고 톤을 중립으로 낮춘다`() = runTest {
        fakeUserInfoRepository.error = "사용자 정보를 불러오지 못했습니다."
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.CHALLENGER_WIN)

        val state = createViewModel().data()

        assertEquals("$CHALLENGER_NICKNAME $RESULT_WIN_TEXT", state.headlineText)
        assertEquals(HeadlineTone.DECIDED_NEUTRAL, state.headlineTone)
    }

    /**
     * 두 장면 다 색을 기울이지 않는다는 이유로 한 톤으로 합치면, 화면이 톤으로 아이콘까지 고르는 탓에
     * 결과가 이미 난 "우건 승리" 옆에 판정을 기다리는 시계가 다시 붙는다.
     */
    @Test
    fun `관점 미상 결과와 판정 대기 중은 서로 다른 톤이다`() = runTest {
        fakeUserInfoRepository.error = "사용자 정보를 불러오지 못했습니다."
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.CHALLENGER_WIN)
        val resultTone = createViewModel().data().headlineTone

        fakeUserInfoRepository.error = null
        fakeChallengeRepository.detail = sampleDetail()
        val awaitingTone = createViewModel().data().headlineTone

        assertEquals(HeadlineTone.DECIDED_NEUTRAL, resultTone)
        assertEquals(HeadlineTone.NEUTRAL, awaitingTone)
        assertNotEquals(awaitingTone, resultTone)
    }

    /** 서버 불변식(COMPLETED ⟺ result != null)이 깨져도 상태 문자열에서 결과를 지어내지 않는다. */
    @Test
    fun `COMPLETED 인데 result 가 null 이면 판정 대기 중으로 남는다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED", result = null)

        val state = createViewModel().data()

        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
        assertEquals(HeadlineTone.NEUTRAL, state.headlineTone)
    }

    /** 판정이 실리면 시계보다 결과가 먼저다 — 마감이 남아 있어도 남은 시간으로 되돌아가지 않는다. */
    @Test
    fun `마감 전이라도 결과가 있으면 결과 표기가 이긴다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            result = ChallengeResult.CHALLENGER_WIN,
            deadline = FUTURE_DEADLINE,
        )

        val state = createViewModel().data()

        assertEquals(RESULT_WIN_TEXT, state.headlineText)
        assertEquals(HeadlineTone.WIN, state.headlineTone)

        // 마감이 남아 티커가 계속 돈다. 여기서 스코프를 닫지 않으면 runTest 종료 정리가 1분 delay 를
        // 무한히 돌려 테스트가 끝나지 않는다.
        viewModelStore.clear()
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
        assertEquals(DEADLINE_AWAITING_JUDGEMENT_TEXT, state.headlineText)
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

    /**
     * 재제출이 사진 교체로 허용되면서 `VERIFIED` 가 진입점을 닫을 근거가 사라졌다 — 흔들리게 찍은
     * 사용자가 마감 전까지 다시 찍을 수 있어야 한다. 인증 완료 표시는 그대로 두고 그 아래에 붙인다.
     */
    @Test
    fun `내가 이미 인증했어도 재촬영 진입점이 열린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )

        val state = createViewModel().data()

        assertTrue(state.canVerify)
        assertEquals(MyMissionSlot.MY_PHOTO_WITH_RETAKE, state.myMissionSlot)
    }

    /** 바로 위 테스트와 짝 — 내 인증 상태가 무엇이든 진입점은 챌린지 상태만 따른다. */
    @Test
    fun `내 인증이 PENDING 이면 인증 진입점이 열린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.PENDING,
        )

        assertTrue(createViewModel().data().canVerify)
    }

    /** 마감 뒤 교체는 서버가 여전히 거부한다 — 앱도 그 자리에 진입점을 두지 않는다. */
    @Test
    fun `IN_PROGRESS 가 아니면 내 인증 상태와 무관하게 진입점이 닫힌다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.PENDING,
        )
        val pendingState = createViewModel().data()
        assertFalse(pendingState.canVerify)
        assertEquals(MyMissionSlot.NONE, pendingState.myMissionSlot)

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )
        val verifiedState = createViewModel().data()
        assertFalse(verifiedState.canVerify)
        assertEquals(
            MyMissionSlot.MY_PHOTO,
            verifiedState.myMissionSlot,
            "마감 뒤에는 인증 완료 표시만 남고 재촬영은 붙지 않는다",
        )
    }

    /**
     * 슬롯 4개 값이 각각 어떤 입력에서 나오는지 한자리에 고정한다.
     *
     * [MyMissionSlot.MY_PHOTO] 와 [MyMissionSlot.MY_PHOTO_WITH_RETAKE] 를 가르는 것은 내 인증
     * 상태가 아니라 챌린지 상태다 — 둘 다 `VERIFIED` 라 한쪽 입력만 두면 아무것도 증명하지 못한다.
     */
    @Test
    fun `내 미션 슬롯이 챌린지 상태와 내 인증 상태의 조합으로 갈린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.PENDING,
        )
        assertEquals(MyMissionSlot.VERIFY_CTA, createViewModel().data().myMissionSlot)

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )
        assertEquals(MyMissionSlot.MY_PHOTO_WITH_RETAKE, createViewModel().data().myMissionSlot)

        // 사진이 없는 FAILED — 마감까지 미제출한 경우다. 진입점이 열려 있는데도 보여줄 것이 없는
        // 조합이라 canVerify 보다 먼저 걸러진다. 사진이 있는 FAILED(AI 반려)는 아래 별도 테스트다.
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.FAILED,
        )
        assertEquals(MyMissionSlot.NONE, createViewModel().data().myMissionSlot)

        // 위 MY_PHOTO_WITH_RETAKE 와 내 인증 상태가 같다 — 챌린지 상태 한 줄만 바뀐 것이 구분선이다.
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )
        assertEquals(MyMissionSlot.MY_PHOTO, createViewModel().data().myMissionSlot)
    }

    /**
     * AI 가 반려한 인증은 `FAILED` 인데 내가 올린 사진이 그대로 남는다 — 상태만 보고 슬롯을 비우면
     * 사용자가 실제로 낸 사진이 화면에서 사라진다.
     *
     * [MyMissionSlot.MY_PHOTO_WITH_RETAKE] 가 아닌 이유는 반려가 자정 판정(=마감)에서 확정되기
     * 때문이다 — 그 시점에 챌린지는 이미 `COMPLETED` 라 다시 찍을 창이 없다. 아래 단언이 그
     * 실제 조합(COMPLETED + FAILED + 사진)을 그대로 세운다.
     */
    @Test
    fun `AI 가 반려한 인증은 사진이 남아 내 카드에 그대로 그려진다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.FAILED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerAiReason = AI_REASON,
        )

        val state = createViewModel().data()

        assertEquals(VerificationStatus.FAILED, state.myVerificationStatus)
        assertEquals(MyMissionSlot.MY_PHOTO, state.myMissionSlot)
        assertEquals(CHALLENGER_PHOTO_URL, state.myPhotoUrl, "반려돼도 낸 사진은 남는다")
        assertFalse(state.canVerify, "판정은 마감 시점에 내려지므로 재촬영 창이 없다")
    }

    /** 같은 `FAILED` 라도 사진이 없으면 미제출로 마감한 것이라 슬롯이 비어야 한다. */
    @Test
    fun `사진 없는 FAILED 는 미제출이라 슬롯이 비어 있다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.FAILED,
        )

        val state = createViewModel().data()

        assertEquals(MyMissionSlot.NONE, state.myMissionSlot)
        assertNull(state.myPhotoUrl)
        assertNull(state.verifications?.myVerifiedAt, "낸 적이 없으니 확정 시각도 없다")
    }

    /**
     * 양측이 모두 반려된 판정. 사유가 실린 칸은 **그 인증이 AI 판별로 미인증 처리된 칸**이라
     * (계약 보장) 둘 다 `FAILED` + 사진이다. 통과한 칸에 사유가 실린 픽스처를 세우지 않는 이유가
     * 이것이다 — 계약이 배제한 조합으로 단언을 세우면 앱에 판정 사본을 들이는 근거가 된다.
     */
    @Test
    fun `AI 판정 사유가 양측 모두 State 까지 도달한다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.FAILED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerAiReason = AI_REASON,
            opponentStatus = VerificationStatus.FAILED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
            opponentAiReason = OPPONENT_AI_REASON,
        )

        val state = createViewModel().data()

        assertEquals(AI_REASON, state.myAiReason)
        assertEquals(OPPONENT_AI_REASON, state.opponentAiReason)
    }

    /**
     * 통과한 인증에는 사유가 실려 오지 않는다 — 서버가 통과·검증 불가·호출 실패 건의 사유를 컬럼에만
     * 두고 wire 에는 싣지 않기 때문이다. 빈 문자열로 뭉개지도 않는다.
     */
    @Test
    fun `AI 판정 사유가 없으면 State 에서도 null 이다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )

        val state = createViewModel().data()

        assertNull(state.myAiReason)
        assertNull(state.opponentAiReason)
        assertNull(state.myAiReasonText, "그릴 사유가 없으면 접두어만 남은 문구를 만들지 않는다")
        assertNull(state.opponentAiReasonText)
    }

    /**
     * 화면에 그릴 문구는 State 가 접두어까지 붙여 확정한다 — 내 카드와 상대 카드가 각자 조립하면
     * 한쪽만 고쳐져 같은 사실이 두 문장으로 갈린다. 렌더링을 덮는 UI 테스트가 없어, 조립이 어디서
     * 끝나는지를 잠그는 단언이 여기뿐이다.
     */
    @Test
    fun `AI 판정 사유는 접두어가 붙은 표시 문구로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.FAILED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerAiReason = AI_REASON,
            opponentStatus = VerificationStatus.FAILED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
            opponentAiReason = OPPONENT_AI_REASON,
        )

        val state = createViewModel().data()

        assertEquals("$VERIFICATION_REJECTED_PREFIX$AI_REASON", state.myAiReasonText)
        assertEquals("$VERIFICATION_REJECTED_PREFIX$OPPONENT_AI_REASON", state.opponentAiReasonText)
    }

    /** 관점이 뒤집히면 사유도 함께 뒤집힌다 — 사진·상태와 같은 칸에서 나온 값이다. */
    @Test
    fun `내가 opponent 여도 AI 판정 사유가 내 칸에서 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(status = "COMPLETED")
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.FAILED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerAiReason = OPPONENT_AI_REASON,
            opponentStatus = VerificationStatus.FAILED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
            opponentAiReason = AI_REASON,
        )

        val state = createViewModel().data()

        assertEquals(AI_REASON, state.myAiReason)
        assertEquals(OPPONENT_AI_REASON, state.opponentAiReason)
        assertEquals(MyMissionSlot.MY_PHOTO, state.myMissionSlot, "반려된 내 사진은 opponent 칸에서도 남는다")
    }

    /** 진입점은 챌린지 상태만 본다 — 현황을 못 받았다고 주 액션이 사라지지 않는다. */
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

    /** [ChallengeDetailUiState.Data.canVerify] 는 양쪽 다 열려 있어 판정에 쓸 수 없다 — 슬롯으로 본다. */
    @Test
    fun `내가 opponent 여도 내 인증 칸을 정확히 집는다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)

        fakeVerificationRepository.verifications = sampleVerifications(
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
        )
        val mineVerified = createViewModel().data()
        assertEquals(VerificationStatus.VERIFIED, mineVerified.myVerificationStatus)
        assertEquals(MyMissionSlot.MY_PHOTO_WITH_RETAKE, mineVerified.myMissionSlot)

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )
        val opponentVerified = createViewModel().data()
        assertEquals(
            VerificationStatus.PENDING,
            opponentVerified.myVerificationStatus,
            "상대가 인증했다고 내 칸이 인증 완료로 바뀌면 안 된다",
        )
        assertEquals(MyMissionSlot.VERIFY_CTA, opponentVerified.myMissionSlot)
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

    // 아래 테스트들이 "탈퇴로 지워짐" 과 "URL 만 없음" 의 구분선이다 — 둘 다 VERIFIED + photoUrl null 이라
    // photoDeleted 를 잃는 순간 화면에서 데이터 이상이 탈퇴 문구 뒤로 숨는다.

    @Test
    fun `탈퇴한 상대의 사진 삭제 표시가 상대 칸으로 실린다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
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
    fun `내가 opponent 면 사진 삭제 표시도 함께 뒤집힌다`() = runTest {
        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = null,
            challengerPhotoDeleted = true,
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
        )

        val verifications = createViewModel().verifications()

        assertTrue(verifications.opponentPhotoDeleted)
        assertFalse(verifications.myPhotoDeleted)
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

    // 아래 테스트들이 사진 캐시 키의 전부다 — 바뀌면 갈라지고, 안 바뀌면 그대로여야 한다.
    // 앞을 잃으면 교체 후에도 옛 사진이 남고, 뒤를 잃으면 멀쩡한 사진을 매번 다시 받는다.
    // 둘 다 예외도 로그도 남기지 않아 테스트 말고는 잡을 자리가 없다.

    @Test
    fun `사진을 교체하면 URL 이 같아도 캐시 키가 달라진다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerVerifiedAt = VERIFIED_AT,
        )
        val viewModel = createViewModel()
        val before = viewModel.data().myPhotoCacheKey

        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerVerifiedAt = REPLACED_VERIFIED_AT,
        )
        viewModel.retry()
        val replaced = viewModel.data()

        assertEquals(CHALLENGER_PHOTO_URL, replaced.myPhotoUrl, "교체돼도 사진 경로는 그대로다")
        assertNotEquals(before, replaced.myPhotoCacheKey, "경로가 같아도 키는 갈려야 한다")
    }

    @Test
    fun `인증 시각이 그대로면 캐시 키도 그대로다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )
        val viewModel = createViewModel()
        val before = viewModel.data().myPhotoCacheKey

        viewModel.retry()

        assertEquals(before, viewModel.data().myPhotoCacheKey, "안 바뀐 사진을 다시 받게 만들면 안 된다")
    }

    /** 인증 전에는 확정 시각이 없다 — 갈라놓을 대상이 없어 Coil 기본과 같은 URL 단독 키로 떨어진다. */
    @Test
    fun `인증 시각이 없으면 캐시 키가 URL 로만 떨어진다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerVerifiedAt = null,
        )

        val state = createViewModel().data()

        assertEquals(CHALLENGER_PHOTO_URL, state.myPhotoCacheKey)
    }

    @Test
    fun `사진 URL 이 없으면 캐시 키도 null 이다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = null,
            challengerPhotoDeleted = true,
        )

        val state = createViewModel().data()

        assertNull(state.myPhotoCacheKey, "탈퇴로 지워진 사진에는 캐시에서 집을 대상이 없다")
        assertNull(state.opponentPhotoCacheKey)
    }

    @Test
    fun `나와 상대의 캐시 키가 서로 섞이지 않는다`() = runTest {
        fakeVerificationRepository.verifications = sampleVerifications(
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
            challengerVerifiedAt = VERIFIED_AT,
            opponentStatus = VerificationStatus.VERIFIED,
            opponentPhotoUrl = OPPONENT_PHOTO_URL,
            opponentVerifiedAt = REPLACED_VERIFIED_AT,
        )

        val mine = createViewModel().data()

        assertEquals("$CHALLENGER_PHOTO_URL|$VERIFIED_AT", mine.myPhotoCacheKey)
        assertEquals("$OPPONENT_PHOTO_URL|$REPLACED_VERIFIED_AT", mine.opponentPhotoCacheKey)

        fakeUserInfoRepository.userInfo = userInfo(OPPONENT_ID)
        val flipped = createViewModel().data()

        assertEquals(mine.opponentPhotoCacheKey, flipped.myPhotoCacheKey, "관점이 뒤집히면 키도 뒤집힌다")
        assertEquals(mine.myPhotoCacheKey, flipped.opponentPhotoCacheKey)
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
            challengerStatus = VerificationStatus.VERIFIED,
            challengerPhotoUrl = CHALLENGER_PHOTO_URL,
        )

        val state = createViewModel().data()

        assertFalse(state.canForfeit)
        assertEquals(MyMissionSlot.MY_PHOTO_WITH_RETAKE, state.myMissionSlot)
    }

    /**
     * 🔑 **회귀 테스트가 아니다.** 배타성은 `canForfeit` 가 [MyMissionSlot] 의 한 가지로 정의돼 있어
     * 이미 **표현 불가능**하다 — 둘이 동시에 참인 상태를 만들 방법이 타입 차원에서 없다. 그래서 이건
     * 언젠가 깨질 조합을 감시하는 테스트가 아니라, **그 파생이 정말 그 뜻인지**를 상태 공간 전체에서
     * 한 번 확인하는 테스트다.
     *
     * ⚠️ 이 구분을 놓치면 다음 사람이 배타성을 이 테스트가 지켜 준다고 오해한다. `canForfeit` 를
     * 독립 조건식으로 바꾸는 순간 이 단언은 **그대로 통과하면서 보증만 사라진다** — 조건식 두 벌은
     * 여기 적힌 조합에서 우연히 어긋나지 않을 수 있기 때문이다. 지켜야 할 것은 이 단언이 아니라
     * 파생이 [MyMissionSlot] 한 가지로 남아 있다는 사실이다.
     */
    @Test
    fun `기권 진입점과 다시 찍기는 어떤 상태에서도 동시에 보이지 않는다`() = runTest {
        val myStatuses = listOf(
            VerificationStatus.PENDING,
            VerificationStatus.VERIFIED,
            VerificationStatus.FAILED,
            // 현황 없음 — 조회 실패·관점 미상.
            null,
        )

        for (challengeStatus in listOf("IN_PROGRESS", "COMPLETED")) {
            for (myStatus in myStatuses) {
                fakeChallengeRepository.detail = sampleDetail(status = challengeStatus)
                fakeVerificationRepository.getVerificationsError =
                    if (myStatus == null) "네트워크 오류가 발생하였습니다." else null
                fakeVerificationRepository.verifications = sampleVerifications(
                    challengerStatus = myStatus ?: VerificationStatus.PENDING,
                    challengerPhotoUrl = CHALLENGER_PHOTO_URL
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

    /**
     * 의도를 고정하는 테스트다. 인증 현황을 모르면 인증 CTA 가 열리는데, 기권 진입점도 **같은 근거**로
     * 함께 열린다 — 실제 자격은 서버가 판정한다. 여기에만 가드를 더하면 두 진입점이 서로 다른 근거로
     * 갈린다.
     */
    @Test
    fun `인증 현황을 몰라도 기권 진입점이 인증 CTA 와 함께 열린다`() = runTest {
        fakeVerificationRepository.getVerificationsError = "네트워크 오류가 발생하였습니다."

        val state = createViewModel().data()

        assertNull(state.verifications)
        assertTrue(state.canVerify)
        assertTrue(state.canForfeit)
    }

    /** 확인 다이얼로그 없이는 기권이 발생하지 않는다는 수용 기준을 고정한다. */
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

    /** 기권은 멱등이 아니다 — 두 번 나가면 두 번째는 서버가 거부하고 사용자는 이유 없는 실패를 본다. */
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

    /**
     * 서버가 거부하는 사유 대부분은 **내 화면이 낡았다**는 뜻이다. 다시 받지 않으면 사라졌어야 할
     * 기권 진입점이 그대로 남아 사용자가 같은 실패를 반복한다.
     */
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

    /** 전체 화면 로딩으로 덮지 않는다 — 잠기는 것은 진입점뿐이고 읽던 계약서는 그대로 남는다. */
    @Test
    fun `기권 요청이 나가 있는 동안 진입점이 잠긴다`() = runTest {
        fakeChallengeRepository.forfeitDelayMillis = 1_000
        val viewModel = createViewModel()

        viewModel.forfeit()

        val forfeiting = viewModel.data()
        assertTrue(forfeiting.isForfeiting)
        assertEquals(CHALLENGER_MISSION, forfeiting.myMission, "계약서는 그대로 보여야 한다")

        // 되돌리는 코드가 따로 없다 — 재조회가 상태를 다시 세우면서 false 로 확정한다.
        advanceUntilIdle()
        assertFalse(viewModel.data().isForfeiting)
    }

    @Test
    fun `내가 기권했으면 안내가 내 시점으로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            result = ChallengeResult.OPPONENT_WIN,
            forfeitedBy = CHALLENGER_ID,
        )

        assertEquals(FORFEIT_BY_ME_TEXT, createViewModel().data().forfeitNoticeText)
    }

    @Test
    fun `상대가 기권했으면 안내가 상대 실명으로 나온다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            result = ChallengeResult.CHALLENGER_WIN,
            forfeitedBy = OPPONENT_ID,
        )

        assertEquals(
            "$OPPONENT_NICKNAME$FORFEIT_BY_OTHER_SUFFIX",
            createViewModel().data().forfeitNoticeText,
        )
    }

    @Test
    fun `기권이 아니면 안내가 붙지 않는다`() = runTest {
        fakeChallengeRepository.detail = completedDetail(ChallengeResult.CHALLENGER_WIN)

        assertNull(createViewModel().data().forfeitNoticeText)
    }

    /** 라벨·헤드라인이 관점을 모를 때 "나" 를 버리고 실명으로 낮추는 것과 같은 판단이다. */
    @Test
    fun `관점을 모르면 기권자를 실명으로 말한다`() = runTest {
        fakeUserInfoRepository.error = "사용자 정보를 불러오지 못했습니다."
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            result = ChallengeResult.OPPONENT_WIN,
            forfeitedBy = CHALLENGER_ID,
        )

        assertEquals(
            "$CHALLENGER_NICKNAME$FORFEIT_BY_OTHER_SUFFIX",
            createViewModel().data().forfeitNoticeText,
        )
    }

    /** 양측 어느 userId 와도 맞지 않으면 닉네임을 지어낼 수 없어 아는 사실만 말한다. */
    @Test
    fun `기권자가 양측 어느 쪽도 아니면 이름 없이 기권 사실만 말한다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(
            status = "COMPLETED",
            result = ChallengeResult.OPPONENT_WIN,
            forfeitedBy = 999L,
        )

        assertEquals(FORFEIT_UNKNOWN_ACTOR_TEXT, createViewModel().data().forfeitNoticeText)
    }
}
