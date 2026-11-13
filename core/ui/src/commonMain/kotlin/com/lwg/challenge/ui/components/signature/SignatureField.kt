package com.lwg.challenge.ui.components.signature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.HorizontalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature

/** 서명 입력란 한 세트 — 배경 · 빈 상태 안내 · 캔버스 · "다시 그리기". */
@Composable
fun SignatureField(
    signature: Signature,
    onSignatureChange: (Signature) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emptyHint: String = "여기에 서명하세요",
    onDrawingChange: (Boolean) -> Unit = {},
) {
    val isEmpty = signature.isEmpty
    val hintColor = ChallengeTheme.colorScheme.text.secondary

    Column(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SIGNATURE_SLOT_SHAPE)
                .background(ChallengeTheme.colorScheme.background.field)
                // 획이 하나라도 생기면 안내 테두리를 지운다 — 그은 획을 가리기 때문.
                .then(
                    if (isEmpty) {
                        Modifier.signatureDashedBorder(hintColor.copy(alpha = 0.30f))
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            SignatureCanvas(
                signature = signature,
                onSignatureChange = onSignatureChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onDrawingChange = onDrawingChange,
            )

            if (isEmpty) {
                Text(
                    text = emptyHint,
                    style = ChallengeTheme.typography.medium14,
                    color = hintColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (!isEmpty) {
            TextButton(
                onClick = { onSignatureChange(Signature.Empty) },
                modifier = Modifier.align(Alignment.End),
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = ChallengeTheme.colorScheme.icon.secondary,
                )

                HorizontalSpacer(4.dp)

                Text(
                    text = "다시 그리기",
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.text.secondary,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SignatureFieldEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureField(
                signature = Signature.Empty,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SignatureFieldSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureField(
                signature = PreviewSignature,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SignatureFieldDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureField(
                signature = PreviewSignature,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SignatureFieldLongHintPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            SignatureField(
                signature = Signature.Empty,
                onSignatureChange = {},
                modifier = Modifier.fillMaxWidth(),
                emptyHint = "여기에 손가락으로 서명하세요. 서명하면 되돌릴 수 없습니다.",
            )
        }
    }
}
