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

/** 프리뷰 사진 캐시 키가 실행 시점 시계에 흔들리지 않게 고정한다. */
private val PREVIEW_VERIFIED_AT = LocalDateTime(2026, 1, 1, 9, 30, 0)

/** CTA 는 버튼 자체가 여백을 가져서 사진(12dp)보다 위 간격을 넉넉히 준다. */
private val CtaFooterSpacing = 16.dp
private val PhotoFooterSpacing = 12.dp

/**
 * 상세 본문. VS 헤더 → 나의 미션 → 상대 미션 → 영혼의 맹세 순으로 한 스크롤에 쌓는다.
 *
 * 본문 전체를 `verticalScroll` 로 감싸도 안전하다 — 서명은
 * [com.lwg.challenge.ui.components.signature.SignatureView] 로만 렌더되고 `pointerInput` 이 없어서,
 * 입력 화면과 달리 드래그가 "획 긋기"와 경합하지 않는다.
 *
 * 계약서의 `content` 문자열은 그리지 않는다. 계약 조건은 구조화된 필드로 조립한다.
 *
 * @param onRetry 전체 재조회. 인증 현황만 다시 받는 경로가 없어 상세까지 함께 다시 받는다.
 */
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
                // 인증 CTA 는 스크롤 밖 고정에서 이 카드 안으로 들어왔다. 재구성으로 "나의 미션" 이
                // VS 헤더 바로 다음에 와 진입 시 첫 화면에 들어오고, 계약서는 아래로 내려갔다.
                // 카드 안에 두면 어느 미션에 대한 인증인지가 버튼과 붙는 이득도 함께 얻는다.
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
                        // 이미 인증을 마친 카드라 재촬영은 주 액션이 아니다. Filled 로 두면 완료 표시보다
                        // 버튼이 더 강조돼 "아직 인증이 안 됐나" 로 읽힌다.
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
            },
        )

        OathSummaryCard(
            betContent = uiState.betContent,
            // VS 헤더가 이미 상대 표기를 들고 있어, 계약서까지 "남은 시간" 이면 마감이 오늘 자정인지
            // 내일인지 화면 어디에서도 복원되지 않는다. 계약 조건 쪽은 절대 시각으로 못 박는다.
            deadlineText = uiState.deadlineAbsoluteText,
            signatures = uiState.signatures,
            myLabel = uiState.myLabel,
            opponentLabel = uiState.opponentLabel,
        )

        // 인증 CTA 바로 아래가 아니라 본문 맨 아래에 둔다 — 되돌릴 수 없는 행위를 주 액션(인증하기)
        // 바로 옆에 놓으면 오탭 거리가 0이 된다. 기권은 자주 쓰는 길이 아니라 찾아 내려가야 하는
        // 출구다. 마이페이지가 회원 탈퇴를 화면 맨 아래에 둔 것과 같은 배치 판단이다.
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

/** 내 인증 실패 — 다시 인증할 수 없어 하단 슬롯이 통째로 빠지고 실패 뱃지만 남는다. */
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

/** 판정 완료 · 패배 — 내 인증이 FAILED 라 내 카드 하단 슬롯이 통째로 빠지고 상대만 사진을 든다. */
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

/**
 * 관점 미상 + 판정 완료 — 승패를 내 것으로 말할 수 없어 실명("우건 승리")으로 내려가고, 톤도 축하색
 * 대신 중립으로 남는다(진 사람에게 승리색을 보여주지 않는다). 다만 결과는 이미 났으므로 아이콘은
 * 판정 대기 중의 시계가 아니라 트로피다.
 */
@Preview
@Composable
private fun ChallengeDetailScreenUnknownPerspectiveResultPreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(
                myLabel = "지훈",
                headlineText = "우건 승리",
                headlineTone = HeadlineTone.DECIDED_NEUTRAL,
                verifications = null,
                canVerify = false,
            ),
            onVerifyClick = {},
            onForfeitClick = {},
            onRetry = {},
        )
    }
}

/** 관점 미상 — 라벨이 "나" 가 아니라 챌린저 실명으로 나오고, 어느 칸이 내 것인지 몰라 현황도 빠진다. */
@Preview
@Composable
private fun ChallengeDetailScreenUnknownPerspectivePreview() {
    PreviewContainer {
        ChallengeDetailScreen(
            uiState = previewData(myLabel = "지훈", verifications = null),
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
    opponentStatus: VerificationStatus = VerificationStatus.PENDING,
    opponentPhotoUrl: String? = null,
    opponentPhotoDeleted: Boolean = false,
    opponentVerifiedAt: LocalDateTime? = PREVIEW_VERIFIED_AT,
) = VerificationSection(
    myStatus = myStatus,
    myPhotoUrl = myPhotoUrl,
    myPhotoDeleted = myPhotoDeleted,
    myVerifiedAt = myVerifiedAt,
    opponentStatus = opponentStatus,
    opponentPhotoUrl = opponentPhotoUrl,
    opponentPhotoDeleted = opponentPhotoDeleted,
    opponentVerifiedAt = opponentVerifiedAt,
)

private fun previewData(
    myLabel: String = "나",
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
    myLabel = myLabel,
    myMissionLabel = "${myLabel}의 미션",
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
