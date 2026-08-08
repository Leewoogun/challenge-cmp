package com.lwg.challenge.ui.components.signature

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignatureLimits.GRID
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import kotlin.math.roundToInt

// 그리기(SignatureCanvas)와 렌더(SignatureView)가 갈라지지 않도록 렌더 로직은 여기 한 벌만 둔다.

/** 서명 캔버스의 가로:세로 비율. 입력과 렌더가 다르면 서명이 왜곡되므로 컴포넌트가 고정한다. */
internal const val SIGNATURE_ASPECT_RATIO: Float = 2f

/** 캔버스 폭 대비 획 굵기 비율. 고정 dp 면 크기가 다른 화면끼리 상대 두께가 어긋난다. */
private const val STROKE_WIDTH_RATIO: Float = 0.009f

/** 폭 비례 굵기의 하한. 작은 렌더에서 획이 실선으로 안 보일 만큼 얇아지는 걸 막는다. */
private val MIN_STROKE_WIDTH: Dp = 1.5.dp

/** 획 굵기. 호출부가 정하지 않는다 — 비율 불변식은 컴포넌트가 지킨다. */
internal fun Density.signatureStrokeWidthPx(canvasWidth: Float): Float =
    maxOf(canvasWidth * STROKE_WIDTH_RATIO, MIN_STROKE_WIDTH.toPx())

private fun DrawScope.strokeWidthPx(): Float = signatureStrokeWidthPx(size.width)

/**
 * 그리기 영역이 캔버스 4변에서 파고드는 깊이. [StrokeCap.Round] 번짐이 밖으로 잘리는 걸 막는다.
 * 수집([toSignaturePoint])과 렌더([toOffset])가 같은 값을 써야 손가락 위치와 획이 일치한다.
 */
internal fun signatureInsetPx(strokeWidthPx: Float): Float = strokeWidthPx / 2f

/** 도메인 좌표 → 화면 좌표. 인셋만큼 안쪽으로 밀린 영역에 매핑한다. */
internal fun SignaturePoint.toOffset(canvasSize: Size, strokeWidthPx: Float): Offset {
    val inset = signatureInsetPx(strokeWidthPx)
    return Offset(
        x = inset + x / GRID.toFloat() * drawableExtent(canvasSize.width, inset),
        y = inset + y / GRID.toFloat() * drawableExtent(canvasSize.height, inset),
    )
}

/** 화면 좌표 → 도메인 좌표. [toOffset] 의 역변환. 그릴 영역이 없을 만큼 작은 캔버스면 `null`. */
internal fun Offset.toSignaturePoint(canvasSize: Size, strokeWidthPx: Float): SignaturePoint? {
    val inset = signatureInsetPx(strokeWidthPx)
    val drawableWidth = drawableExtent(canvasSize.width, inset)
    val drawableHeight = drawableExtent(canvasSize.height, inset)
    if (drawableWidth <= 0f || drawableHeight <= 0f) return null
    return SignaturePoint(
        x = ((x - inset) / drawableWidth * GRID).roundToInt().coerceIn(0, GRID),
        y = ((y - inset) / drawableHeight * GRID).roundToInt().coerceIn(0, GRID),
    )
}

private fun drawableExtent(extent: Float, inset: Float): Float =
    (extent - 2f * inset).coerceAtLeast(0f)

internal fun DrawScope.drawSignature(
    signature: Signature,
    color: Color,
) {
    signature.strokes.forEach { stroke -> drawStroke(stroke.points, color) }
}

internal fun DrawScope.drawStroke(
    points: List<SignaturePoint>,
    color: Color,
) {
    val strokeWidthPx = strokeWidthPx()
    when (points.size) {
        0 -> Unit
        // 탭으로 찍힌 1점짜리 획. Path 로는 아무것도 그려지지 않으므로 점을 찍는다.
        1 -> drawCircle(
            color = color,
            radius = strokeWidthPx / 2f,
            center = points.first().toOffset(size, strokeWidthPx),
        )

        else -> drawPath(
            path = points.toPath(size, strokeWidthPx),
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

private fun List<SignaturePoint>.toPath(canvasSize: Size, strokeWidthPx: Float): Path {
    val path = Path()
    val start = first().toOffset(canvasSize, strokeWidthPx)
    path.moveTo(start.x, start.y)
    for (index in 1 until size) {
        val next = this[index].toOffset(canvasSize, strokeWidthPx)
        path.lineTo(next.x, next.y)
    }
    return path
}

/** Preview 전용 더미 서명 — 대략 필기체 두 획 + 점 하나. */
internal val PreviewSignature: Signature = Signature(
    strokes = listOf(
        stroke(120 to 620, 200 to 380, 280 to 640, 360 to 360, 440 to 660),
        stroke(80 to 720, 300 to 700, 520 to 660, 700 to 600),
        stroke(820 to 400),
    ),
)

private fun stroke(vararg points: Pair<Int, Int>) =
    SignatureStroke(points.map { (x, y) -> SignaturePoint(x, y) })
