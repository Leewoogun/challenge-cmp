package com.lwg.challenge.remote.network.util

import com.lwg.challenge.remote.model.BaseResponse
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ApiResultConverterFactoryTest {

    private val ktorfit = Ktorfit.Builder()
        .httpClient(HttpClient(MockEngine { error("응답 변환만 검증하므로 요청을 보내지 않는다") }))
        .baseUrl("https://test.local/")
        .build()

    private val converter = ApiResultConverterFactory().suspendResponseConverter(
        typeData = TypeData(qualifiedName = "ApiResult", typeInfo = typeInfo<ApiResult<BaseResponse>>()),
        ktorfit = ktorfit,
    )!!

    @Test
    fun `요청 취소는 실패로 바꾸지 않고 그대로 전파한다`() = runTest {
        assertFailsWith<CancellationException> {
            converter.convert(KtorfitResult.Failure(CancellationException("화면 이탈")))
        }
    }

    @Test
    fun `IOException 은 NetworkError 로 변환한다`() = runTest {
        val result = converter.convert(KtorfitResult.Failure(IOException("연결 끊김")))

        assertIs<ApiResult.Failure.NetworkError>(result)
    }

    @Test
    fun `그 외 예외는 UnknownApiError 로 변환한다`() = runTest {
        val result = converter.convert(KtorfitResult.Failure(IllegalStateException("예상 못한 오류")))

        assertIs<ApiResult.Failure.UnknownApiError>(result)
    }
}
