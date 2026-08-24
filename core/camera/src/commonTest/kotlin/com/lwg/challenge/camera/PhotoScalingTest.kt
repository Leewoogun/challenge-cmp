package com.lwg.challenge.camera

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhotoScalingTest {

    @Test
    fun `축소가 필요 없는 작은 원본이면 sampleSize 는 1 이다`() {
        assertEquals(1, PhotoScaling.sampleSize(720, 480, 1440))
    }

    @Test
    fun `정확히 2배 큰 원본이면 sampleSize 는 2 다`() {
        assertEquals(2, PhotoScaling.sampleSize(2880, 2880, 1440))
    }

    @Test
    fun `정확히 4배 큰 원본이면 sampleSize 는 4 다`() {
        assertEquals(4, PhotoScaling.sampleSize(5760, 5760, 1440))
    }

    @Test
    fun `가로로 긴 원본은 긴 변 기준으로 sampleSize 를 정한다`() {
        assertEquals(2, PhotoScaling.sampleSize(4000, 1000, 1440))
    }

    @Test
    fun `세로로 긴 원본은 긴 변 기준으로 sampleSize 를 정한다`() {
        assertEquals(2, PhotoScaling.sampleSize(1000, 4000, 1440))
    }

    @Test
    fun `sampleSize 로 축소해도 maxDimension 아래로 떨어지지 않는다`() {
        val sample = PhotoScaling.sampleSize(2879, 2879, 1440)

        assertEquals(1, sample)
        assertTrue(2879 / sample >= 1440)
    }

    @Test
    fun `1픽셀 원본이면 sampleSize 는 1 이다`() {
        assertEquals(1, PhotoScaling.sampleSize(1, 1, 1440))
    }

    @Test
    fun `0 이하 입력이면 sampleSize 는 1 이다`() {
        assertEquals(1, PhotoScaling.sampleSize(0, 100, 1440))
        assertEquals(1, PhotoScaling.sampleSize(100, -1, 1440))
        assertEquals(1, PhotoScaling.sampleSize(100, 100, 0))
    }

    @Test
    fun `가로로 긴 원본은 종횡비를 유지하며 긴 변을 maxDimension 에 맞춘다`() {
        assertEquals(ScaledSize(1440, 720), PhotoScaling.targetSize(4000, 2000, 1440))
    }

    @Test
    fun `세로로 긴 원본은 종횡비를 유지하며 긴 변을 maxDimension 에 맞춘다`() {
        assertEquals(ScaledSize(720, 1440), PhotoScaling.targetSize(2000, 4000, 1440))
    }

    @Test
    fun `원본이 maxDimension 보다 작으면 확대하지 않는다`() {
        assertEquals(ScaledSize(800, 600), PhotoScaling.targetSize(800, 600, 1440))
    }

    @Test
    fun `원본 긴 변이 정확히 maxDimension 이면 원본 크기 그대로다`() {
        assertEquals(ScaledSize(1440, 1080), PhotoScaling.targetSize(1440, 1080, 1440))
    }

    @Test
    fun `극단적인 종횡비에서도 결과가 0 이 되지 않는다`() {
        assertEquals(ScaledSize(1440, 1), PhotoScaling.targetSize(10000, 1, 1440))
        assertEquals(ScaledSize(1, 1440), PhotoScaling.targetSize(1, 10000, 1440))
    }
}
