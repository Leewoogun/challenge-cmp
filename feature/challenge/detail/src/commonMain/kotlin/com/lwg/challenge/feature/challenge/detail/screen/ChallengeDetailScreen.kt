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
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeSide
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractUserInfo
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationResult
import com.lwg.challenge.feature.challenge.detail.component.ForfeitText
import com.lwg.challenge.feature.challenge.detail.component.MissionCard
import com.lwg.challenge.feature.challenge.detail.component.OathSummaryCard
import com.lwg.challenge.feature.challenge.detail.component.RejectionReasonText
import com.lwg.challenge.feature.challenge.detail.component.VerificationPhoto
import com.lwg.challenge.feature.challenge.detail.component.VsHeaderCard
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.HeadlineDisplay
import com.lwg.challenge.feature.challenge.detail.contract.HeadlineTone
import com.lwg.challenge.feature.challenge.detail.contract.MyMissionSlot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val VERIFY_CTA_TEXT = "인증하기"
private const val RETAKE_CTA_TEXT = "다시 찍기"

private val PREVIEW_VERIFIED_AT = LocalDateTime(2026, 1, 1, 9, 30, 0)
private val PREVIEW_SIGNED_AT = LocalDateTime(2025, 12, 31, 21, 0, 0)

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
    val myVerification = uiState.verifications?.myVerification
    val opponentVerification = uiState.verifications?.opponentVerification

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
            myProfileImageUrl = uiState.detail.me.profileImageUrl,
            opponentNickname = uiState.detail.opponent.nickname,
            opponentProfileImageUrl = uiState.detail.opponent.profileImageUrl,
            headlineText = uiState.headline.text,
            headlineTone = uiState.headline.tone,
            forfeitText = uiState.forfeitNoticeText,
        )

        MissionCard(
            label = uiState.myMissionLabel,
            mission = uiState.detail.me.mission,
            status = myVerification?.status,
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
                            status = myVerification?.status,
                            photoUrl = myVerification?.photoUrl,
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
                            status = myVerification?.status,
                            photoUrl = myVerification?.photoUrl,
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
            mission = uiState.detail.opponent.mission,
            status = opponentVerification?.status,
            footerSpacing = PhotoFooterSpacing,
            footer = {
                VerificationPhoto(
                    status = opponentVerification?.status,
                    photoUrl = opponentVerification?.photoUrl,
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
            betContent = uiState.detail.betContent,
            deadlineText = uiState.deadlineAbsoluteText,
            contract = uiState.detail.contract,
            mySignatureHint = uiState.mySignatureHint,
            opponentSignatureHint = uiState.opponentSignatureHint,
            myLabel = uiState.myLabel,
            opponentLabel = uiState.detail.opponent.nickname,
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

/** IN_PROGRESS · 내가 인증 완료 — 내 사진 아래에 Outlined "다시 찍기" 가 붙는다. */
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

/** 마감 후 · 양측 인증 완료 — 두 카드 모두 사진만 있고 재촬영 버튼이 없다. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 인증 현황 조회 실패 — 뱃지가 빠지고 사진 영역은 조회 실패 문구, CTA 는 열려 있다. */
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

/** 내 인증 실패 · 마감까지 미제출 — 진행 중이어도 하단 슬롯이 빠지고 실패 뱃지만 남는다. */
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

/** 내 인증 실패 · AI 반려 — 내 사진 아래 반려 사유가 붙고 재촬영 CTA 는 없다. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 상대 인증 AI 반려 — 상대 카드는 슬롯 분기 없이 붙어 자리가 달라 따로 확인한다. 긴 사유는 줄바꿈된다. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 계약서 없음 — 서명 블록만 빠진다. */
@Preview
@Composable
private fun ChallengeDetailScreenNoContractPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(contract = null),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 마감 후 — "판정 대기 중" NEUTRAL, 내 사진만 남는다. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 승리. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 패배 — 내가 미제출이라 하단 슬롯이 빠진다. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 무승부. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 양측 패배. */
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
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 서명을 읽지 못한 칸 — "서명 대기 중" 이 아니라 실패 문구가 나온다. */
@Preview
@Composable
private fun ChallengeDetailScreenUnreadableSignaturePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                opponentSignature = null,
                opponentSignedAt = PREVIEW_SIGNED_AT,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 긴 미션 + 큰 폰트 — 첫 화면 안에 CTA 가 남는지 확인한다. */
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

/** IN_PROGRESS · 내 미인증 — 인증 CTA 와 본문 맨 아래 기권 진입점이 함께 있다. */
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

/** 기권 요청 중 — 진입점만 잠긴다. */
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

/** 판정 완료 · 내가 기권 — "패배" 아래 "내가 기권했어요", 기권 진입점은 사라진다. */
@Preview
@Composable
private fun ChallengeDetailScreenForfeitedByMePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "패배",
                headlineTone = HeadlineTone.LOSE,
                forfeitedBy = ChallengeSide.ME,
                verifications = previewVerifications(
                    opponentStatus = VerificationStatus.VERIFIED,
                    opponentPhotoUrl = "https://example.com/theirs.jpg",
                ),
                status = "COMPLETED",
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 판정 완료 · 상대가 기권 — "승리" 아래 "우건님이 기권했어요". */
@Preview
@Composable
private fun ChallengeDetailScreenForfeitedByOpponentPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                headlineText = "승리",
                headlineTone = HeadlineTone.WIN,
                forfeitedBy = ChallengeSide.OPPONENT,
                status = "COMPLETED",
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
        // KMP @Preview 에는 fontScale 파라미터가 없어 밀도를 직접 바꾼다.
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

private fun previewVerifications(
    myStatus: VerificationStatus = VerificationStatus.PENDING,
    myPhotoUrl: String? = null,
    myAiReason: String? = null,
    opponentStatus: VerificationStatus = VerificationStatus.PENDING,
    opponentPhotoUrl: String? = null,
    opponentAiReason: String? = null,
) = ChallengeVerifications(
    challengeId = 1L,
    myVerification = VerificationResult(
        status = myStatus,
        photoUrl = myPhotoUrl,
        verifiedAt = PREVIEW_VERIFIED_AT,
        photoDeleted = false,
        aiReason = myAiReason,
    ),
    opponentVerification = VerificationResult(
        status = opponentStatus,
        photoUrl = opponentPhotoUrl,
        verifiedAt = PREVIEW_VERIFIED_AT,
        photoDeleted = false,
        aiReason = opponentAiReason,
    ),
)

private fun previewData(
    myMission: String? = "책 30페이지 읽기",
    opponentMission: String? = "오늘 운동 1시간 하기",
    opponentSignature: Signature? = previewSignature(),
    opponentSignedAt: LocalDateTime? = PREVIEW_SIGNED_AT,
    contract: Contract? = Contract(
        content = "영혼의 맹세",
        isFinalized = true,
        mySignature = previewSignature(),
        mySignedAt = PREVIEW_SIGNED_AT,
        opponentSignature = opponentSignature,
        opponentSignedAt = opponentSignedAt,
    ),
    status: String = "IN_PROGRESS",
    forfeitedBy: ChallengeSide? = null,
    headlineText: String = "남은 시간: 5시간 32분",
    headlineTone: HeadlineTone = HeadlineTone.URGENT,
    verifications: ChallengeVerifications? = previewVerifications(),
    isForfeiting: Boolean = false,
) = ChallengeDetailUiState.Data(
    detail = ChallengeDetail(
        challengeId = 1L,
        status = status,
        myResult = null,
        forfeitedBy = forfeitedBy,
        challengeDate = LocalDate(2026, 7, 28),
        deadline = LocalDateTime(2026, 7, 29, 0, 0, 0),
        betContent = "커피 사기",
        me = ContractUserInfo(nickname = "나", profileImageUrl = null, mission = myMission),
        opponent = ContractUserInfo(nickname = "우건", profileImageUrl = null, mission = opponentMission),
        contract = contract,
    ),
    verifications = verifications,
    headline = HeadlineDisplay(text = headlineText, tone = headlineTone),
    isForfeiting = isForfeiting,
)
