package com.lwg.challenge.feature.mypage.component

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.feature.mypage.archivePreviewItem
import com.lwg.challenge.feature.mypage.contract.ArchiveChallengeItemState
import com.lwg.challenge.ui.components.BetStrip
import com.lwg.challenge.ui.components.ChallengeOutcomePill
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약서 보관함 카드. design.md §2.4 — 홈 챌린지 카드의 문법에서 인증 상태 pill 2개를 빼고
 * 날짜를 더한 형태다.
 *
 * 홈 카드와 컴포넌트를 합치지 않는 이유: 합치면 `showVerificationStatus` 같은 플래그가 생기고
 * 그 플래그가 카드 내부 레이아웃을 갈라 놓는다. **문법을 공유하되 컴포넌트는 나눈다.**
 *
 * 인증 pill 을 빼도 정보가 줄지 않는다 — 판정 규칙상 결과 4종이 양측 인증 상태를 유일하게
 * 결정하므로(승=나만 인증 / 패=상대만 / 무=양쪽 / 양패=양쪽 미인증) pill 2개는 정보량이 0이다.
 *
 * 상대가 탈퇴했어도 특별 표시를 넣지 않는다 — 닉네임이 `"탈퇴한 사용자"` 로 오는 것으로 충분하고,
 * 톤다운은 증거 보관 화면에서 *"이 기록은 무효"* 로 읽힌다.
 */
@Composable
internal fun ArchiveChallengeCard(
    item: ArchiveChallengeItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                opponentNickname = item.opponentNickname,
                dateText = item.dateText,
                outcome = item.outcome,
            )

            MissionRow(
                label = "나의 미션",
                mission = item.myMission,
                missionColor = ChallengeTheme.colorScheme.onBackground,
                missionEmphasized = true,
            )

            MissionRow(
                label = item.opponentMissionLabel,
                mission = item.opponentMission,
                missionColor = ChallengeTheme.colorScheme.onSurfaceVariant,
                missionEmphasized = false,
            )

            BetStrip(text = item.betText)
        }
    }
}

/**
 * 날짜는 결과 pill 옆이 아니라 상대 닉네임 뒤에 붙는다 — 우측에 두면 *"이날 졌다"* 보다
 * *"이 날짜의 결과"* 라는 표 같은 읽기가 되어 카드의 서술 톤이 깨진다.
 */
@Composable
private fun HeaderRow(
    opponentNickname: String,
    dateText: String,
    outcome: ChallengeOutcome,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChallengeTheme.colorScheme.primary,
            )
            // 닉네임만 줄인다. 날짜는 카드의 핵심 정보라 잘리면 안 된다.
            CaptionText(
                text = "vs $opponentNickname",
                modifier = Modifier.weight(1f, fill = false),
            )
            CaptionText(text = "·")
            CaptionText(text = dateText)
        }

        ChallengeOutcomePill(outcome = outcome)
    }
}

@Composable
private fun CaptionText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = ChallengeTheme.typography.medium12,
        color = ChallengeTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun MissionRow(
    label: String,
    mission: String,
    missionColor: Color,
    missionEmphasized: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = mission,
            style = if (missionEmphasized) {
                ChallengeTheme.typography.bold14
            } else {
                ChallengeTheme.typography.medium14
            },
            color = missionColor,
        )
    }
}

@Preview
@Composable
private fun ArchiveChallengeCardWinPreview() {
    ArchiveChallengeCardPreviewContainer {
        ArchiveChallengeCard(item = archivePreviewItem(), onClick = {})
    }
}

@Preview
@Composable
private fun ArchiveChallengeCardOutcomesPreview() {
    ArchiveChallengeCardPreviewContainer {
        ArchiveChallengeCard(
            item = archivePreviewItem(myResult = ChallengeOutcome.WIN),
            onClick = {},
        )
        ArchiveChallengeCard(
            item = archivePreviewItem(
                challengeId = 2L,
                challengeDate = LocalDate(2026, 8, 9),
                opponentNickname = "민지",
                myMission = "물 2L 마시기",
                opponentMission = "10000보 걷기",
                bet = "점심 사기 🍚",
                myResult = ChallengeOutcome.LOSE,
            ),
            onClick = {},
        )
        ArchiveChallengeCard(
            item = archivePreviewItem(
                challengeId = 3L,
                challengeDate = LocalDate(2026, 8, 3),
                opponentNickname = "도윤",
                myMission = "아침 6시 기상하기",
                opponentMission = "영어 단어 30개 외우기",
                bet = "치킨 사기 🍗",
                myResult = ChallengeOutcome.DRAW,
            ),
            onClick = {},
        )
        ArchiveChallengeCard(
            item = archivePreviewItem(
                challengeId = 4L,
                challengeDate = LocalDate(2026, 7, 31),
                opponentNickname = "지훈",
                myMission = "야식 참기",
                opponentMission = "일기 쓰기",
                bet = "떡볶이 사기 🍢",
                myResult = ChallengeOutcome.BOTH_LOSE,
            ),
            onClick = {},
        )
    }
}

/** 탈퇴한 상대. 뱃지·톤다운 없이 닉네임만 `"탈퇴한 사용자"` 로 읽힌다 (§2.5.2). */
@Preview
@Composable
private fun ArchiveChallengeCardWithdrawnOpponentPreview() {
    ArchiveChallengeCardPreviewContainer {
        ArchiveChallengeCard(
            item = archivePreviewItem(
                challengeId = 5L,
                challengeDate = LocalDate(2026, 6, 12),
                opponentNickname = "탈퇴한 사용자",
                myMission = "코딩 2시간",
                opponentMission = "러닝 5km",
                bet = "아이스크림 사기 🍦",
                myResult = ChallengeOutcome.WIN,
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ArchiveChallengeCardLongTextPreview() {
    ArchiveChallengeCardPreviewContainer {
        ArchiveChallengeCard(
            item = archivePreviewItem(
                challengeId = 6L,
                challengeDate = LocalDate(2026, 8, 31),
                opponentNickname = "오늘도지지않는불꽃같은도전자김준혁입니다",
                myMission = "새벽 다섯 시에 일어나서 헬스장 가서 유산소 30분 웨이트 60분 하기",
                opponentMission = "퇴근하고 집에 오는 길에 편의점 들르지 않고 곧장 집으로 가기",
                bet = "제일 비싼 한우 오마카세 사주기 🥩",
                myResult = ChallengeOutcome.LOSE,
            ),
            onClick = {},
        )
    }
}

@Composable
private fun ArchiveChallengeCardPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}
