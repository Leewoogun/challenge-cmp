package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.ProfilePlaceholder
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 상세 최상단 대결 헤더. 양측 아바타를 "VS" 로 마주 세우고 그 아래 마감 표기를 붙인다.
 *
 * 마감 문구·색을 파라미터로 받는 이유: "남은 시간: 5시간 32분"(마감 전) 과 "판정 대기 중"(마감 후) 은
 * 접두어까지 다른 별개 문장이라, 여기서 시각을 비교해 고르면 Composable 이 시간 판정을 떠안는다.
 *
 * 상호작용이 없다 — 아바타는 표시 전용이라 탭 콜백을 뚫지 않는다.
 */
@Composable
internal fun VsHeaderCard(
    myNickname: String,
    opponentNickname: String,
    deadlineText: String,
    deadlineColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PartyColumn(nickname = myNickname)

                VsColumn()

                PartyColumn(nickname = opponentNickname)
            }

            VerticalSpacer(16.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = deadlineColor,
                )
                Text(
                    text = deadlineText,
                    style = ChallengeTheme.typography.medium14,
                    color = deadlineColor,
                )
            }
        }
    }
}

@Composable
private fun PartyColumn(nickname: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProfilePlaceholder(
            nickname = nickname,
            size = 64.dp,
            shape = RoundedCornerShape(20.dp),
            textStyle = ChallengeTheme.typography.medium24,
        )
        Text(
            text = nickname,
            style = ChallengeTheme.typography.bold14,
            color = ChallengeTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VsColumn() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = ChallengeTheme.colorScheme.primary,
        )

        VerticalSpacer(4.dp)

        Text(
            text = "VS",
            style = ChallengeTheme.typography.bold12,
            color = ChallengeTheme.colorScheme.primary,
        )
    }
}

@Preview
@Composable
private fun VsHeaderCardBeforeDeadlinePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VsHeaderCard(
                myNickname = "나",
                opponentNickname = "우건",
                deadlineText = "남은 시간: 5시간 32분",
                deadlineColor = ChallengeTheme.colorScheme.warning,
            )
        }
    }
}

@Preview
@Composable
private fun VsHeaderCardAfterDeadlinePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VsHeaderCard(
                myNickname = "나",
                opponentNickname = "우건",
                deadlineText = "판정 대기 중",
                deadlineColor = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
