package com.lwg.challenge.camera

/**
 * 업로드 직전 JPEG 에서 GPS 등 개인정보가 실릴 수 있는 메타데이터 세그먼트를 잘라낸다.
 *
 * 서버는 받은 바이트를 무가공으로 저장·서빙하므로 앱이 떼지 않으면 EXIF 의 GPS 좌표가
 * 상대에게 그대로 전달된다. 플랫폼 인코더가 재인코딩 시 대개 EXIF 를 옮겨 적지 않지만,
 * 그 부수 효과에 기대지 않고 여기서 명시적으로 제거를 보장한다.
 * 회전은 압축 단계에서 이미 픽셀에 반영되므로 방향 메타데이터를 잃어도 안전하다.
 *
 * 제거 대상은 APP1(Exif/XMP)·APP13(IPTC) 뿐이다. APP0(JFIF)·APP2(ICC 컬러 프로파일)는
 * 지우면 색 재현이 바뀔 수 있어 보존한다.
 */
internal object JpegMetadata {

    private const val SOI = 0xD8
    private const val EOI = 0xD9
    private const val SOS = 0xDA
    private const val TEM = 0x01
    private const val APP1 = 0xE1
    private const val APP13 = 0xED

    private val MARKER_PREFIX = 0xFF.toByte()

    /** 메타데이터 세그먼트를 제거한 바이트를 반환한다. 파싱 불가 시 원본을 그대로 반환한다. */
    fun strip(jpeg: ByteArray): ByteArray {
        val segments = findMetadataSegments(jpeg)
        if (segments.isNullOrEmpty()) return jpeg

        val result = ByteArray(jpeg.size - segments.sumOf { it.last - it.first + 1 })
        var src = 0
        var dst = 0
        for (range in segments) {
            jpeg.copyInto(result, dst, src, range.first)
            dst += range.first - src
            src = range.last + 1
        }
        jpeg.copyInto(result, dst, src, jpeg.size)
        return result
    }

    fun hasMetadataSegment(jpeg: ByteArray): Boolean =
        !findMetadataSegments(jpeg).isNullOrEmpty()

    /**
     * 제거 대상 세그먼트 구간 목록. JPEG 이 아니거나 파싱 중 범위를 벗어나면 null —
     * 제거 실패보다 사용자 사진 파괴가 훨씬 나쁘므로 호출부는 원본을 그대로 쓴다.
     */
    private fun findMetadataSegments(jpeg: ByteArray): List<IntRange>? {
        if (jpeg.size < 2 || jpeg[0] != MARKER_PREFIX || jpeg[1] != SOI.toByte()) return null

        val segments = mutableListOf<IntRange>()
        var index = 2
        while (index < jpeg.size) {
            if (jpeg[index] != MARKER_PREFIX) return null
            // 마커 앞에는 FF fill 바이트가 임의 개수 올 수 있다
            var markerIndex = index
            while (markerIndex < jpeg.size && jpeg[markerIndex] == MARKER_PREFIX) markerIndex++
            if (markerIndex >= jpeg.size) return null

            when (val marker = jpeg[markerIndex].toInt() and 0xFF) {
                // SOS 이후는 FF 00 스터핑·RST 마커가 섞인 엔트로피 데이터라 마커 파싱이 불가능 — 통째로 보존하고 종료
                SOS, EOI -> return segments
                // FF 00 은 스캔 데이터 안에서만 유효한 스터핑 — SOS 전에 나오면 구조가 깨진 것
                0x00 -> return null
                TEM, SOI, in 0xD0..0xD7 -> index = markerIndex + 1
                else -> {
                    if (markerIndex + 2 >= jpeg.size) return null
                    // 길이는 빅엔디안 2바이트, 길이 필드 자신을 포함한다
                    val length = ((jpeg[markerIndex + 1].toInt() and 0xFF) shl 8) or
                        (jpeg[markerIndex + 2].toInt() and 0xFF)
                    if (length < 2) return null
                    val end = markerIndex + 1 + length
                    if (end > jpeg.size) return null
                    if (marker == APP1 || marker == APP13) {
                        segments.add(markerIndex - 1 until end)
                    }
                    index = end
                }
            }
        }
        // SOS/EOI 없이 끝난 불완전 JPEG
        return null
    }
}
