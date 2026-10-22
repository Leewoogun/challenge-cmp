package com.lwg.challenge.feature.challenge.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.detail.component.ForfeitText
import com.lwg.challenge.feature.challenge.detail.component.MissionCard
import com.lwg.challenge.feature.challenge.detail.component.OathSummaryCard
import com.lwg.challenge.feature.challenge.detail.component.RejectionReasonText
import com.lwg.challenge.feature.challenge.detail.component.VerificationPhoto
import com.lwg.challenge.feature.challenge.detail.component.VsHeaderCard
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.HeadlineTone
import com.lwg.challenge.feature.challenge.detail.contract.MyMissionSlot
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import com.lwg.challenge.feature.challenge.detail.contract.VerificationSection
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val VERIFY_CTA_TEXT = "인증하기"
private const val RETAKE_CTA_TEXT = "다시 찍기"

private val PREVIEW_VERIFIED_AT = LocalDateTime(2026, 1, 1, 9, 30, 0)

private val CtaFooterSpacing = 16.dp
private val PhotoFooterSpacing = 12.dp

private val RejectionReasonSpacing = 8.dp

@Composable
internal fun ChallengeDetailScreen(
    uiState: ChallengeDetailUiState.Data,
    onVerifyClick: () -> Unit,
    onForfeitClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        VsHeaderCard(
            myNickname = uiState.myLabel,
            opponentNickname = uiState.opponentNickname,
            headlineText = uiState.headlineText,
            headlineTone = uiState.headlineTone,
            forfeitText = uiState.forfeitNoticeText,
        )

        MissionCard(
            label = uiState.myMissionLabel,
            mission = uiState.myMission,
            status = uiState.myVerificationStatus,
            footerSpacing = when (uiState.myMissionSlot) {
                MyMissionSlot.MY_PHOTO,
                MyMissionSlot.MY_PHOTO_WITH_RETAKE,
                -> PhotoFooterSpacing

                MyMissionSlot.VERIFY_CTA, MyMissionSlot.NONE -> CtaFooterSpacing
            },
            footer = when (uiState.myMissionSlot) {
                MyMissionSlot.VERIFY_CTA -> {
                    {
                        IconTextButton(
                            text = VERIFY_CTA_TEXT,
                            icon = Icons.Filled.PhotoCamera,
                            onClick = onVerifyClick,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = ChallengeTheme.typography.bold16,
                        )
                    }
                }

                MyMissionSlot.MY_PHOTO -> {
                    {
                        VerificationPhoto(
                            status = uiState.myVerificationStatus,
                            photoUrl = uiState.myPhotoUrl,
                            cacheKey = uiState.myPhotoCacheKey,
                            photoDeleted = uiState.myPhotoDeleted,
                        )
                        uiState.myAiReasonText?.let { reasonText ->
                            VerticalSpacer(RejectionReasonSpacing)
                            RejectionReasonText(text = reasonText)
                        }
                    }
                }
                MyMissionSlot.MY_PHOTO_WITH_RETAKE -> {
                    {
                        VerificationPhoto(
                            status = uiState.myVerificationStatus,
                            photoUrl = uiState.myPhotoUrl,
                            cacheKey = uiState.myPhotoCacheKey,
                            photoDeleted = uiState.myPhotoDeleted,
                        )
                        VerticalSpacer(12.dp)
                        IconTextButton(
                            text = RETAKE_CTA_TEXT,
                            icon = Icons.Filled.PhotoCamera,
                            onClick = onVerifyClick,
                            modifier = Modifier.fillMaxWidth(),
                            style = IconTextButtonStyle.Outlined,
                            textStyle = ChallengeTheme.typography.bold16,
                        )
                    }
                }

                MyMissionSlot.NONE -> null
            },
        )

        MissionCard(
            label = uiState.opponentMissionLabel,
            mission = uiState.opponentMission,
            status = uiState.opponentVerificationStatus,
            footerSpacing = PhotoFooterSpacing,
            footer = {
                VerificationPhoto(
                    status = uiState.opponentVerificationStatus,
                    photoUrl = uiState.opponentPhotoUrl,
                    cacheKey = uiState.opponentPhotoCacheKey,
                    photoDeleted = uiState.opponentPhotoDeleted,
                    onRetry = onRetry,
                )
                uiState.opponentAiReasonText?.let { reasonText ->
                    VerticalSpacer(RejectionReasonSpacing)
                    RejectionReasonText(text = reasonText)
                }
            },
        )

        OathSummaryCard(
            betContent = uiState.betContent,
            deadlineText = uiState.deadlineAbsoluteText,
            signatures = uiState.signatures,
            myLabel = uiState.myLabel,
            opponentLabel = uiState.opponentLabel,
        )
        if (uiState.canForfeit) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ForfeitText(
                    onClick = onForfeitClick,
                    enabled = !uiState.isForfeiting,
                )
            }
        }
    }
}

/** IN_PROGRESS / 양측 미인증 — 내 카드에 인증 CTA, 상대 카드에 "아직 인증하지 않았어요". */
@Preview
@Composable
private fun ChallengeDetailScreenPendingPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * IN_PROGRESS · 내가 인증 완료 — 이번 변경의 핵심 장면. 완료 뱃지와 내 사진은 그대로 남고 그 아래에
 * 보조 강조(Outlined)의 "다시 찍기" 가 붙는다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenVerifiedWithRetakePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                verifications = previewVerifications(
                    myStatus = VerificationStatus.VERIFIED,
                    myPhotoUrl = "https://example.com/mine.jpg",
                ),
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 마감 후 · 양측 인증 완료 — 교체가 닫혀 두 카드 모두 사진만 들고 재촬영 버튼이 없다. */
@Preview
@Composable
private fun ChallengeDetailScreenBothVerifiedPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                verifications = previewVerifications(
                    myStatus = VerificationStatus.VERIFIED,
                    myPhotoUrl = "https://example.com/mine.jpg",
                    opponentStatus = VerificationStatus.VERIFIED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * 인증 현황 조회 실패 — 뱃지는 빠지고 사진 영역은 "인증 현황을 불러오지 못했어요" 로 강등되지만
 * CTA 는 그대로 열려 있다(진행 중이면 열린다 — 내 인증 상태를 보지 않는다).
 */
@Preview
@Composable
private fun ChallengeDetailScreenVerificationUnknownPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(verifications = null),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * 내 인증 실패 · **마감까지 미제출** — 낼 사진이 없어 하단 슬롯이 통째로 빠지고 실패 뱃지만 남는다.
 *
 * 진입점(`canVerify`)을 일부러 열어 둔 채 확인한다 — 내 인증 상태가 게이트보다 먼저 걸러진다는 게
 * 이 장면의 요지다. 사진이 있는 실패는 아래 [ChallengeDetailScreenMyVerificationRejectedPreview] 다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenMyVerificationFailedPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                verifications = previewVerifications(myStatus = VerificationStatus.FAILED),
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * 내 인증 실패 · **AI 반려** — 같은 `FAILED` 인데 내가 올린 사진이 그대로 남는다. 위
 * [ChallengeDetailScreenMyVerificationFailedPreview] 와 가르는 것은 사진 유무뿐이다.
 *
 * 재촬영 CTA 가 붙지 않아야 한다 — 반려는 자정 판정에서 확정되고 그 시점이 곧 마감이라 챌린지가
 * 이미 닫혀 있다. `canVerify = false` 가 그 사실을 옮긴 것이다.
 *
 * 사진 아래에 반려 사유가 한 단계 낮은 위계로 붙는다. 사유가 실려 왔다는 것 자체가 미인증 처리됐다는
 * 뜻이라 상태를 함께 보지 않는다
 * ([com.lwg.challenge.feature.challenge.detail.contract.VerificationSection.myAiReason] 참조).
 * 상대 칸에 붙는 모양은 아래 [ChallengeDetailScreenOpponentVerificationRejectedPreview] 다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenMyVerificationRejectedPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "패배",
                headlineTone = HeadlineTone.LOSE,
                verifications = previewVerifications(
                    myStatus = VerificationStatus.FAILED,
                    myPhotoUrl = "https://example.com/mine.jpg",
                    myAiReason = "책상 위 커피잔 사진으로, 미션 수행 증거로 보기 어렵습니다.",
                    opponentStatus = VerificationStatus.VERIFIED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * 상대 인증이 **AI 반려** — 같은 사유 한 줄이 상대 카드의 사진 아래에 붙는다.
 *
 * 위 [ChallengeDetailScreenMyVerificationRejectedPreview] 와 짝으로 둔다. 내 카드는 슬롯 분기를
 * 거쳐, 상대 카드는 분기 없이 사진 다음에 바로 붙어 **그리는 자리가 서로 다르기** 때문에 두 장을
 * 모두 눈으로 확인해야 한다(렌더링을 덮는 UI 테스트가 없다).
 *
 * 사유가 길어 줄바꿈되는 모양도 여기서 드러난다 — 자르지 않는 것이 이 문구의 규칙이다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenOpponentVerificationRejectedPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "승리",
                headlineTone = HeadlineTone.WIN,
                verifications = previewVerifications(
                    myStatus = VerificationStatus.VERIFIED,
                    myPhotoUrl = "https://example.com/mine.jpg",
                    opponentStatus = VerificationStatus.FAILED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                    opponentAiReason = "실내에서 찍힌 책상 위 커피잔 사진으로 보이며, 미션으로 약속한 " +
                        "야외 달리기를 수행했다고 볼 만한 장면이 확인되지 않습니다.",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 계약서 없음(soul-oath 이전 생성) — 내기·마감은 그대로 나오고 서명 블록만 divider 째로 빠진다. */
@Preview
@Composable
private fun ChallengeDetailScreenNoContractPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(signatures = null),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 마감 후 — 헤더가 "판정 대기 중" NEUTRAL 로 내려가고, 인증은 마쳤지만 교체가 닫혀 사진만 남는다. */
@Preview
@Composable
private fun ChallengeDetailScreenAfterDeadlinePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "판정 대기 중",
                headlineTone = HeadlineTone.NEUTRAL,
                verifications = previewVerifications(
                    myStatus = VerificationStatus.VERIFIED,
                    myPhotoUrl = "https://example.com/mine.jpg",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 승리 — 헤더가 트로피 primary 로 올라서고, 양측 사진은 남고 CTA 는 닫혀 있다. */
@Preview
@Composable
private fun ChallengeDetailScreenWinPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "승리",
                headlineTone = HeadlineTone.WIN,
                verifications = previewVerifications(
                    myStatus = VerificationStatus.VERIFIED,
                    myPhotoUrl = "https://example.com/mine.jpg",
                    opponentStatus = VerificationStatus.VERIFIED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 패배 — 내가 미제출로 FAILED 라 하단 슬롯이 통째로 빠지고 상대만 사진을 든다. */
@Preview
@Composable
private fun ChallengeDetailScreenLosePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "패배",
                headlineTone = HeadlineTone.LOSE,
                verifications = previewVerifications(
                    myStatus = VerificationStatus.FAILED,
                    opponentStatus = VerificationStatus.VERIFIED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 무승부 — 양측 VERIFIED 인데도 승자가 없는 장면. 헤더만 중립 onBackground 로 남는다. */
@Preview
@Composable
private fun ChallengeDetailScreenDrawPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "무승부",
                headlineTone = HeadlineTone.DRAW,
                verifications = previewVerifications(
                    myStatus = VerificationStatus.VERIFIED,
                    myPhotoUrl = "https://example.com/mine.jpg",
                    opponentStatus = VerificationStatus.VERIFIED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 양측 패배 — 두 사진 영역이 모두 안내 문구로 내려앉고 헤더만 결과를 말한다. */
@Preview
@Composable
private fun ChallengeDetailScreenBothLosePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "양측 패배",
                headlineTone = HeadlineTone.LOSE,
                verifications = previewVerifications(
                    myStatus = VerificationStatus.FAILED,
                    opponentStatus = VerificationStatus.FAILED,
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 서명을 읽지 못한 칸 — "서명 대기 중" 이 아니라 실패를 밝힌다(서명은 한 사람이다). */
@Preview
@Composable
private fun ChallengeDetailScreenUnreadableSignaturePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                opponentSignature = null,
                opponentSignatureHint = "서명을 불러오지 못했어요",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 긴 미션 + 큰 폰트 — 첫 화면 안에 CTA 가 남는지 확인하는 자리다. */
@Preview
@Composable
private fun ChallengeDetailScreenLongMissionLargeFontPreview() {
    PreviewContainer(fontScale = 1.5f) {
        ChallengeDetailScreen(
            uiState = previewData(
                myMission = "매일 아침 6시에 일어나서 집 앞 공원 세 바퀴를 뛰고 인증 사진을 찍어 올리기",
                opponentMission = "퇴근 후 카페에 들러 두 시간 동안 자격증 인강을 듣고 필기 노트 남기기",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * IN_PROGRESS · 내 미인증 — 이번 변경의 핵심 장면. 내 카드의 인증 CTA 와 본문 맨 아래 기권 진입점이
 * 함께 있다. 기권이 주 액션 옆이 아니라 스크롤 끝에 있다는 것이 여기서 보인다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenForfeitEntryPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 기권 요청 중 — 진입점이 잠긴다. 전체 화면 로딩으로 덮지 않아 읽던 계약서가 그대로 남는다. */
@Preview
@Composable
private fun ChallengeDetailScreenForfeitingPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(isForfeiting = true),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * 판정 완료 · 내가 기권 — 헤드라인 "패배" 아래에 "내가 기권했어요" 가 한 단계 낮은 위계로 붙고,
 * 하단 진입점은 사라진다.
 *
 * 기권 진입점과 "다시 찍기" 가 함께 나오지 않는다는 것을 눈으로 확인하는 자리이기도 하다 — 여기서는
 * 둘 다 없고, 위 [ChallengeDetailScreenForfeitEntryPreview] 에는 기권만, "다시 찍기" 프리뷰에는
 * 재촬영만 있다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenForfeitedByMePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "패배",
                headlineTone = HeadlineTone.LOSE,
                forfeitNoticeText = "내가 기권했어요",
                verifications = previewVerifications(
                    opponentStatus = VerificationStatus.VERIFIED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                ),
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/**
 * 판정 완료 · 상대가 기권 — 헤드라인 "승리" 아래에 "우건님이 기권했어요". 내가 인증했는지와 무관하게
 * 이긴 장면이라 내 카드는 인증 전 그대로다.
 *
 * 바로 위 프리뷰와 짝으로, 기권이 걸린 화면에는 "다시 찍기" 가 나올 자리가 없다는 것을 보여준다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenForfeitedByOpponentPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "승리",
                headlineTone = HeadlineTone.WIN,
                forfeitNoticeText = "우건님이 기권했어요",
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

@Composable
private fun PreviewContainer(
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    ChallengeTheme {
        // KMP @Preview 에는 안드로이드 전용 어노테이션의 fontScale 파라미터가 없어 밀도를 직접 바꾼다.
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = LocalDensity.current.density,
                fontScale = fontScale,
            ),
        ) {
            Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
                content()
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

private fun previewVerifications(
    myStatus: VerificationStatus = VerificationStatus.PENDING,
    myPhotoUrl: String? = null,
    myPhotoDeleted: Boolean = false,
    myVerifiedAt: LocalDateTime? = PREVIEW_VERIFIED_AT,
    myAiReason: String? = null,
    opponentStatus: VerificationStatus = VerificationStatus.PENDING,
    opponentPhotoUrl: String? = null,
    opponentPhotoDeleted: Boolean = false,
    opponentVerifiedAt: LocalDateTime? = PREVIEW_VERIFIED_AT,
    opponentAiReason: String? = null,
) = VerificationSection(
    myStatus = myStatus,
    myPhotoUrl = myPhotoUrl,
    myPhotoDeleted = myPhotoDeleted,
    myVerifiedAt = myVerifiedAt,
    myAiReason = myAiReason,
    opponentStatus = opponentStatus,
    opponentPhotoUrl = opponentPhotoUrl,
    opponentPhotoDeleted = opponentPhotoDeleted,
    opponentVerifiedAt = opponentVerifiedAt,
    opponentAiReason = opponentAiReason,
)

private fun previewData(
    myMission: String? = "책 30페이지 읽기",
    opponentMission: String? = "오늘 운동 1시간 하기",
    opponentSignature: Signature? = previewSignature(),
    opponentSignatureHint: String = "서명 대기 중",
    isFinalized: Boolean = true,
    signatures: SignatureSection? = previewSignatures(
        opponentSignature = opponentSignature,
        opponentSignatureHint = opponentSignatureHint,
        isFinalized = isFinalized,
    ),
    headlineText: String = "남은 시간: 5시간 32분",
    headlineTone: HeadlineTone = HeadlineTone.URGENT,
    verifications: VerificationSection? = previewVerifications(),
    canVerify: Boolean = true,
    forfeitNoticeText: String? = null,
    isForfeiting: Boolean = false,
) = ChallengeDetailUiState.Data(
    myMission = myMission,
    opponentNickname = "우건",
    opponentMission = opponentMission,
    betContent = "커피 사기",
    headlineText = headlineText,
    headlineTone = headlineTone,
    deadlineAbsoluteText = "7월 28일 (월) 24:00",
    signatures = signatures,
    verifications = verifications,
    canVerify = canVerify,
    forfeitNoticeText = forfeitNoticeText,
    isForfeiting = isForfeiting,
)
