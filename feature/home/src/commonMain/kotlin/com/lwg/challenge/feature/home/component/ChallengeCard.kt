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
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.MyChallengeInfo
import com.lwg.challenge.domain.model.OpponentChallengeInfo
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.ui.components.ChallengePenaltyArea
import com.lwg.challenge.ui.components.ChallengeOutcomePill
import com.lwg.challenge.ui.components.VerificationStatusPill
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 홈의 챌린지 카드.
 */
@Composable
internal fun ChallengeCard(
    challenge: ActiveChallenge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    deadlineTimeText: String? = null,
) {
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
                opponentNickname = challenge.opponent.nickname,
                deadlineTimeText = deadlineTimeText,
                outcome = challenge.outcome,
            )

            MissionRow(
                label = "나의 미션",
                mission = challenge.me.mission,
                missionColor = ChallengeTheme.colorScheme.onBackground,
                isMyMission = true,
                status = challenge.me.verificationStatus,
            )

            MissionRow(
                label = "${challenge.opponent.nickname}의 미션",
                mission = challenge.opponent.mission,
                missionColor = ChallengeTheme.colorScheme.onSurfaceVariant,
                isMyMission = false,
                status = challenge.opponent.verificationStatus,
            )

            ChallengePenaltyArea(text = "내기: ${challenge.penalty}")
        }
    }
}

@Composable
private fun HeaderRow(
    opponentNickname: String,
    deadlineTimeText: String?,
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

        if (outcome != null) {
            ChallengeOutcomePill(outcome = outcome)
        } else if (deadlineTimeText != null) {
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
                    text = deadlineTimeText,
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
    isMyMission: Boolean,
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
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = mission,
                style = if (isMyMission) {
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
            challenge = previewChallenge(
                myMission = "오늘 운동 1시간 하기",
                myStatus = VerificationStatus.VERIFIED,
                opponentNickname = "우건",
                opponentMission = "책 30페이지 읽기",
                opponentStatus = VerificationStatus.PENDING,
                penalty = "커피 사기 ☕",
            ),
            deadlineTimeText = "3시간 남음",
            onClick = {},
        )
        ChallengeCard(
            challenge = previewChallenge(
                myMission = "물 2L 마시기",
                myStatus = VerificationStatus.FAILED,
                opponentNickname = "민지",
                opponentMission = "10000보 걷기",
                opponentStatus = VerificationStatus.VERIFIED,
                penalty = "점심 사기 🍚",
            ),
            deadlineTimeText = "곧 마감",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ChallengeCardWinPreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challenge = previewChallenge(
                myMission = "오늘 운동 1시간 하기",
                myStatus = VerificationStatus.VERIFIED,
                opponentNickname = "우건",
                opponentMission = "책 30페이지 읽기",
                opponentStatus = VerificationStatus.FAILED,
                penalty = "커피 사기 ☕",
                outcome = ChallengeOutcome.WIN,
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ChallengeCardLosePreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challenge = previewChallenge(
                myMission = "물 2L 마시기",
                myStatus = VerificationStatus.FAILED,
                opponentNickname = "민지",
                opponentMission = "10000보 걷기",
                opponentStatus = VerificationStatus.VERIFIED,
                penalty = "점심 사기 🍚",
                outcome = ChallengeOutcome.LOSE,
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ChallengeCardDrawPreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challenge = previewChallenge(
                myMission = "아침 6시 기상하기",
                myStatus = VerificationStatus.VERIFIED,
                opponentNickname = "도윤",
                opponentMission = "영어 단어 30개 외우기",
                opponentStatus = VerificationStatus.VERIFIED,
                penalty = "치킨 사기 🍗",
                outcome = ChallengeOutcome.DRAW,
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ChallengeCardBothLosePreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            challenge = previewChallenge(
                myMission = "야식 참기",
                myStatus = VerificationStatus.FAILED,
                opponentNickname = "지훈",
                opponentMission = "일기 쓰기",
                opponentStatus = VerificationStatus.FAILED,
                penalty = "떡볶이 사기 🍢",
                outcome = ChallengeOutcome.BOTH_LOSE,
            ),
            onClick = {},
        )
    }
}

private fun previewChallenge(
    myMission: String,
    myStatus: VerificationStatus,
    opponentNickname: String,
    opponentMission: String,
    opponentStatus: VerificationStatus,
    penalty: String,
    outcome: ChallengeOutcome? = null,
) = ActiveChallenge(
    challengeId = 1L,
    me = MyChallengeInfo(mission = myMission, verificationStatus = myStatus),
    opponent = OpponentChallengeInfo(
        nickname = opponentNickname,
        mission = opponentMission,
        verificationStatus = opponentStatus,
    ),
    deadline = LocalDateTime.parse("2026-06-27T22:00:00"),
    penalty = penalty,
    status = if (outcome == null) ActiveChallengeStatus.IN_PROGRESS else ActiveChallengeStatus.COMPLETED,
    outcome = outcome,
)

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
