package com.lwg.challenge.remote.network.util

import com.lwg.challenge.remote.model.BaseResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiResultTest {

    private class Recorder {
        val messages = mutableListOf<String>()
        val onError: (String) -> Unit = { messages.add(it) }
    }

    @Test
    fun `UnknownApiError 는 onError 를 호출한다`() {
        val recorder = Recorder()

        ApiResult.Failure.UnknownApiError(IllegalStateException("deserialize failed"))
            .suspendOnFailureWithErrorHandling(recorder.onError)

        assertEquals(1, recorder.messages.size)
        assertTrue(recorder.messages.first().isNotBlank())
    }

    @Test
    fun `UnknownApiError 의 메시지는 재시도를 권하지 않는 고정 문구다`() {
        val recorder = Recorder()

        ApiResult.Failure.UnknownApiError(IllegalStateException("deserialize failed"))
            .suspendOnFailureWithErrorHandling(recorder.onError)

        assertEquals(listOf("요청을 처리하지 못했어요"), recorder.messages)
    }

    @Test
    fun `HttpError 는 서버 메시지로 onError 를 호출한다`() {
        val recorder = Recorder()

        ApiResult.Failure.HttpError(code = 500, message = "server down", body = "{}")
            .suspendOnFailureWithErrorHandling(recorder.onError)

        assertEquals(listOf("server down"), recorder.messages)
    }

    @Test
    fun `NetworkError 는 네트워크 안내 문구로 onError 를 호출한다`() {
        val recorder = Recorder()

        ApiResult.Failure.NetworkError(message = "timeout")
            .suspendOnFailureWithErrorHandling(recorder.onError)

        assertEquals(
            listOf("네트워크 오류가 발생하였습니다. 네트워크를 확인하여 주세요."),
            recorder.messages,
        )
    }

    @Test
    fun `CustomError 는 서버 메시지로 onError 를 호출한다`() {
        val recorder = Recorder()

        ApiResult.Failure.CustomError(code = 4001, message = "이미 신청한 챌린지입니다.")
            .suspendOnFailureWithErrorHandling(recorder.onError)

        assertEquals(listOf("이미 신청한 챌린지입니다."), recorder.messages)
    }

    @Test
    fun `Success 는 onError 를 호출하지 않는다`() {
        val recorder = Recorder()

        ApiResult.Success(BaseResponse(error = false, code = 200, message = "OK"))
            .suspendOnFailureWithErrorHandling(recorder.onError)

        assertEquals(emptyList(), recorder.messages)
    }

    @Test
    fun `모든 Failure 분기에서 onError 는 최대 1회만 호출된다`() {
        val failures = listOf(
            ApiResult.Failure.HttpError(code = 500, message = "server down", body = "{}"),
            ApiResult.Failure.NetworkError(message = "timeout"),
            ApiResult.Failure.UnknownApiError(IllegalStateException("deserialize failed")),
            ApiResult.Failure.CustomError(code = 4001, message = "business error"),
        )

        failures.forEach { failure ->
            val recorder = Recorder()
            failure.suspendOnFailureWithErrorHandling(recorder.onError)
            assertEquals(1, recorder.messages.size, "$failure 에서 onError 호출 횟수가 1 이 아니다")
        }
    }
}
