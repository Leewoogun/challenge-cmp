package com.lwg.challenge.remote.network.util

import com.lwg.challenge.utils.Logger
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val RequestTimeMark = AttributeKey<TimeMark>("RequestTimeMark")
private val RequestMethod = AttributeKey<String>("RequestMethod")

private const val MAX_BODY_LENGTH = 5000
private const val UNKNOWN = "unknown"

val HttpNetworkLogger = createClientPlugin("HttpNetworkLogger") {

    onRequest { request, content ->
        request.attributes.put(RequestTimeMark, TimeSource.Monotonic.markNow())
        request.attributes.put(RequestMethod, request.method.value)
    }

    onResponse { response ->
        val request = response.call.request
        val method = request.attributes.getOrNull(RequestMethod) ?: "UNKNOWN"
        val mark = request.attributes.getOrNull(RequestTimeMark)
        val elapsed = mark?.elapsedNow()?.inWholeMilliseconds ?: -1
        val responseBody = response.responseBodyLog()

        Logger.i(
            buildString {
                appendLine()
                appendLine("┌── HTTP $method ──────────────")
                appendLine("│ Request URL: ${request.url}")
                appendLine("│ Response Code: ${response.status.value}")
                appendLine("│ Response Time: ${elapsed}ms")
                appendLine("│ Response Body: $responseBody")
                append("└──────────────────────────")
            }
        )
    }
}

/**
 * 텍스트류(JSON 계열, 그리고 모든 text 하위 타입) 응답만 본문을 읽고, 그 외에는 본문을 건드리지 않은 채 요약만 남긴다.
 *
 * 이 로거가 붙은 HttpClient 는 Coil 이미지 로더처럼 응답을 **스트리밍으로** 소비하는 쪽과 공유된다
 * (401 자동 갱신을 유지하려고 Coil 도 같은 인증 클라이언트를 쓴다).
 * `prepareRequest { }.execute { }` 경로에서는 Ktor 가 body 를 메모리에 저장하지 않으므로,
 * 로거가 먼저 [bodyAsText] 로 채널을 비우면 실제 소비자에게는 빈 스트림이 도착해 디코딩이 실패한다.
 * (사진 조회가 200 + 정상 JPEG 인데도 "사진을 불러오지 못했어요" 가 뜨던 회귀의 원인)
 *
 * 같은 이유로 Content-Type 을 믿을 수 없을 때(헤더 없음 / 파싱 실패 / 와일드카드)에도 읽지 않는 쪽으로 기운다.
 * 로그가 덜 자세한 것이 남의 스트림을 깨뜨리는 것보다 낫다.
 */
internal suspend fun HttpResponse.responseBodyLog(): String {
    val contentType = runCatching { contentType() }.getOrNull()

    if (!contentType.isTextualForLogging()) {
        return "[binary: ${contentType ?: UNKNOWN}, ${contentLength() ?: UNKNOWN}B]"
    }

    return bodyAsText().take(MAX_BODY_LENGTH)
}

internal fun ContentType?.isTextualForLogging(): Boolean {
    if (this == null) return false

    return contentType.equals("text", ignoreCase = true) ||
        match(ContentType.Application.Json) ||
        contentSubtype.endsWith("+json", ignoreCase = true)
}
