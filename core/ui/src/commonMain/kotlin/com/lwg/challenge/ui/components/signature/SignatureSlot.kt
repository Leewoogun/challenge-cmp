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
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature

private val SLOT_CORNER_RADIUS = 12.dp

/** 서명 한 칸의 껍데기 모서리. 입력란과 슬롯이 같은 값을 써야 왕복해도 같은 칸으로 보인다. */
internal val SIGNATURE_SLOT_SHAPE = RoundedCornerShape(SLOT_CORNER_RADIUS)

/** 읽기 전용 서명 칸. 미서명·디코드 실패는 시각이 같아 `null` + [emptyHint] 로만 가른다. */
@Composable
fun SignatureSlot(
    signature: Signature?,
    modifier: Modifier = Modifier,
    emptyHint: String = "서명 대기 중",
) {
    val hintColor = ChallengeTheme.colorScheme.text.secondary

    Box(
        modifier = modifier
            .clip(SIGNATURE_SLOT_SHAPE)
            .background(ChallengeTheme.colorScheme.background.field)
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
        // 서명이 없어도 빈 캔버스로 높이를 확보한다 — 2-up 배치에서 좌우 칸 높이가 어긋나지 않게.
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

@ThemePreviews
@Composable
private fun SignatureSlotSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureSlot(
                signature = PreviewSignature,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SignatureSlotEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureSlot(
                signature = null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SignatureSlotHalfWidthPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
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
