package com.lwg.challenge.remote.network.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.toByteArray
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpNetworkLoggerTest {

    private val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()) +
        ByteArray(2048) { (it % 251).toByte() }

    private fun mockClient(
        content: ByteArray,
        contentType: String?,
        installLogger: Boolean,
    ): HttpClient {
        val engine = MockEngine {
            respond(
                content = ByteReadChannel(content),
                status = HttpStatusCode.OK,
                headers = HeadersBuilder().apply {
                    contentType?.let { append(HttpHeaders.ContentType, it) }
                    append(HttpHeaders.ContentLength, content.size.toString())
                }.build(),
            )
        }
        return HttpClient(engine) {
            if (installLogger) install(HttpNetworkLogger)
        }
    }

    // prepareGet + execute 는 Ktor 가 body 를 저장하지 않는 스트리밍 경로다. Coil 이미지 로더가 쓰는 경로이며
    // 로거가 본문을 먼저 소비하면 이 테스트가 빈 배열로 실패한다.
    @Test
    fun `이미지 응답은 로거를 거친 뒤에도 후속 reader 가 전체 바이트를 읽는다`() = runTest {
        val client = mockClient(jpegBytes, ContentType.Image.JPEG.toString(), installLogger = true)

        val streamed = client.prepareGet("https://example.com/photo.jpg").execute { response ->
            response.bodyAsChannel().toByteArray()
        }

        assertContentEquals(jpegBytes, streamed)
    }

    @Test
    fun `JSON 응답은 로거를 거친 뒤에도 본문을 다시 읽을 수 있다`() = runTest {
        val json = """{"error":false,"code":200,"message":"OK"}"""
        val client = mockClient(
            content = json.encodeToByteArray(),
            contentType = ContentType.Application.Json.toString(),
            installLogger = true,
        )

        val body = client.get("https://example.com/api").bodyAsText()

        assertEquals(json, body)
    }

    @Test
    fun `이미지 응답의 로그 본문에는 바이너리 요약만 남는다`() = runTest {
        val client = mockClient(jpegBytes, ContentType.Image.JPEG.toString(), installLogger = false)

        val log = client.prepareGet("https://example.com/photo.jpg")
            .execute { response -> response.responseBodyLog() }

        assertEquals("[binary: image/jpeg, ${jpegBytes.size}B]", log)
    }

    @Test
    fun `Content-Type 이 없으면 본문을 읽지 않는다`() = runTest {
        val client = mockClient(jpegBytes, contentType = null, installLogger = false)

        val streamed = client.prepareGet("https://example.com/photo.jpg").execute { response ->
            assertEquals("[binary: unknown, ${jpegBytes.size}B]", response.responseBodyLog())
            response.bodyAsChannel().toByteArray()
        }

        assertContentEquals(jpegBytes, streamed)
    }

    @Test
    fun `JSON 응답의 로그 본문에는 본문이 그대로 담긴다`() = runTest {
        val json = """{"error":false,"code":200,"message":"OK"}"""
        val client = mockClient(
            content = json.encodeToByteArray(),
            contentType = "application/json; charset=UTF-8",
            installLogger = false,
        )

        val log = client.get("https://example.com/api").responseBodyLog()

        assertEquals(json, log)
    }

    @Test
    fun `텍스트류 Content-Type 만 본문 로깅 대상이다`() {
        val missing: ContentType? = null

        assertTrue(ContentType.Application.Json.isTextualForLogging())
        assertTrue(ContentType.parse("application/json; charset=utf-8").isTextualForLogging())
        assertTrue(ContentType.parse("application/problem+json").isTextualForLogging())
        assertTrue(ContentType.Text.Plain.isTextualForLogging())
        assertTrue(ContentType.Text.Html.isTextualForLogging())

        assertFalse(ContentType.Image.JPEG.isTextualForLogging())
        assertFalse(ContentType.Image.PNG.isTextualForLogging())
        assertFalse(ContentType.Application.OctetStream.isTextualForLogging())
        assertFalse(ContentType.Any.isTextualForLogging())
        assertFalse(missing.isTextualForLogging())
    }
}
