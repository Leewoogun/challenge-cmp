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
 * 한쪽 진영의 미션 카드. 나/상대 구분은 [label] 문자열과 [footer] 에 무엇을 넣느냐뿐이다.
 *
 * 나/상대를 별도 컴포넌트로 쪼개지 않는 이유: 두 카드는 나란히 놓이는 같은 껍데기라, 갈라 두면
 * 라벨 타이포·뱃지 위치·간격이 한쪽만 바뀌며 어긋난다.
 *
 * @param mission 아직 맹세하지 않았으면 `null`. 행을 빼지 않고 자리표시자를 두는 이유는, 맹세가
 *   들어오는 순간 카드 높이가 변해 아래 블록이 밀리는 걸 막기 위해서다.
 * @param status 인증 현황을 못 받았으면 `null` — 뱃지를 통째로 뺀다. 뱃지에는 대기중/인증완료/실패
 *   세 값밖에 없어 아무거나 고르면 확인하지 못한 사실을 단정하게 된다("대기중" 은 이미 인증한
 *   사람을 미인증으로 못 박는다). 라벨만 남기면 아무 주장도 하지 않는다.
 * @param footerSpacing [footer] 위 여백. CTA(16dp)와 사진(12dp)이 달라 호출부가 정한다.
 *   [footer] 가 `null` 이면 여백도 붙지 않는다.
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

/**
 * 나 / AI 반려 — 실패 뱃지가 붙지만 내가 올린 사진은 그대로 남고, 그 아래에 반려 사유가 붙는다.
 * 카드 한 장으로 완성된 반려 장면이다.
 *
 * 재촬영 CTA 가 없는 게 핵심이다. 반려는 자정 판정에서 확정되고 그 시점이 곧 마감이라, 이 카드를
 * 보는 순간 챌린지는 이미 닫혀 있어 다시 찍을 창이 존재하지 않는다.
 *
 * 뱃지(error)와 사유(onSurfaceVariant)가 색으로 경쟁하지 않는지도 여기서 확인한다 — 사유는 판정이
 * 아니라 판정에 붙는 설명이라 위계가 한 단계 아래다.
 */
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
                    // isLoading 으로 때우지 않고 URL 을 채우는 이유: 로딩 문구는 분기가 어디서
                    // 끝났든 똑같이 나와서, 실패 문구가 사진 자리를 가로채도 프리뷰가 같아 보인다.
                    VerificationPhoto(
                        status = VerificationStatus.FAILED,
                        photoUrl = "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
                        cacheKey = "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger|2026-01-01T09:30",
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
