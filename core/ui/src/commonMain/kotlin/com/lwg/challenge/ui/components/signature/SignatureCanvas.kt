package com.lwg.challenge.ui.components.signature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignatureLimits.GRID
import com.lwg.challenge.domain.model.signature.SignatureLimits.MAX_POINTS
import com.lwg.challenge.domain.model.signature.SignatureLimits.MAX_STROKES
import com.lwg.challenge.domain.model.signature.SignatureLimits.MIN_POINT_DISTANCE
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 손가락으로 서명을 그리는 캔버스. */
@Composable
fun SignatureCanvas(
    signature: Signature,
    onSignatureChange: (Signature) -> Unit,
    modifier: Modifier = Modifier,
    strokeColor: Color = ChallengeTheme.colorScheme.point,
    enabled: Boolean = true,
    onDrawingChange: (Boolean) -> Unit = {},
) {
    val canvasSize = remember { mutableStateOf(IntSize.Zero) }
    val latestSignature = rememberUpdatedState(signature)
    val latestOnChange = rememberUpdatedState(onSignatureChange)
    val latestOnDrawingChange = rememberUpdatedState(onDrawingChange)
    val activeStroke = remember { ActiveStroke() }

    Canvas(
        modifier = modifier
            .aspectRatio(SIGNATURE_ASPECT_RATIO)
            .onSizeChanged { canvasSize.value = it }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    latestOnDrawingChange.value(true)
                    activeStroke.begin(
                        point = quantize(down.position, canvasSize.value),
                        strokeCount = latestSignature.value.strokeCount,
                        budget = MAX_POINTS - latestSignature.value.pointCount,
                    )
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        // consume 된 change 는 이동량이 Zero 라 판정을 consume 뒤로 미루면 획이 점 1개로 남는다.
                        val moved = change.positionChanged()
                        change.consume()
                        if (!change.pressed) break
                        if (moved) {
                            activeStroke.extend(
                                point = quantize(change.position, canvasSize.value),
                                budget = MAX_POINTS - latestSignature.value.pointCount,
                            )
                        }
                    }
                    val finished = activeStroke.finish(
                        budget = MAX_POINTS - latestSignature.value.pointCount,
                    )
                    latestOnDrawingChange.value(false)
                    if (finished != null) {
                        val current = latestSignature.value
                        latestOnChange.value(
                            Signature(current.strokes + SignatureStroke(finished)),
                        )
                    }
                }
            },
    ) {
        drawSignature(latestSignature.value, strokeColor)
        drawStroke(activeStroke.points, strokeColor)
    }
}

/** 확정 전 획 버퍼. 호출부가 콜백을 점 단위가 아니라 획 단위로 받게 한다. */
private class ActiveStroke {

    val points: SnapshotStateList<SignaturePoint> = mutableStateListOf()

    /** 간축으로 버려진 마지막 점. 끝점 보존에 쓴다. */
    private var droppedTail: SignaturePoint? = null

    private var isRecording = false

    /** 캔버스 크기를 아직 모르거나 상한에 걸리면 이 제스처를 통째로 무시한다. */
    fun begin(point: SignaturePoint?, strokeCount: Int, budget: Int) {
        reset()
        if (point == null || strokeCount >= MAX_STROKES || budget <= 0) return
        isRecording = true
        points += point
    }

    fun extend(point: SignaturePoint?, budget: Int) {
        if (!isRecording || point == null) return
        if (!point.isFarEnoughFrom(points.last())) {
            droppedTail = point
            return
        }
        if (points.size >= budget) return
        points += point
        droppedTail = null
    }

    /** 확정된 획을 돌려주고 버퍼를 비운다. 기록 중이 아니었거나 빈 획이면 `null`. */
    fun finish(budget: Int): List<SignaturePoint>? {
        // 끝점이 간축에 걸려 잘리면 획이 실제보다 짧아 보인다.
        droppedTail?.let { if (points.size < budget) points += it }
        val finished = points.toList().takeIf { isRecording && it.isNotEmpty() }
        reset()
        return finished
    }

    private fun reset() {
        points.clear()
        droppedTail = null
        isRecording = false
    }
}

private fun SignaturePoint.isFarEnoughFrom(other: SignaturePoint): Boolean {
    val dx = x - other.x
    val dy = y - other.y
    return dx * dx + dy * dy >= MIN_POINT_DISTANCE * MIN_POINT_DISTANCE
}

/** 캔버스 px 좌표를 `0..`[GRID] 정규화 좌표로 양자화한다. 크기를 아직 모르면 `null`. */
private fun Density.quantize(position: Offset, canvasSize: IntSize): SignaturePoint? {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) return null
    val size = Size(canvasSize.width.toFloat(), canvasSize.height.toFloat())
    return position.toSignaturePoint(size, signatureStrokeWidthPx(size.width))
}

@Preview
@Composable
private fun SignatureCanvasEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureCanvas(
                signature = Signature.Empty,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun SignatureCanvasSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureCanvas(
                signature = PreviewSignature,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun SignatureCanvasDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureCanvas(
                signature = PreviewSignature,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
                strokeColor = ChallengeTheme.colorScheme.text.secondary,
                enabled = false,
            )
        }
    }
}
