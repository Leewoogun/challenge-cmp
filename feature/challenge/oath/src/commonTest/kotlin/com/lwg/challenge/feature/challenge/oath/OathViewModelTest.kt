package com.lwg.challenge.feature.challenge.oath

import app.cash.turbine.test
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractParty
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.oath.contract.OathModalEffect
import com.lwg.challenge.feature.challenge.oath.contract.OathUiEffect
import com.lwg.challenge.feature.challenge.oath.contract.OathUiState
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val CHALLENGE_ID = 42L

@OptIn(ExperimentalCoroutinesApi::class)
class OathViewModelTest {

    private lateinit var fakeChallengeRepository: FakeChallengeRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeChallengeRepository = FakeChallengeRepository()
        fakeChallengeRepository.detail = sampleDetail()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): OathViewModel =
        OathViewModel(
            challengeId = CHALLENGE_ID,
            challengeRepository = fakeChallengeRepository,
        )

    private fun OathViewModel.data(): OathUiState.Data =
        assertIs<OathUiState.Data>(uiState.value)

    private fun sampleSignature() = Signature(
        strokes = listOf(
            SignatureStroke(listOf(SignaturePoint(10, 20), SignaturePoint(30, 40))),
        ),
    )

    private fun sampleDetail(
        challengerSignature: Signature? = Signature(
            strokes = listOf(SignatureStroke(listOf(SignaturePoint(1, 2), SignaturePoint(3, 4)))),
        ),
    ) = ChallengeDetail(
        challengeId = CHALLENGE_ID,
        status = "PENDING",
        challengeDate = LocalDate.parse("2026-01-01"),
        // 상대 시간 문자열은 실행 시점 시계에 의존한다. 이미 지난 마감으로 고정해 결과를 결정론적으로 만든다.
        deadline = LocalDateTime.parse("2026-01-01T00:00:00"),
        betContent = "커피 사기",
        challenger = ContractParty(
            userId = 1L,
            nickname = "우건",
            profileImageUrl = null,
            mission = "오늘 운동 1시간 하기",
        ),
        opponent = ContractParty(
            userId = 2L,
            nickname = "나",
            profileImageUrl = null,
            mission = "",
        ),
        contract = Contract(
            content = "영혼의 맹세",
            isFinalized = false,
            challengerSignature = challengerSignature,
            challengerSignedAt = challengerSignature?.let { LocalDateTime.parse("2026-01-01T00:00:00") },
            opponentSignature = null,
            opponentSignedAt = null,
        ),
    )

    @Test
    fun `init 시 상세를 조회하고 계약서 필드를 그대로 매핑한다`() = runTest {
        val viewModel = createViewModel()

        assertEquals(1, fakeChallengeRepository.detailCallCount)
        val state = viewModel.data()
        assertEquals(CHALLENGE_ID, state.challengeId)
        assertEquals("우건", state.challengerNickname)
        assertEquals("오늘 운동 1시간 하기", state.challengerMission)
        assertEquals("커피 사기", state.betContent)
        assertEquals("마감", state.deadlineText)
        assertEquals("우건의 미션", state.challengerMissionLabel)
        assertEquals("우건의 서명", state.challengerSignatureLabel)
        assertEquals(sampleDetail().contract.challengerSignature, state.challengerSignature)
        assertEquals("", state.myMission)
        assertTrue(state.signature.isEmpty)
        assertFalse(state.isSubmitting)
        assertEquals(OathModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `상세 조회에 실패하면 Error 로 전이한다`() = runTest {
        fakeChallengeRepository.detailError = "네트워크 오류가 발생하였습니다."

        val viewModel = createViewModel()

        val state = assertIs<OathUiState.Error>(viewModel.uiState.value)
        assertEquals("네트워크 오류가 발생하였습니다.", state.message)
    }

    @Test
    fun `retry 는 상세를 재조회한다`() = runTest {
        fakeChallengeRepository.detailError = "네트워크 오류가 발생하였습니다."
        val viewModel = createViewModel()
        assertIs<OathUiState.Error>(viewModel.uiState.value)

        fakeChallengeRepository.detailError = null
        viewModel.retry()

        assertEquals(2, fakeChallengeRepository.detailCallCount)
        assertEquals("우건", viewModel.data().challengerNickname)
    }

    @Test
    fun `Data 상태에서 재조회가 실패하면 Error 로 덮지 않고 메시지만 낸다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")
        fakeChallengeRepository.detailError = "네트워크 오류가 발생하였습니다."

        viewModel.uiEffect.test {
            viewModel.retry()

            val effect = assertIs<OathUiEffect.ShowMessage>(awaitItem())
            assertEquals("네트워크 오류가 발생하였습니다.", effect.message)
            expectNoEvents()
        }

        val state = viewModel.data()
        assertEquals("책 30페이지 읽기", state.myMission)
    }

    @Test
    fun `재조회에 성공해도 사용자가 입력한 미션과 서명은 보존된다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")
        viewModel.updateSignature(sampleSignature())

        viewModel.retry()

        val state = viewModel.data()
        assertEquals("책 30페이지 읽기", state.myMission)
        assertEquals(sampleSignature(), state.signature)
    }

    @Test
    fun `미션만 있으면 제출할 수 없고 서명까지 있어야 제출할 수 있다`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateMyMission("책 30페이지 읽기")
        assertFalse(viewModel.data().canSubmit)

        viewModel.updateSignature(sampleSignature())
        assertTrue(viewModel.data().canSubmit)
    }

    @Test
    fun `submit 은 trim 된 미션과 서명으로 수락을 요청한다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMyMission("  책 30페이지 읽기  ")
        viewModel.updateSignature(sampleSignature())

        viewModel.submit()

        assertEquals(1, fakeChallengeRepository.acceptCalls.size)
        val call = fakeChallengeRepository.acceptCalls.first()
        assertEquals(CHALLENGE_ID, call.challengeId)
        assertEquals("책 30페이지 읽기", call.myMission)
        assertEquals(sampleSignature(), call.signature)
    }

    @Test
    fun `submit 을 연속 호출해도 수락 요청은 1회만 나간다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")
        viewModel.updateSignature(sampleSignature())

        viewModel.submit()
        viewModel.submit()

        assertEquals(1, fakeChallengeRepository.acceptCalls.size)
    }

    @Test
    fun `제출에 실패하면 화면을 닫지 않고 미션과 서명을 보존한다`() = runTest {
        fakeChallengeRepository.acceptError = "이미 응답한 챌린지입니다."
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")
        viewModel.updateSignature(sampleSignature())

        viewModel.uiEffect.test {
            viewModel.submit()

            val effect = assertIs<OathUiEffect.ShowMessage>(awaitItem())
            assertEquals("이미 응답한 챌린지입니다.", effect.message)
            expectNoEvents()
        }

        val state = viewModel.data()
        assertFalse(state.isSubmitting)
        assertEquals("책 30페이지 읽기", state.myMission)
        assertEquals(sampleSignature(), state.signature)
    }

    @Test
    fun `제출에 성공하면 완료 메시지를 낸 뒤 화면을 닫는다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")
        viewModel.updateSignature(sampleSignature())

        viewModel.uiEffect.test {
            viewModel.submit()

            val effect = assertIs<OathUiEffect.ShowMessage>(awaitItem())
            assertEquals("맹세가 완료됐어요 🔥", effect.message)
            assertEquals(OathUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `아무것도 쓰지 않았으면 requestExit 은 확인 없이 바로 화면을 닫는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.requestExit()

            assertEquals(OathUiEffect.NavigateBack, awaitItem())
        }

        assertEquals(OathModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `미션만 입력한 상태의 requestExit 은 확인을 띄우고 화면을 닫지 않는다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")

        viewModel.uiEffect.test {
            viewModel.requestExit()

            expectNoEvents()
        }

        assertEquals(OathModalEffect.ExitConfirm, viewModel.modalEffect.value)
    }

    @Test
    fun `서명만 그린 상태의 requestExit 도 확인을 띄우고 화면을 닫지 않는다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateSignature(sampleSignature())

        viewModel.uiEffect.test {
            viewModel.requestExit()

            expectNoEvents()
        }

        assertEquals(OathModalEffect.ExitConfirm, viewModel.modalEffect.value)
    }

    // Data 가 아닌 상태에서도 뒤로가기는 막히지 않아야 한다. 계약서를 못 불러온 화면에 갇히면
    // 사용자가 앱을 종료하는 것 말고는 빠져나갈 방법이 없다.
    @Test
    fun `Error 상태의 requestExit 은 확인 없이 바로 화면을 닫는다`() = runTest {
        fakeChallengeRepository.detailError = "네트워크 오류가 발생하였습니다."
        val viewModel = createViewModel()
        assertIs<OathUiState.Error>(viewModel.uiState.value)

        viewModel.uiEffect.test {
            viewModel.requestExit()

            assertEquals(OathUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `confirmExit 은 화면을 닫고 dismiss 는 모달을 내린다`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")
        viewModel.requestExit()
        assertEquals(OathModalEffect.ExitConfirm, viewModel.modalEffect.value)

        viewModel.dismiss()
        assertEquals(OathModalEffect.Hidden, viewModel.modalEffect.value)

        viewModel.uiEffect.test {
            viewModel.confirmExit()

            assertEquals(OathUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `제출 중에는 requestExit 이 아무 일도 하지 않는다`() = runTest {
        fakeChallengeRepository.acceptPending = true
        val viewModel = createViewModel()
        viewModel.updateMyMission("책 30페이지 읽기")
        viewModel.updateSignature(sampleSignature())
        viewModel.submit()
        assertTrue(viewModel.data().isSubmitting)

        viewModel.uiEffect.test {
            viewModel.requestExit()

            expectNoEvents()
        }

        assertEquals(OathModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `챌린저가 서명하지 않았어도 Data 로 정상 전이한다`() = runTest {
        fakeChallengeRepository.detail = sampleDetail(challengerSignature = null)

        val viewModel = createViewModel()

        val state = viewModel.data()
        assertNull(state.challengerSignature)
        assertEquals("우건", state.challengerNickname)
    }
}
