package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.ui.components.BetStrip
import com.lwg.challenge.ui.components.ChallengeOutcomePill
import com.lwg.challenge.ui.components.VerificationStatusPill
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 챌린지 카드 — 홈/상세 등 재사용 가능한 단위.
 *
 * design.md §4.1 props 명세 그대로. 네비게이션은 호출부 책임([onClick] 1개만 노출).
 *
 * @param challengeId 식별자 (디버깅/key 용; 외부에서 LazyColumn key 와 별개로 활용).
 * @param myMission 현재 사용자의 미션 (강조 표시).
 * @param opponentNickname 상대 닉네임 ("vs {opponent}" / "{opponent}의 미션" 에 노출).
 * @param opponentMission 상대 미션 (톤 다운).
 * @param remainingText 마감까지 남은 시간 표기. 변환은 State 책임(`HomeUiState.Data.deadlineTextOf`).
 *   **진행 중 카드에만 넘긴다.** 판정이 끝난 카드는 이 자리를 결과에 내주므로 null 로 두면 되고,
 *   [outcome] 마저 null 이면 헤더 우측을 비운다.
 * @param outcome 판정 결과. null 이면 [remainingText] 유무로 진행 중/결과 미상을 가른다.
 * @param myVerificationStatus 현재 사용자 인증 상태 (status pill 색/아이콘/라벨 결정).
 * @param opponentVerificationStatus 상대 인증 상태.
 * @param penalty 진 쪽이 해야 하는 벌칙 (카드 하단 띠).
 */
@Composable
internal fun ChallengeCard(
    challengeId: Long,
    myMission: String,
    opponentNickname: String,
    opponentMission: String,
    myVerificationStatus: VerificationStatus,
    opponentVerificationStatus: VerificationStatus,
    penalty: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    remainingText: String? = null,
    outcome: ChallengeOutcome? = null,
) {
    @Suppress("UNUSED_VARIABLE")
    val id = challengeId

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeaderRow(
                opponentNickname = opponentNickname,
                remainingText = remainingText,
                outcome = outcome,
            )

            MissionRow(
                label = "나의 미션",
                mission = myMission,
                missionColor = ChallengeTheme.colorScheme.onBackground,
                missionEmphasized = true,
                status = myVerificationStatus,
            )

            MissionRow(
                label = "${opponentNickname}의 미션",
                mission = opponentMission,
                missionColor = ChallengeTheme.colorScheme.onSurfaceVariant,
                missionEmphasized = false,
                status = opponentVerificationStatus,
            )

            BetStrip(text = "내기: $penalty")
        }
    }
}

@Composable
private fun HeaderRow(
    opponentNickname: String,
    remainingText: String?,
    outcome: ChallengeOutcome?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChallengeTheme.colorScheme.primary,
            )
            Text(
                text = "vs $opponentNickname",
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        }
        // 판정이 끝난 카드에는 시계를 남기지 않고 자리를 통째로 결과에 넘긴다. 이 자리의 시계·텍스트는
        // warning 으로 하드코딩돼 있어 "지금 움직이면 바꿀 수 있다" 는 촉구인데, 결과가 나온 뒤엔
        // 바꿀 것이 없어 "마감" 이 촉구색으로 뜨는 조합만 남는다 — 상세가 HeadlineTone 으로 없앤 문제다.
        //
        // 둘 다 null 이면 아무것도 그리지 않는다. 매퍼가 모르는 myResult 를 null 로 흡수하는(항목은
        // 살리는) 정책이라 서버가 결과 종류를 하나 추가하면 판정 완료 + outcome=null 카드가 실제로
        // 내려온다. 이때 남은 시간을 그리면 끝난 챌린지를 진행 중처럼 보여주게 되고, 없는 결과를 지어내
        // 그릴 수도 없다 — 상세가 같은 상황에서 결과를 날조하지 않고 표기를 낮추는 것과 같은 판단이다.
        // 빈 자리가 틀린 자리보다 낫다.
        if (outcome != null) {
            ChallengeOutcomePill(outcome = outcome)
        } else if (remainingText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = ChallengeTheme.colorScheme.warning,
                )
                Text(
                    text = remainingText,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.warning,
                )
            }
        }
    }
}

@Composable
private fun MissionRow(
    label: String,
    mission: String,
    missionColor: Color,
    missionEmphasized: Boolean,
    status: VerificationStatus,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                // design.md: text-[11px] → medium12 근사 (tokens.md §5.2)
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = mission,
                // semibold(웹) → Medium(앱) — Typography.kt §5.3 정책
                style = if (missionEmphasized) {
                    ChallengeTheme.typography.bold14
                } else {
                    ChallengeTheme.typography.medium14
                },
                color = missionColor,
            )
        }
        VerificationStatusPill(status = status)
    }
}

@Preview
@Composable
private fun ChallengeCardPreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challengeId = 1L,
            myMission = "오늘 운동 1시간 하기",
            opponentNickname = "우건",
            opponentMission = "책 30페이지 읽기",
            remainingText = "3시간 남음",
            myVerificationStatus = VerificationStatus.VERIFIED,
            opponentVerificationStatus = VerificationStatus.PENDING,
            penalty = "커피 사기 ☕",
            onClick = {},
        )
        ChallengeCard(
            challengeId = 2L,
            myMission = "물 2L 마시기",
            opponentNickname = "민지",
            opponentMission = "10000보 걷기",
            remainingText = "곧 마감",
            myVerificationStatus = VerificationStatus.FAILED,
            opponentVerificationStatus = VerificationStatus.VERIFIED,
            penalty = "점심 사기 🍚",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ChallengeCardWinPreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challengeId = 3L,
            myMission = "오늘 운동 1시간 하기",
            opponentNickname = "우건",
            opponentMission = "책 30페이지 읽기",
            myVerificationStatus = VerificationStatus.VERIFIED,
            opponentVerificationStatus = VerificationStatus.FAILED,
            penalty = "커피 사기 ☕",
            onClick = {},
            outcome = ChallengeOutcome.WIN,
        )
    }
}

@Preview
@Composable
private fun ChallengeCardLosePreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challengeId = 4L,
            myMission = "물 2L 마시기",
            opponentNickname = "민지",
            opponentMission = "10000보 걷기",
            myVerificationStatus = VerificationStatus.FAILED,
            opponentVerificationStatus = VerificationStatus.VERIFIED,
            penalty = "점심 사기 🍚",
            onClick = {},
            outcome = ChallengeOutcome.LOSE,
        )
    }
}

@Preview
@Composable
private fun ChallengeCardDrawPreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challengeId = 5L,
            myMission = "아침 6시 기상하기",
            opponentNickname = "도윤",
            opponentMission = "영어 단어 30개 외우기",
            myVerificationStatus = VerificationStatus.VERIFIED,
            opponentVerificationStatus = VerificationStatus.VERIFIED,
            penalty = "치킨 사기 🍗",
            onClick = {},
            outcome = ChallengeOutcome.DRAW,
        )
    }
}

@Preview
@Composable
private fun ChallengeCardBothLosePreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challengeId = 6L,
            myMission = "야식 참기",
            opponentNickname = "지훈",
            opponentMission = "일기 쓰기",
            myVerificationStatus = VerificationStatus.FAILED,
            opponentVerificationStatus = VerificationStatus.FAILED,
            penalty = "떡볶이 사기 🍢",
            onClick = {},
            outcome = ChallengeOutcome.BOTH_LOSE,
        )
    }
}

/** 서버가 모르는 결과값을 보냈을 때(tolerant 매핑이 null 로 흡수) 의 카드 — 시계도 결과도 없이 자리를 비운다. */
@Preview
@Composable
private fun ChallengeCardUnknownOutcomePreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challengeId = 7L,
            myMission = "매일 30분 산책하기",
            opponentNickname = "서연",
            opponentMission = "블로그 글 1개 쓰기",
            myVerificationStatus = VerificationStatus.VERIFIED,
            opponentVerificationStatus = VerificationStatus.VERIFIED,
            penalty = "아이스크림 사기 🍦",
            onClick = {},
            remainingText = null,
            outcome = null,
        )
    }
}

@Composable
private fun ChallengeCardPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}
