package com.lwg.challenge.remote.model.serializer

import com.lwg.challenge.utils.datetime.ChallengeDateTimeFormats
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * 서버 wire 포맷(`yyyy-MM-dd HH:mm:ss`, KST) ↔ [LocalDateTime].
 *
 * 기본 ISO 직렬화기는 `T` 구분자를 요구해 이 포맷을 읽지 못한다.
 *
 * 파싱 실패를 예외로 올리지 않고 null 로 떨어뜨린다 — 시간 문자열 하나가 어긋나면 응답 전체
 * 디코딩이 실패해 화면이 통째로 비기 때문. 그 값을 버릴지 항목을 버릴지는 매퍼가 정한다.
 */
object WireLocalDateTimeSerializer : NullTolerantStringSerializer<LocalDateTime>("WireLocalDateTime") {
    override fun parse(text: String): LocalDateTime? = ChallengeDateTimeFormats.parseWireDateTime(text)
    override fun render(value: LocalDateTime): String = value.format(ChallengeDateTimeFormats.WIRE_DATE_TIME)
}

/** 날짜 전용 `yyyy-MM-dd`. ISO 와 동일하지만 실패 관용 정책을 [WireLocalDateTimeSerializer] 와 맞춘다. */
object WireLocalDateSerializer : NullTolerantStringSerializer<LocalDate>("WireLocalDate") {
    override fun parse(text: String): LocalDate? = ChallengeDateTimeFormats.parseWireDate(text)
    override fun render(value: LocalDate): String = value.toString()
}

/**
 * 문자열 형태의 값을 읽되, JSON null / 문자열 아닌 값 / 파싱 실패를 모두 null 로 흡수한다.
 *
 * `decodeString()` 대신 [JsonDecoder.decodeJsonElement] 를 쓰는 이유: 실패를 예외로 알게 되면
 * 그 시점엔 이미 스트림이 깨져 복구할 수 없다. 엘리먼트로 먼저 받아야 관용 처리가 가능하다.
 */
abstract class NullTolerantStringSerializer<T : Any>(name: String) : KSerializer<T?> {

    final override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(name, PrimitiveKind.STRING).nullable

    protected abstract fun parse(text: String): T?
    protected abstract fun render(value: T): String

    @OptIn(ExperimentalSerializationApi::class)
    final override fun serialize(encoder: Encoder, value: T?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(render(value))
    }

    final override fun deserialize(decoder: Decoder): T? {
        val jsonDecoder = decoder as? JsonDecoder ?: return parse(decoder.decodeString())
        val primitive = jsonDecoder.decodeJsonElement() as? JsonPrimitive ?: return null
        if (!primitive.isString) return null
        return parse(primitive.content)
    }
}
