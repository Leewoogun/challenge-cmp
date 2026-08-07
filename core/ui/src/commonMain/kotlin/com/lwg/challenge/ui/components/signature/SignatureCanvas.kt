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

/**
 * 손가락으로 서명을 그리는 캔버스.
 *
 * 상태는 호출부가 소유한다 — 그리는 중인 획만 내부 버퍼에 두고, 손을 떼는 시점에
 * [onSignatureChange] 로 한 번 확정한다. "다시 그리기"는 호출부가
 * [Signature.Empty] 를 넣으면 된다.
 *
 * 좌표는 캔버스 크기로 정규화한 뒤 `0..`[GRID] 로 양자화해 담는다. 손실은 이 한 번뿐이라
 * 이후 저장/복원 왕복은 무손실이다. 정규화 기준 영역은 캔버스 전체가 아니라 4변을
 * [signatureInsetPx] 만큼 좁힌 영역이며, 렌더도 같은 인셋을 쓴다.
 *
 * [MAX_POINTS] / [MAX_STROKES] 를 넘는 입력은 **조용히 무시**한다(예외 없음).
 *
 * 캔버스가 스크롤 컨테이너 안에 놓이면 [onDrawingChange] 로 획을 긋는 동안을 알려준다.
 * 포인터를 매 이벤트 consume 하므로 부모 스크롤이 제스처를 가져가지 않지만, 호출부가
 * 스크롤 자체를 잠글 수 있게 신호를 열어 둔다.
 *
 * 정규화 좌표는 캔버스 비율에 의존한다. 입력과 렌더의 비율이 다르면 서명이 왜곡되므로
 * **컴포넌트가 비율(2:1)을 고정한다.** 호출부는 폭만 정한다. 획 굵기도 같은 이유로 폭에
 * 비례시켜 내부에서 계산한다 — 호출부가 정하면 [SignatureView] 와 어긋난다.
 *
 * 배경/테두리는 그리지 않는다.
 */
@Composable
fun SignatureCanvas(
    signature: Signature,
    onSignatureChange: (Signature) -> Unit,
    modifier: Modifier = Modifier,
    strokeColor: Color = ChallengeTheme.colorScheme.primary,
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
                        // positionChange() 는 consume 된 change 에 Offset.Zero 를 돌려준다.
                        // 따라서 이동 여부는 반드시 consume 전에 판정해야 한다 — 순서가 뒤집히면
                        // extend() 가 한 번도 호출되지 않아 획이 점 1개로 남는다 (2026-08-03 실기 회귀).
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

/**
 * 확정 전 획 버퍼.
 *
 * 확정된 서명과 분리해 둬야 호출부가 변경 콜백을 받는 횟수가 점 단위가 아니라 획 단위가 된다.
 */
private class ActiveStroke {

    /** 그리는 중인 획. 스냅샷 리스트라 점이 늘 때마다 Canvas 가 다시 그려진다. */
    val points: SnapshotStateList<SignaturePoint> = mutableStateListOf()

    /** 간축으로 버려진 마지막 점. 끝점 보존에 쓴다. */
    private var droppedTail: SignaturePoint? = null

    private var isRecording = false

    /** 캔버스 크기를 아직 모르거나(`point == null`) 상한에 걸리면 이 제스처를 통째로 무시한다. */
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

/**
 * 캔버스 px 좌표를 `0..`[GRID] 정규화 좌표로 양자화한다. 크기를 아직 모르면 `null`.
 *
 * 인셋 계산이 렌더와 갈라지면 그리는 위치와 보이는 위치가 어긋나므로, 굵기·인셋·역변환을
 * 직접 계산하지 않고 렌더와 같은 [toSignaturePoint] 한 벌에 위임한다.
 */
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
                .background(ChallengeTheme.colorScheme.background)
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
                .background(ChallengeTheme.colorScheme.background)
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
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureCanvas(
                signature = PreviewSignature,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
                strokeColor = ChallengeTheme.colorScheme.onSurfaceVariant,
                enabled = false,
            )
        }
    }
}
