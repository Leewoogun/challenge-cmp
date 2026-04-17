package com.lwg.challenge.local.datastore

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

internal class JsonSerializer<T>(
    private val serializer: KSerializer<T>,
    override val defaultValue: T,
) : OkioSerializer<T> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun readFrom(source: BufferedSource): T {
        return try {
            json.decodeFromString(serializer, source.readUtf8())
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: T, sink: BufferedSink) {
        sink.writeUtf8(json.encodeToString(serializer, t))
    }
}
