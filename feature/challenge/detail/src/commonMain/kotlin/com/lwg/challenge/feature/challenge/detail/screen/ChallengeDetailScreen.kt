package com.lwg.challenge.feature.challenge.detail.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.detail.component.SignatureColumn
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
internal fun ChallengeDetailScreen(
    uiState: ChallengeDetailUiState.Data,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
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

        // 계약서가 길어도 인증 액션이 스크롤에 밀려 사라지지 않도록 스크롤 밖에 고정한다.
        if (uiState.canVerify) {
            IconTextButton(
                text = "인증하기",
                icon = Icons.Filled.PhotoCamera,
                onClick = onVerifyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                textStyle = ChallengeTheme.typography.bold16,
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeDetailScreenBothSignedPreview() {
    PreviewContainer {
        ChallengeDetailScreen(uiState = previewData(), onVerifyClick = {})
    }
}

@Preview
@Composable
private fun ChallengeDetailScreenOpponentNotSignedPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                opponentSignature = null,
                opponentSignatureHint = "서명 대기 중",
                isFinalized = false,
            ),
            onVerifyClick = {},
        )
    }
}

@Preview
@Composable
private fun ChallengeDetailScreenUnreadablePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                opponentSignature = null,
                opponentSignatureHint = "서명을 불러오지 못했어요",
            ),
            onVerifyClick = {},
        )
    }
}

/** PENDING — 상대가 아직 미션을 걸지 않아 상대 미션 행이 통째로 빠진다. */
@Preview
@Composable
private fun ChallengeDetailScreenOpponentMissionMissingPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                opponentMission = null,
                opponentSignature = null,
                opponentSignatureHint = "서명 대기 중",
                isFinalized = false,
            ),
            onVerifyClick = {},
        )
    }
}

/** 관점 미상 — 라벨이 "나" 가 아니라 챌린저 실명으로 나온다. */
@Preview
@Composable
private fun ChallengeDetailScreenUnknownPerspectivePreview() {
    PreviewContainer {
        ChallengeDetailScreen(uiState = previewData(myLabel = "우건"), onVerifyClick = {})
    }
}

/** 계약서 없음(soul-oath 이전 생성) — 계약 조건은 그대로 나오고 서명 블록만 divider 째로 빠진다. */
@Preview
@Composable
private fun ChallengeDetailScreenNoContractPreview() {
    PreviewContainer {
        ChallengeDetailScreen(uiState = previewData(signatures = null), onVerifyClick = {})
    }
}

/** IN_PROGRESS — 인증 CTA 가 스크롤 밖 하단에 고정되어 나온다. */
@Preview
@Composable
private fun ChallengeDetailScreenCanVerifyPreview() {
    PreviewContainer {
        ChallengeDetailScreen(uiState = previewData(canVerify = true), onVerifyClick = {})
    }
}

/** 비-IN_PROGRESS — 인증 CTA 가 나오지 않는다. */
@Preview
@Composable
private fun ChallengeDetailScreenCannotVerifyPreview() {
    PreviewContainer {
        ChallengeDetailScreen(uiState = previewData(canVerify = false), onVerifyClick = {})
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
    canVerify: Boolean = false,
) = ChallengeDetailUiState.Data(
    myLabel = myLabel,
    myMissionLabel = "${myLabel}의 미션",
    myMission = "책 30페이지 읽기",
    opponentNickname = "우건",
    opponentMission = opponentMission,
    betContent = "커피 사기",
    deadlineText = "5시간 32분",
    signatures = signatures,
    canVerify = canVerify,
)
