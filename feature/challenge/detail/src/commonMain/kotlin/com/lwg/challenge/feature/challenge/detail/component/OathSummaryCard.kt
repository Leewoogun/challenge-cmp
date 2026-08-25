package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.HorizontalDivider
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약 조건 + 서명을 한 장으로 압축한 계약서 카드.
 *
 * @param signatures 계약서가 없는 챌린지(soul-oath 이전 생성)면 `null` — 없는 맹세를 "서명 대기 중"
 *   으로 그리면 안 되므로 divider 째로 서명 블록을 통째로 생략한다.
 * @param deadlineText 절대 마감 시각. 포맷은 ViewModel 이 확정한다.
 */
@Composable
internal fun OathSummaryCard(
    betContent: String,
    deadlineText: String,
    signatures: SignatureSection?,
    myLabel: String,
    opponentLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ChallengeTheme.colorScheme.primary,
                )
                Text(
                    text = "영혼의 맹세",
                    style = ChallengeTheme.typography.bold14,
                    color = ChallengeTheme.colorScheme.onBackground,
                )
            }

            VerticalSpacer(12.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChallengeTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OathField(
                    label = "내기",
                    value = betContent,
                    valueColor = ChallengeTheme.colorScheme.primary,
                )

                // Lovable 원본(challenge-detail.tsx)의 내부 박스는 '내기' 한 필드뿐이라 이 행은 디자인에
                // 없는 의도적 이탈이다(pm-lead 판정으로 존치). VS 헤더의 상대 표기("남은 시간: 3시간")
                // 만으로는 마감이 오늘 자정인지 내일인지 복원할 수 없는데, 그건 계약 조건이라 계약서가
                // 맡아야 한다. "디자인에 없는 행" 이라는 이유로 지우지 말 것.
                OathField(
                    label = "마감",
                    value = deadlineText,
                    valueColor = ChallengeTheme.colorScheme.onBackground,
                )

                if (signatures != null) {
                    HorizontalDivider()

                    Column {
                        Text(
                            text = "서명",
                            style = ChallengeTheme.typography.medium10,
                            color = ChallengeTheme.colorScheme.onSurfaceVariant,
                        )

                        VerticalSpacer(8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SignatureColumn(
                                signature = signatures.mySignature,
                                emptyHint = signatures.mySignatureHint,
                                label = myLabel,
                                modifier = Modifier.weight(1f),
                            )
                            SignatureColumn(
                                signature = signatures.opponentSignature,
                                emptyHint = signatures.opponentSignatureHint,
                                label = opponentLabel,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OathField(
    label: String,
    value: String,
    valueColor: Color,
) {
    Column {
        Text(
            text = label,
            style = ChallengeTheme.typography.medium10,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )

        VerticalSpacer(4.dp)

        Text(
            text = value,
            style = ChallengeTheme.typography.medium14,
            color = valueColor,
        )
    }
}

@Preview
@Composable
private fun OathSummaryCardBothSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            OathSummaryCard(
                betContent = "커피 사기",
                deadlineText = "7월 28일 (월) 24:00",
                signatures = oathPreviewSignatures(),
                myLabel = "나",
                opponentLabel = "우건",
            )
        }
    }
}

@Preview
@Composable
private fun OathSummaryCardOpponentNotSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            OathSummaryCard(
                betContent = "커피 사기",
                deadlineText = "7월 28일 (월) 24:00",
                signatures = oathPreviewSignatures(opponentSignature = null),
                myLabel = "나",
                opponentLabel = "우건",
            )
        }
    }
}

/** 계약서 없음 — divider 와 서명 블록이 통째로 빠지고 내기·마감만 남는다. */
@Preview
@Composable
private fun OathSummaryCardNoContractPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            OathSummaryCard(
                betContent = "커피 사기",
                deadlineText = "7월 28일 (월) 24:00",
                signatures = null,
                myLabel = "나",
                opponentLabel = "우건",
            )
        }
    }
}

private fun oathPreviewSignatures(
    opponentSignature: Signature? = oathPreviewSignature(),
) = SignatureSection(
    mySignature = oathPreviewSignature(),
    mySignatureHint = "서명 대기 중",
    opponentSignature = opponentSignature,
    opponentSignatureHint = "서명 대기 중",
    isFinalized = opponentSignature != null,
)

private fun oathPreviewSignature() = Signature(
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
