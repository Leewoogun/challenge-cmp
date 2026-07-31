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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
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
 * @param myVerificationStatus 현재 사용자 인증 상태 (status pill 색/아이콘/라벨 결정).
 * @param opponentVerificationStatus 상대 인증 상태.
 * @param bet 내기 내용 (카드 하단 띠).
 */
@Composable
internal fun ChallengeCard(
    challengeId: Long,
    myMission: String,
    opponentNickname: String,
    opponentMission: String,
    remainingText: String,
    myVerificationStatus: ChallengeVerificationStatus,
    opponentVerificationStatus: ChallengeVerificationStatus,
    bet: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

            BetStrip(text = "내기: $bet")
        }
    }
}

@Composable
private fun HeaderRow(
    opponentNickname: String,
    remainingText: String,
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

@Composable
private fun MissionRow(
    label: String,
    mission: String,
    missionColor: Color,
    missionEmphasized: Boolean,
    status: ChallengeVerificationStatus,
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
        StatusPill(status = status)
    }
}

@Composable
private fun StatusPill(status: ChallengeVerificationStatus) {
    val visual = statusVisualOf(status)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = visual.accent.copy(alpha = 0.10f),
        contentColor = visual.accent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = visual.accent,
            )
            Text(
                text = visual.label,
                style = ChallengeTheme.typography.medium10,
                color = visual.accent,
            )
        }
    }
}

private data class StatusVisual(
    val accent: Color,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun statusVisualOf(status: ChallengeVerificationStatus): StatusVisual = when (status) {
    ChallengeVerificationStatus.PENDING -> StatusVisual(
        accent = ChallengeTheme.colorScheme.warning,
        label = "대기중",
        icon = Icons.Filled.Schedule,
    )
    ChallengeVerificationStatus.VERIFIED -> StatusVisual(
        accent = ChallengeTheme.colorScheme.success,
        label = "인증완료",
        icon = Icons.Filled.CheckCircle,
    )
    ChallengeVerificationStatus.FAILED -> StatusVisual(
        accent = ChallengeTheme.colorScheme.error,
        label = "실패",
        icon = Icons.Filled.Cancel,
    )
}

@Preview
@Composable
private fun ChallengeCardPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChallengeCard(
                challengeId = 1L,
                myMission = "오늘 운동 1시간 하기",
                opponentNickname = "우건",
                opponentMission = "책 30페이지 읽기",
                remainingText = "3시간 남음",
                myVerificationStatus = ChallengeVerificationStatus.VERIFIED,
                opponentVerificationStatus = ChallengeVerificationStatus.PENDING,
                bet = "커피 사기 ☕",
                onClick = {},
            )
            ChallengeCard(
                challengeId = 2L,
                myMission = "물 2L 마시기",
                opponentNickname = "민지",
                opponentMission = "10000보 걷기",
                remainingText = "곧 마감",
                myVerificationStatus = ChallengeVerificationStatus.FAILED,
                opponentVerificationStatus = ChallengeVerificationStatus.VERIFIED,
                bet = "점심 사기 🍚",
                onClick = {},
            )
        }
    }
}
