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
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 서명 입력란 한 세트 — 배경 · 빈 상태 안내 · 캔버스 · "다시 그리기".
 *
 * [SignatureCanvas] 는 좌표 수집과 렌더만 하고 배경/테두리를 그리지 않으므로 그 껍데기를 여기서 씌운다.
 * 위저드 맹세 step 과 상대 맹세 화면이 같은 입력란을 써야 해서 `:core:ui` 에 둔다.
 *
 * 높이는 지정하지 않는다 — [SignatureCanvas] 가 비율(2:1)을 고정하므로 호출부는 폭만 정한다.
 */
@Composable
fun SignatureField(
    signature: Signature,
    onSignatureChange: (Signature) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emptyHint: String = "여기에 서명하세요",
) {
    val isEmpty = signature.isEmpty
    val hintColor = ChallengeTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SIGNATURE_SLOT_SHAPE)
                .background(ChallengeTheme.colorScheme.secondary)
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

        // 빈 상태에서는 지울 게 없다 — 자리조차 차지하지 않도록 아예 그리지 않는다.
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
                    tint = ChallengeTheme.colorScheme.onSurfaceVariant,
                )

                HorizontalSpacer(4.dp)

                Text(
                    text = "다시 그리기",
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SignatureFieldEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
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

@Preview
@Composable
private fun SignatureFieldSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
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

@Preview
@Composable
private fun SignatureFieldDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
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

@Preview
@Composable
private fun SignatureFieldLongHintPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
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
