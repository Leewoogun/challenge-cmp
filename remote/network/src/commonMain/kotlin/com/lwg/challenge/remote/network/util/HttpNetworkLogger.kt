package com.lwg.challenge.remote.network.util

import com.lwg.challenge.utils.Logger
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.util.AttributeKey
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val RequestTimeMark = AttributeKey<TimeMark>("RequestTimeMark")
private val RequestMethod = AttributeKey<String>("RequestMethod")

private const val MAX_BODY_LENGTH = 5000

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
        val responseBody = response.bodyAsText().take(MAX_BODY_LENGTH)

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
