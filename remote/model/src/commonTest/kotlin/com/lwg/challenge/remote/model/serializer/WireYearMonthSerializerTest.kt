package com.lwg.challenge.remote.model.serializer

import kotlinx.datetime.YearMonth
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 아직 이 직렬화기를 쓰는 DTO 가 없어 테스트 전용 홀더로 인코딩/디코딩을 검증한다. */
@Serializable
private data class Holder(
    @Serializable(with = WireYearMonthSerializer::class)
    val month: YearMonth? = null,
)

class WireYearMonthSerializerTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private fun decode(month: String) =
        json.decodeFromString(Holder.serializer(), """{ "month": $month }""").month

    @Test
    fun `wire 포맷 yyyy-MM 을 파싱한다`() {
        assertEquals(YearMonth(2026, 8), decode("\"2026-08\""))
    }

    /** 서버가 `2026-8` 을 거부하므로 앱도 받아주지 않는다 — 받아주면 그대로 요청에 되돌려 보내게 된다. */
    @Test
    fun `zero-padding 없는 월은 null 로 떨어진다`() {
        assertNull(decode("\"2026-8\""))
    }

    /** 월 필드에 날짜까지 온 것은 계약 위반이라 값을 취하지 않고 필드만 비운다. */
    @Test
    fun `날짜까지 온 문자열은 null 로 떨어진다`() {
        assertNull(decode("\"2026-08-14\""))
    }

    @Test
    fun `날짜시간 문자열은 null 로 떨어진다`() {
        assertNull(decode("\"2026-08-14 09:30:00\""))
    }

    @Test
    fun `깨진 문자열은 null 로 떨어진다`() {
        assertNull(decode("\"garbage\""))
    }

    /** 파싱 실패든 타입 불일치든 응답 전체 디코딩이 터지면 화면이 통째로 비므로 전부 null 로 흡수한다. */
    @Test
    fun `JSON null 과 문자열 아닌 값도 null 로 흡수한다`() {
        assertNull(decode("null"))
        assertNull(decode("202608"))
    }

    @Test
    fun `필드 누락은 기본값 null`() {
        assertNull(json.decodeFromString(Holder.serializer(), "{ }").month)
    }

    /** 한 자리 월도 서버가 요구하는 zero-padding 형태로 나가야 한다 — `2026-3` 은 서버가 거부한다. */
    @Test
    fun `직렬화하면 zero-padding 된 wire 포맷으로 되돌아간다`() {
        assertEquals(
            """{"month":"2026-03"}""",
            json.encodeToString(Holder.serializer(), Holder(YearMonth(2026, 3))),
        )
        assertEquals(
            """{"month":"2026-12"}""",
            json.encodeToString(Holder.serializer(), Holder(YearMonth(2026, 12))),
        )
    }

    @Test
    fun `디코딩 후 다시 인코딩하면 원문과 같다`() {
        val decoded = json.decodeFromString(Holder.serializer(), """{"month":"2026-08"}""")

        assertEquals("""{"month":"2026-08"}""", json.encodeToString(Holder.serializer(), decoded))
    }
}
