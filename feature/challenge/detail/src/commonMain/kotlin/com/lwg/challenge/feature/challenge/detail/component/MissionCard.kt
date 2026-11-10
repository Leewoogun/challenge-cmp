package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.feature.challenge.detail.contract.VERIFICATION_REJECTED_PREFIX
import com.lwg.challenge.ui.components.VerificationStatusPill
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val EMPTY_MISSION_TEXT = "—"

/**
 * 한쪽 진영의 미션 카드. 나/상대는 [label] 과 [footer] 로만 구분한다.
 *
 * @param mission 아직 맹세하지 않았으면 `null`. 카드 높이가 튀지 않게 자리표시자를 둔다.
 * @param status 인증 현황을 못 받았으면 `null` 이고 뱃지를 뺀다. 아무 값이나 고르면 모르는 사실을 단정한다.
 * @param footerSpacing [footer] 위 여백. CTA(16dp)와 사진(12dp)이 달라 호출부가 정한다.
 */
@Composable
internal fun MissionCard(
    label: String,
    mission: String?,
    status: VerificationStatus?,
    modifier: Modifier = Modifier,
    footerSpacing: Dp = 16.dp,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
                if (status != null) {
                    VerificationStatusPill(status = status)
                }
            }

            VerticalSpacer(8.dp)

            Text(
                text = mission ?: EMPTY_MISSION_TEXT,
                style = ChallengeTheme.typography.bold16,
                color = if (mission == null) {
                    ChallengeTheme.colorScheme.onSurfaceVariant
                } else {
                    ChallengeTheme.colorScheme.onBackground
                },
            )

            if (footer != null) {
                VerticalSpacer(footerSpacing)
                Column(content = footer)
            }
        }
    }
}

/** 나 / IN_PROGRESS — 인증 CTA 가 슬롯에 들어간다. */
@Preview
@Composable
private fun MissionCardMineWithCtaPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MissionCard(
                label = "나의 미션",
                mission = "오늘 운동 1시간 하기",
                status = VerificationStatus.PENDING,
                footer = {
                    IconTextButton(
                        text = "인증하기",
                        icon = Icons.Filled.PhotoCamera,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = ChallengeTheme.typography.bold16,
                    )
                },
            )
        }
    }
}

/** 나 / 인증 완료 — CTA 자리에 내 인증 사진이 들어간다(슬롯 여백 12dp). */
@Preview
@Composable
private fun MissionCardMineWithPhotoPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MissionCard(
                label = "나의 미션",
                mission = "오늘 운동 1시간 하기",
                status = VerificationStatus.VERIFIED,
                footerSpacing = 12.dp,
                footer = {
                    VerificationPhoto(
                        status = VerificationStatus.VERIFIED,
                        photoUrl = null,
                        cacheKey = null,
                        isLoading = true,
                    )
                },
            )
        }
    }
}

/** 나 / 인증 완료 + 재촬영 가능 — 사진 아래에 보조 강조(Outlined)의 "다시 찍기" 가 하나 더 붙는다. */
@Preview
@Composable
private fun MissionCardMineWithPhotoAndRetakePreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MissionCard(
                label = "나의 미션",
                mission = "오늘 운동 1시간 하기",
                status = VerificationStatus.VERIFIED,
                footerSpacing = 12.dp,
                footer = {
                    VerificationPhoto(
                        status = VerificationStatus.VERIFIED,
                        photoUrl = null,
                        cacheKey = null,
                        isLoading = true,
                    )
                    VerticalSpacer(12.dp)
                    IconTextButton(
                        text = "다시 찍기",
                        icon = Icons.Filled.PhotoCamera,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        style = IconTextButtonStyle.Outlined,
                        textStyle = ChallengeTheme.typography.bold16,
                    )
                },
            )
        }
    }
}

/** 나 / AI 반려 — 실패 뱃지 + 내 사진 + 반려 사유. 마감 뒤라 재촬영 CTA 는 없다. */
@Preview
@Composable
private fun MissionCardMineFailedPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MissionCard(
                label = "나의 미션",
                mission = "오늘 운동 1시간 하기",
                status = VerificationStatus.FAILED,
                footerSpacing = 12.dp,
                footer = {
                    VerificationPhoto(
                        status = VerificationStatus.FAILED,
                        photoUrl = null,
                        cacheKey = null,
                        isLoading = true,
                    )
                    VerticalSpacer(8.dp)
                    RejectionReasonText(
                        text = "${VERIFICATION_REJECTED_PREFIX}책상 위 커피잔 사진으로, " +
                            "미션 수행 증거로 보기 어렵습니다.",
                    )
                },
            )
        }
    }
}

/** 상대 / 미인증 — 슬롯에 사진 영역이 들어가고 "아직 인증하지 않았어요" 를 그린다. */
@Preview
@Composable
private fun MissionCardOpponentPendingPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MissionCard(
                label = "우건의 미션",
                mission = "책 30페이지 읽기",
                status = VerificationStatus.PENDING,
                footerSpacing = 12.dp,
                footer = {
                    VerificationPhoto(
                        status = VerificationStatus.PENDING,
                        photoUrl = null,
                        cacheKey = null,
                    )
                },
            )
        }
    }
}

/** 인증 현황 모름 — 뱃지가 통째로 빠지고 슬롯의 사진 영역이 대체 문구로 내려간다. */
@Preview
@Composable
private fun MissionCardStatusUnknownPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MissionCard(
                label = "우건의 미션",
                mission = "책 30페이지 읽기",
                status = null,
                footerSpacing = 12.dp,
                footer = {
                    VerificationPhoto(
                        status = null,
                        photoUrl = null,
                        cacheKey = null,
                        onRetry = {},
                    )
                },
            )
        }
    }
}

/** 상대가 아직 맹세하지 않음 — 본문이 자리표시자로 남고 슬롯은 통째로 빠진다. */
@Preview
@Composable
private fun MissionCardMissionNotSetPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MissionCard(
                label = "우건의 미션",
                mission = null,
                status = VerificationStatus.PENDING,
            )
        }
    }
}
