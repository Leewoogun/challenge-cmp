package com.lwg.challenge.camera

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JpegMetadataTest {

    private val gpsPayload = "GPS:37.5665,126.9780".encodeToByteArray()
    private val app1Exif = segment(0xE1, "Exif".encodeToByteArray() + bytes(0x00, 0x00) + gpsPayload)
    private val app13Iptc = segment(0xED, "Photoshop 3.0".encodeToByteArray() + bytes(0x00))
    private val app0Jfif = segment(0xE0, "JFIF".encodeToByteArray() + bytes(0x00, 0x01, 0x02, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00))
    private val app2Icc = segment(0xE2, "ICC_PROFILE".encodeToByteArray() + bytes(0x00, 0x01, 0x01))

    // FF 00 스터핑과 APP1 마커처럼 보이는 FF E1 을 일부러 넣은 스캔 데이터 —
    // SOS 이후를 마커로 파싱하면 이 바이트열이 잘려 나간다
    private val scanData = bytes(0x12, 0x34, 0xFF, 0x00, 0x56, 0xFF, 0xE1, 0x00, 0x08, 0x78, 0xFF, 0x00)
    private val sos = segment(0xDA, bytes(0x01, 0x01, 0x00)) + scanData + bytes(0xFF, 0xD9)

    @Test
    fun `APP1 Exif 세그먼트가 제거된다`() {
        val stripped = JpegMetadata.strip(jpeg(app0Jfif, app1Exif, sos))

        assertContentEquals(jpeg(app0Jfif, sos), stripped)
    }

    @Test
    fun `GPS 페이로드가 결과에 남지 않는다`() {
        val input = jpeg(app0Jfif, app1Exif, sos)
        assertTrue(input.containsBytes(gpsPayload))

        val stripped = JpegMetadata.strip(input)

        assertFalse(stripped.containsBytes(gpsPayload))
    }

    @Test
    fun `APP13 IPTC 세그먼트도 제거된다`() {
        val stripped = JpegMetadata.strip(jpeg(app0Jfif, app13Iptc, sos))

        assertContentEquals(jpeg(app0Jfif, sos), stripped)
    }

    @Test
    fun `APP0 JFIF 는 보존된다`() {
        val stripped = JpegMetadata.strip(jpeg(app0Jfif, app1Exif, sos))

        assertTrue(stripped.containsBytes(app0Jfif))
    }

    @Test
    fun `APP2 ICC 프로파일은 보존된다`() {
        val stripped = JpegMetadata.strip(jpeg(app0Jfif, app2Icc, app1Exif, sos))

        assertTrue(stripped.containsBytes(app2Icc))
    }

    @Test
    fun `SOS 이후 스캔 데이터는 마커처럼 보이는 바이트가 있어도 그대로 보존된다`() {
        val stripped = JpegMetadata.strip(jpeg(app1Exif, sos))

        assertContentEquals(sos, stripped.copyOfRange(stripped.size - sos.size, stripped.size))
    }

    @Test
    fun `EXIF 가 없는 JPEG 은 내용이 그대로다`() {
        val input = jpeg(app0Jfif, sos)

        assertContentEquals(input, JpegMetadata.strip(input))
    }

    @Test
    fun `JPEG 이 아닌 입력은 원본 그대로 반환된다`() {
        val png = bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01)

        assertContentEquals(png, JpegMetadata.strip(png))
    }

    @Test
    fun `잘린 JPEG 은 원본 그대로 반환된다`() {
        // APP1 길이 필드가 남은 바이트보다 길어 파싱이 범위를 벗어나는 케이스
        val truncated = jpeg(app0Jfif, app1Exif).copyOfRange(0, 2 + app0Jfif.size + 5)

        assertContentEquals(truncated, JpegMetadata.strip(truncated))
    }

    @Test
    fun `hasMetadataSegment 는 APP1 유무를 정확히 판정한다`() {
        assertTrue(JpegMetadata.hasMetadataSegment(jpeg(app0Jfif, app1Exif, sos)))
        assertFalse(JpegMetadata.hasMetadataSegment(jpeg(app0Jfif, sos)))
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    private fun segment(marker: Int, payload: ByteArray): ByteArray {
        val length = payload.size + 2
        return bytes(0xFF, marker, length shr 8, length and 0xFF) + payload
    }

    private fun jpeg(vararg segments: ByteArray): ByteArray =
        segments.fold(bytes(0xFF, 0xD8)) { acc, s -> acc + s }

    private fun ByteArray.containsBytes(needle: ByteArray): Boolean =
        (0..size - needle.size).any { start -> needle.indices.all { this[start + it] == needle[it] } }
}
