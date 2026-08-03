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

// SignatureCanvas(그리기)와 SignatureView(렌더 전용)가 같은 그림을 그려야 하므로
// 렌더 로직은 여기 한 벌만 둔다. 두 벌이면 그리기 결과와 보기 결과가 갈라진다.

/**
 * 서명 캔버스의 가로:세로 비율. **입력·렌더 공통 불변식이다.**
 *
 * 정규화 좌표는 캔버스 비율에 의존한다. 입력과 렌더의 비율이 다르면 서명이 왜곡되므로
 * 컴포넌트가 비율을 고정한다. 호출부는 폭만 정한다.
 */
internal const val SIGNATURE_ASPECT_RATIO: Float = 2f

/**
 * 캔버스 폭 대비 획 굵기 비율.
 *
 * 굵기가 고정 dp 면 입력 캔버스(≈335dp)와 상세 렌더(≈158dp)에서 획의 상대 두께가 2배 어긋나
 * 작은 쪽 서명이 뭉갠다. 좌표는 폭에 비례해 스케일되는데 굵기만 절대값이면 비율이 깨지므로,
 * 굵기도 폭에 비례시켜 어느 크기에서도 같은 서명으로 보이게 한다.
 */
private const val STROKE_WIDTH_RATIO: Float = 0.009f

/** 폭 비례 굵기의 하한. 작은 렌더에서 획이 실선으로 안 보일 만큼 얇아지는 걸 막는다. */
private val MIN_STROKE_WIDTH: Dp = 1.5.dp

/**
 * 획 굵기. 호출부가 정하지 않는다 — 비율 불변식은 컴포넌트가 지킨다.
 *
 * 두 화면이 각자 굵기를 넘기면 같은 서명이 다르게 보이므로 [SIGNATURE_ASPECT_RATIO] 와
 * 마찬가지로 여기서만 계산한다. 수집부(`SignatureCanvas`)는 [DrawScope] 밖에서 굵기를
 * 알아야 하므로 폭을 인자로 받는 형태로 노출한다.
 */
internal fun Density.signatureStrokeWidthPx(canvasWidth: Float): Float =
    maxOf(canvasWidth * STROKE_WIDTH_RATIO, MIN_STROKE_WIDTH.toPx())

private fun DrawScope.strokeWidthPx(): Float = signatureStrokeWidthPx(size.width)

/**
 * 그리기 가능 영역이 캔버스 4변에서 파고드는 깊이 = 획 굵기의 절반.
 *
 * 좌표 `0`/[GRID] 는 캔버스 경계에 정확히 닿는데 [StrokeCap.Round] 는 획 중심선에서
 * 굵기/2 만큼 바깥으로 번진다. 인셋 없이 그리면 경계에 붙은 획의 번짐 절반이 캔버스 밖으로
 * 나가 잘려 보인다. 4변을 굵기/2 만큼 좁혀 번짐까지 캔버스 안에 들어오게 한다.
 *
 * **비율([STROKE_WIDTH_RATIO])이 아니라 clamp 를 마친 실제 굵기에서 뽑는다.** 상세 렌더
 * (≈158dp)에서는 0.9% 가 1.42dp 라 [MIN_STROKE_WIDTH] 로 올라가므로, 비율로 계산한 인셋은
 * 실제 번짐보다 얕아 여전히 잘린다.
 *
 * 인셋은 **화면 좌표계 문제**다 — 저장되는 좌표는 그대로 `0..`[GRID] 다. 대신 수집
 * ([toSignaturePoint])과 렌더([toOffset])가 **같은 인셋을 써서 정확히 역변환**해야 손가락을
 * 댄 위치와 획이 보이는 위치가 일치한다. 한쪽에만 인셋을 적용하면 렌더 인셋만 있을 땐 그리는
 * 위치와 보이는 위치가 어긋나고, 입력 패딩만 있을 땐 보이지 않는 벽에 눌려 가장자리 획이
 * 납작해진다. 그래서 두 변환 모두 이 함수 하나만 쓴다.
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

/**
 * 화면 좌표 → 도메인 좌표. [toOffset] 의 정확한 역변환이다.
 *
 * 인셋 영역(가장자리)을 터치하면 정규화 값이 음수/1 초과가 되지만 `coerceIn` 이 `0`/[GRID] 로
 * 잡는다 — 가장자리까지 그으면 경계에 붙은 획이 되는 게 자연스럽다. 그리기 가능 영역이 없을
 * 만큼 작은 캔버스에서는 0 나눗셈을 피해 `null` 을 돌려주고 수집을 무시한다.
 */
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
