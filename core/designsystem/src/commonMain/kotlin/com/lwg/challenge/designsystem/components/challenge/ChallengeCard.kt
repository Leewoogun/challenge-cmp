@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.designsystem.components.challenge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import kotlin.time.Instant

/**
 * 챌린지 카드 — 홈/상세 등 재사용 가능한 단위.
 *
 * design.md §4.1 props 명세 그대로. 네비게이션은 호출부 책임([onClick] 1개만 노출).
 *
 * @param challengeId 식별자 (디버깅/key 용; 외부에서 LazyColumn key 와 별개로 활용).
 * @param myMission 현재 사용자의 미션 (강조 표시).
 * @param opponentNickname 상대 닉네임 ("vs {opponent}" / "{opponent}의 미션" 에 노출).
 * @param opponentMission 상대 미션 (톤 다운).
 * @param deadline 마감 시각(Instant). 내부에서 [toRelativeKoreanString] 으로 상대 시간 변환.
 * @param myVerificationStatus 현재 사용자 인증 상태 (status pill 색/아이콘/라벨 결정).
 * @param opponentVerificationStatus 상대 인증 상태.
 * @param bet 내기 내용 (카드 하단 띠).
 */
@Composable
fun ChallengeCard(
    challengeId: Long,
    myMission: String,
    opponentNickname: String,
    opponentMission: String,
    deadline: Instant,
    myVerificationStatus: ChallengeVerificationStatus,
    opponentVerificationStatus: ChallengeVerificationStatus,
    bet: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("UNUSED_VARIABLE")
    val id = challengeId

    val remainingText = remember(deadline) { deadline.toRelativeKoreanString() }

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

            BetStrip(bet = bet)
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

@Composable
private fun BetStrip(bet: String) {
    val primary = ChallengeTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color = primary.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = primary,
        )
        Text(
            text = "내기: $bet",
            style = ChallengeTheme.typography.medium12,
            color = primary,
        )
    }
}
