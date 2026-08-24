package com.lwg.challenge.camera

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompressionSpecTest {

    @Test
    fun `initial 에서 시작해 step 씩 내려가 min 으로 끝난다`() {
        val spec = spec(initialQuality = 90, minQuality = 60, qualityStep = 10)

        assertEquals(listOf(90, 80, 70, 60), spec.qualitySteps())
    }

    @Test
    fun `step 이 딱 안 떨어져도 min 이 포함된다`() {
        val spec = spec(initialQuality = 85, minQuality = 50, qualityStep = 10)

        assertEquals(listOf(85, 75, 65, 55, 50), spec.qualitySteps())
    }

    @Test
    fun `min 아래로 내려가지 않는다`() {
        val spec = spec(initialQuality = 85, minQuality = 50, qualityStep = 10)

        assertTrue(spec.qualitySteps().all { it >= spec.minQuality })
    }

    @Test
    fun `내림차순이다`() {
        val steps = spec(initialQuality = 85, minQuality = 50, qualityStep = 10).qualitySteps()

        assertEquals(steps.sortedDescending(), steps)
    }

    @Test
    fun `initial 과 min 이 같으면 한 번만 시도한다`() {
        val spec = spec(initialQuality = 70, minQuality = 70, qualityStep = 10)

        assertEquals(listOf(70), spec.qualitySteps())
    }

    private fun spec(initialQuality: Int, minQuality: Int, qualityStep: Int) = CompressionSpec(
        maxDimensionPx = 1440,
        targetBytes = 500 * 1024,
        initialQuality = initialQuality,
        minQuality = minQuality,
        qualityStep = qualityStep,
    )
}
