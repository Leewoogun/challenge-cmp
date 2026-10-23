package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.ui.components.signature.SignatureSlot
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 서명 칸 1개 + 그 아래 소유자 라벨. 라벨이 서명 획과 겹치지 않게 칸 밖에 둔다.
 *
 * @param emptyHint [signature] 가 `null` 일 때 칸 안에 뜨는 문구.
 */
@Composable
internal fun SignatureColumn(
    signature: Signature?,
    emptyHint: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SignatureSlot(
            signature = signature,
            modifier = Modifier.fillMaxWidth(),
            emptyHint = emptyHint,
        )

        VerticalSpacer(4.dp)

        Text(
            text = label,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun SignatureColumnSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureColumn(
                signature = previewSignature(),
                emptyHint = "서명 대기 중",
                label = "나",
            )
        }
    }
}

@Preview
@Composable
private fun SignatureColumnNotSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureColumn(
                signature = null,
                emptyHint = "서명 대기 중",
                label = "우건",
            )
        }
    }
}

@Preview
@Composable
private fun SignatureColumnUnreadablePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SignatureColumn(
                signature = null,
                emptyHint = "서명을 불러오지 못했어요",
                label = "우건",
            )
        }
    }
}

/** 실제 배치인 2-up. 좌우 칸의 높이가 서명 유무와 무관하게 같은지 확인한다. */
@Preview
@Composable
private fun SignatureColumnPairPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SignatureColumn(
                    signature = previewSignature(),
                    emptyHint = "서명 대기 중",
                    label = "나",
                    modifier = Modifier.weight(1f),
                )
                SignatureColumn(
                    signature = null,
                    emptyHint = "서명 대기 중",
                    label = "우건",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun previewSignature() = Signature(
    strokes = listOf(
        SignatureStroke(
            listOf(
                SignaturePoint(120, 620),
                SignaturePoint(300, 360),
                SignaturePoint(480, 640),
                SignaturePoint(700, 400),
            ),
        ),
    ),
)
