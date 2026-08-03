package com.lwg.challenge.ui.components.signature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import org.jetbrains.compose.ui.tooling.preview.Preview

private val SLOT_CORNER_RADIUS = 12.dp

/**
 * 서명 한 칸의 껍데기 모서리.
 *
 * [SignatureField] 의 입력란과 [SignatureSlot] 이 같은 값을 써야 입력 화면과 상세 화면을 왕복해도
 * 같은 칸으로 보인다. [signatureDashedBorder] 도 같은 반지름을 쓰므로 여기 한 벌만 둔다.
 */
internal val SIGNATURE_SLOT_SHAPE = RoundedCornerShape(SLOT_CORNER_RADIUS)

/**
 * 읽기 전용 서명 칸 — 배경 박스 + 서명 렌더 또는 빈 상태 안내.
 *
 * 서명이 없는 이유는 두 가지(미서명 / 디코드 실패)지만 **시각은 같고 문구만 다르다.** 그래서
 * 상태를 인자로 받지 않고 `null` + [emptyHint] 로 가른다 — "여기 서명이 안 보인다"는 시각 언어를
 * 한 번 배우면 두 경우에 다 통한다. 어느 쪽인지 판정해 문구를 고르는 건 호출부(ViewModel)의 몫이다.
 *
 * 높이는 지정하지 않는다 — [SignatureView] 가 비율(2:1)을 고정하므로 호출부는 폭만 정한다.
 */
@Composable
fun SignatureSlot(
    signature: Signature?,
    modifier: Modifier = Modifier,
    emptyHint: String = "서명 대기 중",
) {
    val hintColor = ChallengeTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(SIGNATURE_SLOT_SHAPE)
            .background(ChallengeTheme.colorScheme.secondary)
            // 획이 있으면 안내 테두리를 지운다 — 그은 획을 가리기 때문.
            .then(
                if (signature == null) {
                    Modifier.signatureDashedBorder(hintColor.copy(alpha = 0.30f))
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 서명이 없어도 빈 캔버스를 깔아 칸 높이를 확보한다 — 서명 유무로 칸 크기가 흔들리면
        // 2-up 배치에서 좌우 칸의 높이가 어긋난다.
        SignatureView(
            signature = signature ?: Signature.Empty,
            modifier = Modifier.fillMaxWidth(),
        )

        if (signature == null) {
            Text(
                text = emptyHint,
                style = ChallengeTheme.typography.medium12,
                color = hintColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

/** 빈 상태를 알리는 점선 테두리. 굵기의 절반만큼 안으로 밀어야 획이 잘리지 않는다. */
internal fun Modifier.signatureDashedBorder(color: Color): Modifier = drawBehind {
    val strokeWidth = 1.5.dp.toPx()
    val inset = strokeWidth / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = CornerRadius(SLOT_CORNER_RADIUS.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
            ),
        ),
    )
}

@Preview
@Composable
private fun SignatureSlotSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureSlot(
                signature = PreviewSignature,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun SignatureSlotEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureSlot(
                signature = null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 상세 화면의 2-up 배치 폭. 절반 폭에서도 안내 문구가 읽히는지 확인한다. */
@Preview
@Composable
private fun SignatureSlotHalfWidthPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureSlot(
                signature = null,
                modifier = Modifier.fillMaxWidth(0.5f),
                emptyHint = "서명을 불러오지 못했어요",
            )
        }
    }
}
