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
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import kotlinx.datetime.LocalDateTime

/**
 * 계약 조건 + 서명을 한 장으로 압축한 계약서 카드.
 *
 * @param contract 계약서가 없는 챌린지(soul-oath 이전 생성)면 `null` 이고 서명 블록을 통째로 뺀다.
 */
@Composable
internal fun OathSummaryCard(
    betContent: String,
    deadlineText: String,
    contract: Contract?,
    mySignatureHint: String,
    opponentSignatureHint: String,
    myLabel: String,
    opponentLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
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
                    tint = ChallengeTheme.colorScheme.icon.point,
                )
                Text(
                    text = "영혼의 맹세",
                    style = ChallengeTheme.typography.bold14,
                    color = ChallengeTheme.colorScheme.text.primary,
                )
            }

            VerticalSpacer(12.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChallengeTheme.colorScheme.background.cardSub)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OathField(
                    label = "내기",
                    value = betContent,
                    valueColor = ChallengeTheme.colorScheme.text.point,
                )

                // 디자인에 없지만 의도적으로 둔 행이다. 상대 표기만으로는 마감 날짜를 알 수 없다.
                OathField(
                    label = "마감",
                    value = deadlineText,
                    valueColor = ChallengeTheme.colorScheme.text.primary,
                )

                if (contract != null) {
                    HorizontalDivider()

                    Column {
                        Text(
                            text = "서명",
                            style = ChallengeTheme.typography.medium10,
                            color = ChallengeTheme.colorScheme.text.secondary,
                        )

                        VerticalSpacer(8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SignatureColumn(
                                signature = contract.mySignature,
                                emptyHint = mySignatureHint,
                                label = myLabel,
                                modifier = Modifier.weight(1f),
                            )
                            SignatureColumn(
                                signature = contract.opponentSignature,
                                emptyHint = opponentSignatureHint,
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
            color = ChallengeTheme.colorScheme.text.secondary,
        )

        VerticalSpacer(4.dp)

        Text(
            text = value,
            style = ChallengeTheme.typography.medium14,
            color = valueColor,
        )
    }
}

@ThemePreviews
@Composable
private fun OathSummaryCardBothSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            OathSummaryCard(
                betContent = "커피 사기",
                deadlineText = "7월 28일 (월) 24:00",
                contract = oathPreviewContract(),
                mySignatureHint = "서명 대기 중",
                opponentSignatureHint = "서명 대기 중",
                myLabel = "나",
                opponentLabel = "우건",
            )
        }
    }
}

@ThemePreviews
@Composable
private fun OathSummaryCardOpponentNotSignedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            OathSummaryCard(
                betContent = "커피 사기",
                deadlineText = "7월 28일 (월) 24:00",
                contract = oathPreviewContract(opponentSignature = null),
                mySignatureHint = "서명 대기 중",
                opponentSignatureHint = "서명 대기 중",
                myLabel = "나",
                opponentLabel = "우건",
            )
        }
    }
}

/** 계약서 없음 — divider 와 서명 블록이 통째로 빠지고 내기·마감만 남는다. */
@ThemePreviews
@Composable
private fun OathSummaryCardNoContractPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            OathSummaryCard(
                betContent = "커피 사기",
                deadlineText = "7월 28일 (월) 24:00",
                contract = null,
                mySignatureHint = "서명 대기 중",
                opponentSignatureHint = "서명 대기 중",
                myLabel = "나",
                opponentLabel = "우건",
            )
        }
    }
}

private val OATH_PREVIEW_SIGNED_AT = LocalDateTime(2026, 7, 27, 21, 0, 0)

private fun oathPreviewContract(
    opponentSignature: Signature? = oathPreviewSignature(),
) = Contract(
    content = "영혼의 맹세",
    isFinalized = opponentSignature != null,
    mySignature = oathPreviewSignature(),
    mySignedAt = OATH_PREVIEW_SIGNED_AT,
    opponentSignature = opponentSignature,
    opponentSignedAt = OATH_PREVIEW_SIGNED_AT.takeIf { opponentSignature != null },
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
