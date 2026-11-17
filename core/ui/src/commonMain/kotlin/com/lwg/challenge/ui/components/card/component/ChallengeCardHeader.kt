package com.lwg.challenge.ui.components.card.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.ui.components.ChallengeOutcomePill
import com.lwg.challenge.ui.components.card.ChallengeCardScope
import kotlinx.datetime.number

/**
 * 카드 첫 줄. 🔥 vs 닉네임 · 날짜 + 오른쪽에 결과 또는 남은 시간.
 *
 * @param dateText 닉네임 뒤에 붙인다 — pill 옆에 두면 표처럼 읽혀 서술로 읽히지 않는다.
 */
@Composable
fun ChallengeCardHeader(
    opponentNickname: String,
    outcome: ChallengeOutcome?,
    modifier: Modifier = Modifier,
    dateText: String? = null,
    deadlineTimeText: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FireIcon()
            // 닉네임만 줄인다. 날짜와 결과는 카드의 핵심 정보라 밀려나면 안 된다.
            CaptionText(
                text = "vs $opponentNickname",
                modifier = Modifier.weight(1f, fill = false),
            )
            if (dateText != null) {
                CaptionText(text = "·")
                CaptionText(text = dateText)
            }
        }

        HeaderTrailing(outcome = outcome, deadlineTimeText = deadlineTimeText, isDeadlineUrgent = false)
    }
}

/**
 * 상대가 한 사람뿐인 목록(친구 전적)의 첫 줄. `vs 닉네임` 대신 카드끼리 갈리는 값([leadingText])만 둔다.
 *
 * @param isDeadlineUrgent true 면 남은 시간을 `warning` 대신 `error` 로 그린다.
 */
@Composable
fun ChallengeCardHeader(
    leadingText: String,
    outcome: ChallengeOutcome?,
    modifier: Modifier = Modifier,
    deadlineTimeText: String? = null,
    isDeadlineUrgent: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FireIcon()
            CaptionText(text = leadingText)
        }

        HeaderTrailing(outcome = outcome, deadlineTimeText = deadlineTimeText, isDeadlineUrgent = isDeadlineUrgent)
    }
}

@Composable
internal fun ChallengeCardScope.ChallengeCardHeader(
    deadlineTimeText: String?,
    modifier: Modifier = Modifier,
) {
    ChallengeCardHeader(
        opponentNickname = cardInfo.opponentNickname,
        outcome = cardInfo.outcome,
        modifier = modifier,
        // 보관함 바가 연·월을 이미 말하지만 카드만 떼어 봐도 읽히도록 월을 반복한다.
        dateText = cardInfo.challengeDate?.let { "${it.month.number}월 ${it.day}일" },
        deadlineTimeText = deadlineTimeText,
    )
}

@Composable
private fun HeaderTrailing(
    outcome: ChallengeOutcome?,
    deadlineTimeText: String?,
    isDeadlineUrgent: Boolean,
) {
    if (outcome != null) {
        ChallengeOutcomePill(outcome = outcome)
    } else if (deadlineTimeText != null) {
        DeadlineTimeText(text = deadlineTimeText, isUrgent = isDeadlineUrgent)
    }
}

@Composable
private fun FireIcon() {
    Icon(
        imageVector = Icons.Filled.LocalFireDepartment,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = ChallengeTheme.colorScheme.icon.point,
    )
}

@Composable
private fun DeadlineTimeText(text: String, isUrgent: Boolean) {
    val color = if (isUrgent) ChallengeTheme.colorScheme.error else ChallengeTheme.colorScheme.warning
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color,
        )
        Text(
            text = text,
            style = ChallengeTheme.typography.medium12,
            color = color,
        )
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
        color = ChallengeTheme.colorScheme.text.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@ThemePreviews
@Composable
private fun ChallengeCardHeaderPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChallengeCardHeader(opponentNickname = "우건", outcome = null, deadlineTimeText = "3시간 남음")
            ChallengeCardHeader(opponentNickname = "우건", outcome = ChallengeOutcome.WIN)
            ChallengeCardHeader(opponentNickname = "준혁", outcome = ChallengeOutcome.LOSE, dateText = "8월 14일")
            ChallengeCardHeader(
                opponentNickname = "오늘도지지않는불꽃같은도전자김준혁입니다",
                outcome = ChallengeOutcome.DRAW,
                dateText = "8월 31일",
            )
        }
    }
}

/** 닉네임 없이 leading 만 — 친구 전적의 진행 중(상태 라벨)·지난(날짜) 카드. */
@ThemePreviews
@Composable
private fun ChallengeCardHeaderLeadingOnlyPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChallengeCardHeader(leadingText = "진행 중", outcome = null, deadlineTimeText = "3시간 남음")
            ChallengeCardHeader(
                leadingText = "받은 도전장",
                outcome = null,
                deadlineTimeText = "곧 마감",
                isDeadlineUrgent = true,
            )
            ChallengeCardHeader(leadingText = "11월 12일", outcome = ChallengeOutcome.BOTH_LOSE)
        }
    }
}
