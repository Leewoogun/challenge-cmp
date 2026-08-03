package com.lwg.challenge.domain.model.signature

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignatureTest {

    private fun stroke(vararg points: Pair<Int, Int>) =
        SignatureStroke(points.map { (x, y) -> SignaturePoint(x, y) })

    @Test
    fun `Empty 는 빈 서명이다`() {
        assertTrue(Signature.Empty.isEmpty)
        assertEquals(0, Signature.Empty.strokeCount)
        assertEquals(0, Signature.Empty.pointCount)
    }

    @Test
    fun `점이 하나도 없는 획만 있으면 빈 서명이다`() {
        val signature = Signature(listOf(SignatureStroke(emptyList()), SignatureStroke(emptyList())))

        assertTrue(signature.isEmpty)
        assertEquals(2, signature.strokeCount)
        assertEquals(0, signature.pointCount)
    }

    @Test
    fun `점이 하나라도 있으면 빈 서명이 아니다`() {
        val signature = Signature(listOf(SignatureStroke(emptyList()), stroke(10 to 20)))

        assertFalse(signature.isEmpty)
    }

    @Test
    fun `pointCount 는 모든 획의 점 개수 합이다`() {
        val signature = Signature(
            listOf(
                stroke(0 to 0, 10 to 10, 20 to 20),
                stroke(100 to 100, 110 to 110),
                stroke(500 to 500),
            ),
        )

        assertEquals(3, signature.strokeCount)
        assertEquals(6, signature.pointCount)
    }
}
