package com.lwg.challenge.feature.mypage

import app.cash.turbine.test
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.usecase.GetMyPageDataUseCase
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.domain.usecase.WithdrawUseCase
import com.lwg.challenge.feature.mypage.contract.MyPageModalEffect
import com.lwg.challenge.feature.mypage.contract.MyPageUiEffect
import com.lwg.challenge.feature.mypage.contract.MyPageUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MyPageViewModelTest {

    private lateinit var fakeRecord: FakeRecordRepository
    private lateinit var fakeUserInfo: FakeUserInfoRepository
    private lateinit var fakeWithdraw: FakeWithdrawRepository
    private lateinit var fakeLogin: FakeLoginRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRecord = FakeRecordRepository()
        fakeUserInfo = FakeUserInfoRepository()
        fakeWithdraw = FakeWithdrawRepository()
        fakeLogin = FakeLoginRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MyPageViewModel = MyPageViewModel(
        getMyPageDataUseCase = GetMyPageDataUseCase(fakeRecord, fakeUserInfo),
        withdrawUseCase = WithdrawUseCase(
            fakeWithdraw,
            LogoutUseCase(fakeLogin, fakeUserInfo),
        ),
    )

    // uiState 가 WhileSubscribed 이므로 구독자가 있어야 파이프라인이 시작된다.
    // 초기 로딩 실패의 ShowMessage 를 검증하는 테스트는 이 호출을 `uiEffect.test { }` 안에 둬야 한다 —
    // uiEffect 가 replay 0 SharedFlow 라 collector 가 붙기 전에 emit 된 이펙트는 그대로 버려진다.
    private fun TestScope.subscribeUiState(viewModel: MyPageViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    @Test
    fun `전적과 유저 정보가 모두 오면 Data 에 둘 다 실린다`() = runTest {
        fakeRecord.record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3)
        fakeUserInfo.seedCache(
            UserInfo(id = 12, kakaoId = 999, nickname = "이우건", profileImageUrl = null),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value

        assertIs<MyPageUiState.Data>(state)
        assertEquals(7, state.record?.win)
        assertEquals("이우건", state.userInfo?.nickname)
        assertEquals("이우건", state.nickname)
    }

    /**
     * 유저 정보 Flow 가 아무것도 흘리지 않아도 combine 이 결과를 내야 한다. UseCase 의 `onEmpty`
     * 처리가 빠지면 마이페이지 전체가 Loading 에 영원히 갇힌다 — 그 회귀를 고정하는 테스트다.
     */
    @Test
    fun `userInfo 가 없어도 Data 를 낸다`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 1, draw = 0, currentStreak = 2)
        fakeUserInfo.error = "유저 조회 실패"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value

        assertIs<MyPageUiState.Data>(state)
        assertEquals(5, state.record?.win)
        assertNull(state.userInfo)
        assertEquals("", state.nickname)
    }

    @Test
    fun `전적이 전부 0 이면 isRecordMuted 가 true`() = runTest {
        fakeRecord.record = UserRecord.Empty

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as MyPageUiState.Data

        assertTrue(state.isRecordMuted)
    }

    @Test
    fun `전적이 있으면 isRecordMuted 가 false`() = runTest {
        fakeRecord.record = UserRecord(win = 1, lose = 0, draw = 0, currentStreak = 1)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as MyPageUiState.Data

        assertFalse(state.isRecordMuted)
    }

    @Test
    fun `전적이 하나도 없으면 요약 캡션이 기록 없음 문구다`() = runTest {
        fakeRecord.record = UserRecord.Empty

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as MyPageUiState.Data

        assertEquals("아직 기록이 없어요", state.recordSummary)
    }

    /**
     * 무가 분모에 들어가므로 전부 무승부도 0 분모가 아니다 — 승률 0% 로 정상 계산된다.
     * 분모를 `win + lose` 로 되돌리면 여기가 0 으로 나눠 터진다.
     */
    @Test
    fun `전부 무승부면 승률이 0 퍼센트다`() = runTest {
        fakeRecord.record = UserRecord(win = 0, lose = 0, draw = 3, currentStreak = 0)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as MyPageUiState.Data

        assertEquals("총 3챌린지 · 승률 0%", state.recordSummary)
    }

    @Test
    fun `승률 분모에 무를 포함한다`() = runTest {
        fakeRecord.record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 0)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as MyPageUiState.Data

        assertEquals("총 12챌린지 · 승률 58%", state.recordSummary)
    }

    /** 5/8 = 62.5% — 정수 나눗셈이면 62 가 나온다. 서버 `Math.round` 와 방향을 맞춘 것을 고정한다. */
    @Test
    fun `승률은 버림이 아니라 반올림한다`() = runTest {
        fakeRecord.record = UserRecord(win = 5, lose = 3, draw = 0, currentStreak = 0)

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as MyPageUiState.Data

        assertEquals("총 8챌린지 · 승률 63%", state.recordSummary)
    }

    @Test
    fun `전적 조회가 실패하면 ShowMessage 를 발행한다`() = runTest {
        fakeRecord.error = "전적 조회 실패"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            val effect = awaitItem()
            assertIs<MyPageUiEffect.ShowMessage>(effect)
            assertEquals("전적 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * 전적 Flow 에 폴백이 없으면 combine 이 값을 못 내 화면이 Loading 에 영원히 갇힌다(무한 스피너).
     * 닉네임·아바타는 출처가 다른 성공한 데이터라 카드는 그려야 한다 — 그 회귀를 고정한다.
     */
    @Test
    fun `전적 조회가 실패해도 Data 를 내고 record 는 null 이다`() = runTest {
        fakeRecord.error = "전적 조회 실패"
        fakeUserInfo.seedCache(
            UserInfo(id = 12, kakaoId = 999, nickname = "이우건", profileImageUrl = null),
        )

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value

        assertIs<MyPageUiState.Data>(state)
        assertNull(state.record)
        assertEquals("이우건", state.nickname)
    }

    // UserRecord.Empty 로 폴백하면 여기가 "아직 기록이 없어요" 가 된다 — 7승 3패인 사람에게 하는 거짓말이다.
    @Test
    fun `전적 조회가 실패하면 요약 캡션이 조회 실패 문구다`() = runTest {
        fakeRecord.error = "전적 조회 실패"

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        val state = viewModel.uiState.value as MyPageUiState.Data

        assertEquals("전적을 불러오지 못했어요", state.recordSummary)
    }

    @Test
    fun `유저 조회가 실패하면 ShowMessage 를 발행한다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeUserInfo.error = "유저 조회 실패"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            subscribeUiState(viewModel)
            val effect = awaitItem()
            assertIs<MyPageUiEffect.ShowMessage>(effect)
            assertEquals("유저 조회 실패", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `탈퇴 확인을 누르면 다이얼로그가 뜬다`() = runTest {
        val viewModel = createViewModel()

        viewModel.requestWithdraw()

        assertEquals(MyPageModalEffect.ConfirmWithdraw, viewModel.modalEffect.value)
    }

    @Test
    fun `탈퇴가 성공하면 Withdrawn 을 발행하고 로컬을 정리한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.withdraw()
            assertIs<MyPageUiEffect.Withdrawn>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(fakeLogin.clearTokensCalled)
        assertTrue(fakeUserInfo.clearUserInfoCacheCalled)
        // 계정이 이미 사라져 끊을 세션이 없다.
        assertFalse(fakeLogin.logoutCalled)
        assertEquals(MyPageModalEffect.Hidden, viewModel.modalEffect.value)
    }

    // 계정이 그대로 살아 있으므로 재시도하려면 세션이 남아 있어야 한다.
    @Test
    fun `탈퇴가 실패하면 ShowMessage 를 발행하고 로컬을 정리하지 않는다`() = runTest {
        fakeWithdraw.error = "일시적인 문제로 탈퇴하지 못했어요. 잠시 후 다시 시도해주세요"

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.withdraw()
            val effect = awaitItem()
            assertIs<MyPageUiEffect.ShowMessage>(effect)
            assertEquals("일시적인 문제로 탈퇴하지 못했어요. 잠시 후 다시 시도해주세요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(fakeLogin.clearTokensCalled)
        assertFalse(fakeUserInfo.clearUserInfoCacheCalled)
    }

    @Test
    fun `탈퇴가 실패하면 다시 시도할 수 있다`() = runTest {
        fakeWithdraw.error = "탈퇴 실패"

        val viewModel = createViewModel()
        viewModel.withdraw()
        viewModel.withdraw()

        assertEquals(2, fakeWithdraw.callCount)
    }

    @Test
    fun `탈퇴 진행 중에는 중복 호출을 무시한다`() = runTest {
        fakeWithdraw.gate = CompletableDeferred()

        val viewModel = createViewModel()
        viewModel.withdraw()
        viewModel.withdraw()

        assertEquals(1, fakeWithdraw.callCount)
    }

    @Test
    fun `탈퇴 진행 중에는 확인 다이얼로그를 다시 띄우지 않는다`() = runTest {
        fakeWithdraw.gate = CompletableDeferred()

        val viewModel = createViewModel()
        viewModel.withdraw()
        viewModel.requestWithdraw()

        assertEquals(MyPageModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `탈퇴 진행 중이면 isWithdrawing 이 true 다`() = runTest {
        fakeRecord.record = UserRecord.Empty
        fakeWithdraw.gate = CompletableDeferred()

        val viewModel = createViewModel()
        subscribeUiState(viewModel)
        viewModel.withdraw()

        val state = viewModel.uiState.value as MyPageUiState.Data
        assertTrue(state.isWithdrawing)
    }
}
