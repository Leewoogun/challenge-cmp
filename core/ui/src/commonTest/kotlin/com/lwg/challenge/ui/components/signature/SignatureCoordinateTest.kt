package com.lwg.challenge.ui.components.signature

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.lwg.challenge.domain.model.signature.SignatureLimits.GRID
import com.lwg.challenge.domain.model.signature.SignaturePoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 수집과 렌더가 같은 인셋을 쓰는지 검증한다 — 깨지면 손가락 위치와 획이 어긋난다. */
class SignatureCoordinateTest {

    // 입력 캔버스(2:1)를 px 로 흉내낸 값. 굵기는 폭의 0.9% 라 인셋은 2.7px.
    private val canvasSize = Size(width = 600f, height = 300f)
    private val strokeWidthPx = 5.4f

    @Test
    fun `경계 점은 인셋만큼 안쪽에 찍힌다`() {
        val topLeft = SignaturePoint(0, 0).toOffset(canvasSize, strokeWidthPx)
        val bottomRight = SignaturePoint(GRID, GRID).toOffset(canvasSize, strokeWidthPx)

        assertEquals(2.7f, topLeft.x, absoluteTolerance = 0.01f)
        assertEquals(2.7f, topLeft.y, absoluteTolerance = 0.01f)
        assertEquals(597.3f, bottomRight.x, absoluteTolerance = 0.01f)
        assertEquals(297.3f, bottomRight.y, absoluteTolerance = 0.01f)
    }

    @Test
    fun `경계 점을 포함한 왕복이 원래 점으로 돌아온다`() {
        val points = listOf(
            SignaturePoint(0, 0),
            SignaturePoint(GRID, GRID),
            SignaturePoint(0, GRID),
            SignaturePoint(GRID, 0),
            SignaturePoint(500, 500),
            SignaturePoint(137, 862),
        )

        points.forEach { point ->
            val roundTripped = point
                .toOffset(canvasSize, strokeWidthPx)
                .toSignaturePoint(canvasSize, strokeWidthPx)

            assertEquals(point, roundTripped)
        }
    }

    @Test
    fun `인셋 바깥 터치는 경계 좌표로 잡힌다`() {
        val beyondTopLeft = Offset(0f, 0f).toSignaturePoint(canvasSize, strokeWidthPx)
        val beyondBottomRight = Offset(600f, 300f).toSignaturePoint(canvasSize, strokeWidthPx)

        assertEquals(SignaturePoint(0, 0), beyondTopLeft)
        assertEquals(SignaturePoint(GRID, GRID), beyondBottomRight)
    }

    @Test
    fun `그리기 영역이 없을 만큼 작은 캔버스는 수집하지 않는다`() {
        val tooSmall = Size(width = 4f, height = 2f)

        assertNull(Offset(2f, 1f).toSignaturePoint(tooSmall, strokeWidthPx))
    }
}
