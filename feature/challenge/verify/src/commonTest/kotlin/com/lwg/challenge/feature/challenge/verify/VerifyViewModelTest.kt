package com.lwg.challenge.feature.challenge.verify

import app.cash.turbine.test
import com.lwg.challenge.camera.CameraCaptureFailure
import com.lwg.challenge.camera.CameraCaptureResult
import com.lwg.challenge.camera.CapturedPhoto
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.feature.challenge.verify.contract.VerifyModalEffect
import com.lwg.challenge.feature.challenge.verify.contract.VerifyUiEffect
import com.lwg.challenge.feature.challenge.verify.contract.VerifyUiState
import com.lwg.challenge.permission.PermissionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val CHALLENGE_ID = 42L

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyViewModelTest {

    private lateinit var fakeVerificationRepository: FakeVerificationRepository
    private lateinit var fakePermissionManager: FakePermissionManager

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeVerificationRepository = FakeVerificationRepository()
        fakePermissionManager = FakePermissionManager()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): VerifyViewModel =
        VerifyViewModel(
            challengeId = CHALLENGE_ID,
            verificationRepository = fakeVerificationRepository,
            permissionManager = fakePermissionManager,
        )

    private fun VerifyViewModel.confirm(): VerifyUiState.Confirm =
        assertIs<VerifyUiState.Confirm>(uiState.value)

    private fun samplePhoto() = CapturedPhoto(
        jpegBytes = ByteArray(16) { it.toByte() },
        width = 1080,
        height = 1440,
    )

    private fun VerifyViewModel.capturePhoto() {
        completeCapture(CameraCaptureResult.Success(samplePhoto()))
    }

    // --- 촬영 진입 / 권한 ---

    @Test
    fun `권한이 GRANTED 면 요청 없이 바로 카메라를 실행한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.requestCapture()

            assertEquals(VerifyUiEffect.LaunchCamera, awaitItem())
        }

        assertEquals(0, fakePermissionManager.requestCallCount)
    }

    @Test
    fun `권한이 DENIED 면 요청하고 허용되면 카메라를 실행한다`() = runTest {
        fakePermissionManager.statusResult = PermissionStatus.DENIED
        fakePermissionManager.requestResult = PermissionStatus.GRANTED
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.requestCapture()

            assertEquals(VerifyUiEffect.LaunchCamera, awaitItem())
        }

        assertEquals(1, fakePermissionManager.requestCallCount)
    }

    @Test
    fun `요청에서 다시 거절되면 카메라도 설정 안내도 띄우지 않는다`() = runTest {
        fakePermissionManager.statusResult = PermissionStatus.DENIED
        fakePermissionManager.requestResult = PermissionStatus.DENIED
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.requestCapture()

            expectNoEvents()
        }

        assertEquals(VerifyModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `요청 결과가 DENIED_ALWAYS 면 설정 안내를 띄운다`() = runTest {
        fakePermissionManager.statusResult = PermissionStatus.DENIED
        fakePermissionManager.requestResult = PermissionStatus.DENIED_ALWAYS
        val viewModel = createViewModel()

        viewModel.requestCapture()

        assertEquals(VerifyModalEffect.CameraPermissionSettings, viewModel.modalEffect.value)
    }

    @Test
    fun `이미 DENIED_ALWAYS 면 재요청 없이 설정 안내를 띄운다`() = runTest {
        fakePermissionManager.statusResult = PermissionStatus.DENIED_ALWAYS
        val viewModel = createViewModel()

        viewModel.requestCapture()

        assertEquals(0, fakePermissionManager.requestCallCount)
        assertEquals(VerifyModalEffect.CameraPermissionSettings, viewModel.modalEffect.value)
    }

    @Test
    fun `openPermissionSettings 는 설정 앱을 열고 모달을 내린다`() = runTest {
        fakePermissionManager.statusResult = PermissionStatus.DENIED_ALWAYS
        val viewModel = createViewModel()
        viewModel.requestCapture()

        viewModel.openPermissionSettings()

        assertEquals(1, fakePermissionManager.openAppSettingsCallCount)
        assertEquals(VerifyModalEffect.Hidden, viewModel.modalEffect.value)
    }

    // --- 촬영 결과 ---

    @Test
    fun `촬영에 성공하면 확인 단계로 전이한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.capturePhoto()

        val state = viewModel.confirm()
        assertFalse(state.isSubmitting)
        assertNull(state.failure)
        assertTrue(state.canSubmit)
    }

    @Test
    fun `촬영을 취소하면 상태가 바뀌지 않는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.completeCapture(CameraCaptureResult.Canceled)

        assertIs<VerifyUiState.Capture>(viewModel.uiState.value)
    }

    @Test
    fun `촬영에 실패하면 메시지만 내고 촬영 단계에 머문다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.completeCapture(CameraCaptureResult.Failed(CameraCaptureFailure.NO_CAMERA))

            val effect = assertIs<VerifyUiEffect.ShowMessage>(awaitItem())
            assertEquals("사용할 수 있는 카메라가 없어요", effect.message)
        }

        assertIs<VerifyUiState.Capture>(viewModel.uiState.value)
    }

    // --- 제출 ---

    @Test
    fun `제출에 성공하면 완료로 전이한다`() = runTest {
        val viewModel = createViewModel()
        viewModel.capturePhoto()

        viewModel.submit()

        assertEquals(listOf(CHALLENGE_ID), fakeVerificationRepository.submittedChallengeIds)
        assertIs<VerifyUiState.Done>(viewModel.uiState.value)
    }

    @Test
    fun `제출 시 촬영한 사진의 jpegBytes 가 그대로 전달된다`() = runTest {
        val viewModel = createViewModel()
        val photo = samplePhoto()
        viewModel.completeCapture(CameraCaptureResult.Success(photo))

        viewModel.submit()

        assertEquals(1, fakeVerificationRepository.submitCallCount)
        assertTrue(photo.jpegBytes.contentEquals(fakeVerificationRepository.submittedJpegBytes.single()))
    }

    @Test
    fun `Rejected 면 서버 메시지를 그대로 표시하고 재시도를 막는다`() = runTest {
        fakeVerificationRepository.submitOutcome =
            VerificationSubmitOutcome.Rejected("사진 용량이 커서 올릴 수 없어요")
        val viewModel = createViewModel()
        viewModel.capturePhoto()

        viewModel.submit()

        val failed = viewModel.confirm()
        assertEquals("사진 용량이 커서 올릴 수 없어요", failed.failure?.message)
        assertFalse(assertNotNull(failed.failure).isRetryable)
        assertFalse(failed.canSubmit)
        assertTrue(failed.canRetake)
    }

    // 거부 문구를 성공 신호로 되읽지 않는다 — 결과 판정은 outcome 타입만 본다.
    @Test
    fun `거부 문구가 와도 성공으로 해석하지 않는다`() = runTest {
        fakeVerificationRepository.submitOutcome =
            VerificationSubmitOutcome.Rejected("JPEG 사진만 올릴 수 있어요")
        val viewModel = createViewModel()
        viewModel.capturePhoto()

        viewModel.submit()

        val failed = viewModel.confirm()
        assertEquals("JPEG 사진만 올릴 수 있어요", failed.failure?.message)
        assertFalse(assertNotNull(failed.failure).isRetryable)
    }

    // --- 전송 유실 (Unreachable) ---

    // 재제출이 last-write-wins 로 허용되면서 맹목 재시도가 안전해졌다 — 열어 두기만 하면 된다.
    @Test
    fun `Unreachable 이면 현황 조회 없이 재시도 가능한 실패를 표시한다`() = runTest {
        fakeVerificationRepository.submitOutcome =
            VerificationSubmitOutcome.Unreachable("네트워크 오류가 발생했어요")
        val viewModel = createViewModel()
        viewModel.capturePhoto()

        viewModel.submit()

        assertEquals(0, fakeVerificationRepository.getVerificationsCallCount)
        val failed = viewModel.confirm()
        assertEquals("네트워크 오류가 발생했어요", failed.failure?.message)
        assertTrue(assertNotNull(failed.failure).isRetryable)
        assertTrue(failed.canSubmit)
        assertTrue(failed.canRetake)
    }

    @Test
    fun `제출 중 submit 을 다시 호출해도 요청은 1회만 나간다`() = runTest {
        fakeVerificationRepository.submitPending = true
        val viewModel = createViewModel()
        viewModel.capturePhoto()

        viewModel.submit()
        assertTrue(viewModel.confirm().isSubmitting)

        viewModel.submit()

        assertEquals(1, fakeVerificationRepository.submitCallCount)
    }

    @Test
    fun `제출 중에는 재촬영 요청도 무시한다`() = runTest {
        fakeVerificationRepository.submitPending = true
        val viewModel = createViewModel()
        viewModel.capturePhoto()
        viewModel.submit()
        assertTrue(viewModel.confirm().isSubmitting)

        viewModel.uiEffect.test {
            viewModel.requestCapture()

            expectNoEvents()
        }
    }

    // --- 이탈 ---

    @Test
    fun `촬영 전 requestExit 은 확인 없이 바로 화면을 닫는다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.requestExit()

            assertEquals(VerifyUiEffect.NavigateBack, awaitItem())
        }

        assertEquals(VerifyModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `사진이 있는 상태의 requestExit 은 확인을 띄우고 화면을 닫지 않는다`() = runTest {
        val viewModel = createViewModel()
        viewModel.capturePhoto()

        viewModel.uiEffect.test {
            viewModel.requestExit()

            expectNoEvents()
        }

        assertEquals(VerifyModalEffect.ExitConfirm, viewModel.modalEffect.value)
    }

    @Test
    fun `제출 중에는 requestExit 이 아무 일도 하지 않는다`() = runTest {
        fakeVerificationRepository.submitPending = true
        val viewModel = createViewModel()
        viewModel.capturePhoto()
        viewModel.submit()
        assertTrue(viewModel.confirm().isSubmitting)

        viewModel.uiEffect.test {
            viewModel.requestExit()

            expectNoEvents()
        }

        assertEquals(VerifyModalEffect.Hidden, viewModel.modalEffect.value)
    }

    @Test
    fun `confirmExit 은 화면을 닫고 dismiss 는 모달만 내린다`() = runTest {
        val viewModel = createViewModel()
        viewModel.capturePhoto()
        viewModel.requestExit()
        assertEquals(VerifyModalEffect.ExitConfirm, viewModel.modalEffect.value)

        viewModel.dismiss()
        assertEquals(VerifyModalEffect.Hidden, viewModel.modalEffect.value)

        viewModel.uiEffect.test {
            viewModel.confirmExit()

            assertEquals(VerifyUiEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `완료 화면의 complete 는 화면을 닫는다`() = runTest {
        val viewModel = createViewModel()
        viewModel.capturePhoto()
        viewModel.submit()
        assertIs<VerifyUiState.Done>(viewModel.uiState.value)

        viewModel.uiEffect.test {
            viewModel.complete()

            assertEquals(VerifyUiEffect.NavigateBack, awaitItem())
        }
    }
}
