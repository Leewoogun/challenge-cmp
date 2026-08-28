package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.BetStrip
import com.lwg.challenge.ui.components.ProfilePlaceholder
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 받은 도전장 카드. design.md §2.3 / §4.3.
 *
 * 표시 문구(`titleText` / `missionLabelText` / `betText` / `deadlineText`)와 임박 판정([isUrgent]) 은
 * `ReceivedChallengeItemState` 가 계산해서 넘긴다 — 컴포저블에서 포맷팅/파생 계산을 하지 않는다.
 * 특히 임박 판정은 `deadline - now` 비교라 `now` 를 ViewModel 이 1회 측정해 주입해야 한다.
 *
 * @param isUrgent 잔여 1시간 이하. true 면 남은 시간을 `error`, 아니면 `warning` 으로 렌더한다.
 * @param actionsEnabled 수락/거절 in-flight 동안 false. 해당 카드의 두 버튼만 비활성화된다.
 */
@Composable
internal fun ReceivedChallengeCard(
    challengerNickname: String,
    challengerProfileImageUrl: String?,
    titleText: String,
    deadlineText: String,
    isUrgent: Boolean,
    missionLabelText: String,
    challengerMission: String,
    betText: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    actionsEnabled: Boolean = true,
) {
    @Suppress("UNUSED_VARIABLE")
    val profileImageUrl = challengerProfileImageUrl

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            HeaderRow(
                challengerNickname = challengerNickname,
                titleText = titleText,
                deadlineText = deadlineText,
                isUrgent = isUrgent,
            )

            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = missionLabelText,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = challengerMission,
                    style = ChallengeTheme.typography.bold14,
                    color = ChallengeTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            BetStrip(
                text = betText,
                modifier = Modifier.padding(top = 10.dp),
            )

            ActionRow(
                onAccept = onAccept,
                onReject = onReject,
                enabled = actionsEnabled,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun HeaderRow(
    challengerNickname: String,
    titleText: String,
    deadlineText: String,
    isUrgent: Boolean,
) {
    val deadlineColor = if (isUrgent) {
        ChallengeTheme.colorScheme.error
    } else {
        ChallengeTheme.colorScheme.warning
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfilePlaceholder(nickname = challengerNickname)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = titleText,
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = deadlineColor,
                )
                Text(
                    text = deadlineText,
                    style = ChallengeTheme.typography.medium12,
                    color = deadlineColor,
                )
            }
        }
    }
}

@Composable
private fun ActionRow(
    onAccept: () -> Unit,
    onReject: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconTextButton(
            text = "수락",
            icon = Icons.Filled.Check,
            onClick = onAccept,
            modifier = Modifier.weight(1f),
            style = IconTextButtonStyle.Filled,
            enabled = enabled,
            textStyle = ChallengeTheme.typography.bold14,
            shape = RoundedCornerShape(12.dp),
            height = 36.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )
        IconTextButton(
            text = "거절",
            icon = Icons.Filled.Close,
            onClick = onReject,
            modifier = Modifier.weight(1f),
            style = IconTextButtonStyle.Outlined,
            enabled = enabled,
            textStyle = ChallengeTheme.typography.bold14,
            shape = RoundedCornerShape(12.dp),
            height = 36.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )
    }
}

@Preview
@Composable
private fun ReceivedChallengeCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ReceivedChallengeCard(
                challengerNickname = "도윤",
                challengerProfileImageUrl = null,
                titleText = "도윤의 도전장",
                deadlineText = "5시간 32분",
                isUrgent = false,
                missionLabelText = "도윤의 미션",
                challengerMission = "아침 6시 기상하기",
                betText = "내기: 치킨 사기 🍗",
                onAccept = {},
                onReject = {},
            )
        }
    }
}

@Preview
@Composable
private fun ReceivedChallengeCardLongMissionPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ReceivedChallengeCard(
                challengerNickname = "도윤",
                challengerProfileImageUrl = null,
                titleText = "도윤의 도전장",
                deadlineText = "42분",
                isUrgent = true,
                missionLabelText = "도윤의 미션",
                challengerMission = "아침 6시에 일어나서 30분 스트레칭하고 찬물 샤워까지 끝낸 다음 " +
                    "출근 전에 책 20페이지 읽고 인증샷 남기기",
                betText = "내기: 치킨 사기 🍗",
                onAccept = {},
                onReject = {},
            )
        }
    }
}

@Preview
@Composable
private fun ReceivedChallengeCardActionsDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ReceivedChallengeCard(
                challengerNickname = "도윤",
                challengerProfileImageUrl = null,
                titleText = "도윤의 도전장",
                deadlineText = "곧 마감",
                isUrgent = true,
                missionLabelText = "도윤의 미션",
                challengerMission = "아침 6시 기상하기",
                betText = "내기: 치킨 사기 🍗",
                onAccept = {},
                onReject = {},
                actionsEnabled = false,
            )
        }
    }
}
