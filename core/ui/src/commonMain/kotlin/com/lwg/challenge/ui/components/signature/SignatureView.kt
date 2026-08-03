package com.lwg.challenge.ui.components.signature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 저장된 서명을 그려 보여주기만 하는 뷰(입력 없음).
 *
 * 챌린지 상세처럼 이미 확정된 계약의 양측 서명을 보여줄 때 쓴다.
 * 렌더 로직은 [SignatureCanvas] 와 공유하므로 그리는 결과와 보이는 결과가 같다.
 *
 * 정규화 좌표는 캔버스 비율에 의존한다. 입력과 렌더의 비율이 다르면 서명이 왜곡되므로
 * **컴포넌트가 비율(2:1)을 고정한다.** 호출부는 폭만 정한다. 획 굵기도 같은 이유로 폭에
 * 비례시켜 내부에서 계산한다 — 절반 폭으로 놓아도 획이 상대적으로 굵어지지 않는다.
 */
@Composable
fun SignatureView(
    signature: Signature,
    modifier: Modifier = Modifier,
    strokeColor: Color = ChallengeTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier.aspectRatio(SIGNATURE_ASPECT_RATIO)) {
        drawSignature(signature, strokeColor)
    }
}

@Preview
@Composable
private fun SignatureViewPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureView(
                signature = PreviewSignature,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 상세 2-up / 계약서 카드 안(≈158dp)의 절반 폭 렌더 — 전체 폭과 같은 서명으로 보여야 한다. */
@Preview
@Composable
private fun SignatureViewHalfWidthPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureView(
                signature = PreviewSignature,
                modifier = Modifier.fillMaxWidth(0.5f),
            )
        }
    }
}

@Preview
@Composable
private fun SignatureViewEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureView(
                signature = Signature.Empty,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
