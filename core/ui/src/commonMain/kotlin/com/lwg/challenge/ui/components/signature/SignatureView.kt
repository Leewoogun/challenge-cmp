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

/** 저장된 서명을 그려 보여주기만 하는 뷰(입력 없음). */
@Composable
fun SignatureView(
    signature: Signature,
    modifier: Modifier = Modifier,
    strokeColor: Color = ChallengeTheme.colorScheme.point,
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
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureView(
                signature = PreviewSignature,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun SignatureViewHalfWidthPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
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
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureView(
                signature = Signature.Empty,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
