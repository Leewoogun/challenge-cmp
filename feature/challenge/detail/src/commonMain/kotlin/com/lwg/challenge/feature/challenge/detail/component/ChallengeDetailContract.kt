package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import com.lwg.challenge.ui.components.ContractCard
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 확정된 계약서 + 양측 서명. 읽기 전용이다.
 *
 * 본문 전체를 `verticalScroll` 로 감싸도 안전하다 — 서명은 [com.lwg.challenge.ui.components.signature.SignatureView]
 * 로만 렌더되고 `pointerInput` 이 없어서, 입력 화면과 달리 드래그가 "획 긋기"와 경합하지 않는다.
 *
 * `contract.content` 문자열은 그리지 않는다. 계약서는 구조화된 필드로 조립한다.
 *
 * 계약 조건 행은 계약서 유무와 무관하게(챌린지 최상위 필드에서 오므로) 항상 그린다. 계약서가 없으면
 * [ChallengeDetailUiState.Data.signatures] 가 null 이라 `footer` 를 넘기지 않게 되고,
 * [ContractCard] 가 divider 까지 통째로 생략한다.
 */
@Composable
internal fun ChallengeDetailContract(
    uiState: ChallengeDetailUiState.Data,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 20.dp),
    ) {
        ContractCard(
            opponentNickname = uiState.opponentNickname,
            myMission = uiState.myMission,
            betContent = uiState.betContent,
            deadlineText = uiState.deadlineText,
            opponentMission = uiState.opponentMission,
            myMissionLabel = uiState.myMissionLabel,
            footer = uiState.signatures?.let { signatures ->
                {
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
                            label = uiState.myLabel,
                            modifier = Modifier.weight(1f),
                        )
                        SignatureColumn(
                            signature = signatures.opponentSignature,
                            emptyHint = signatures.opponentSignatureHint,
                            label = uiState.opponentLabel,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun ChallengeDetailContractBothSignedPreview() {
    PreviewContainer {
        ChallengeDetailContract(uiState = previewData())
    }
}

@Preview
@Composable
private fun ChallengeDetailContractOpponentNotSignedPreview() {
    PreviewContainer {
        ChallengeDetailContract(
            uiState = previewData(
                opponentSignature = null,
                opponentSignatureHint = "서명 대기 중",
                isFinalized = false,
            ),
        )
    }
}

@Preview
@Composable
private fun ChallengeDetailContractUnreadablePreview() {
    PreviewContainer {
        ChallengeDetailContract(
            uiState = previewData(
                opponentSignature = null,
                opponentSignatureHint = "서명을 불러오지 못했어요",
            ),
        )
    }
}

/** PENDING — 상대가 아직 미션을 걸지 않아 상대 미션 행이 통째로 빠진다. */
@Preview
@Composable
private fun ChallengeDetailContractOpponentMissionMissingPreview() {
    PreviewContainer {
        ChallengeDetailContract(
            uiState = previewData(
                opponentMission = null,
                opponentSignature = null,
                opponentSignatureHint = "서명 대기 중",
                isFinalized = false,
            ),
        )
    }
}

/** 관점 미상 — 라벨이 "나" 가 아니라 챌린저 실명으로 나온다. */
@Preview
@Composable
private fun ChallengeDetailContractUnknownPerspectivePreview() {
    PreviewContainer {
        ChallengeDetailContract(uiState = previewData(myLabel = "우건"))
    }
}

/** 계약서 없음(soul-oath 이전 생성) — 계약 조건은 그대로 나오고 서명 블록만 divider 째로 빠진다. */
@Preview
@Composable
private fun ChallengeDetailContractNoContractPreview() {
    PreviewContainer {
        ChallengeDetailContract(uiState = previewData(signatures = null))
    }
}

@Composable
private fun PreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            content()
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

private fun previewSignatures(
    opponentSignature: Signature? = previewSignature(),
    opponentSignatureHint: String = "서명 대기 중",
    isFinalized: Boolean = true,
) = SignatureSection(
    mySignature = previewSignature(),
    mySignatureHint = "서명 대기 중",
    opponentSignature = opponentSignature,
    opponentSignatureHint = opponentSignatureHint,
    isFinalized = isFinalized,
)

private fun previewData(
    myLabel: String = "나",
    opponentMission: String? = "오늘 운동 1시간 하기",
    opponentSignature: Signature? = previewSignature(),
    opponentSignatureHint: String = "서명 대기 중",
    isFinalized: Boolean = true,
    signatures: SignatureSection? = previewSignatures(
        opponentSignature = opponentSignature,
        opponentSignatureHint = opponentSignatureHint,
        isFinalized = isFinalized,
    ),
) = ChallengeDetailUiState.Data(
    myLabel = myLabel,
    myMissionLabel = "${myLabel}의 미션",
    myMission = "책 30페이지 읽기",
    opponentNickname = "우건",
    opponentMission = opponentMission,
    betContent = "커피 사기",
    deadlineText = "5시간 32분",
    signatures = signatures,
)
