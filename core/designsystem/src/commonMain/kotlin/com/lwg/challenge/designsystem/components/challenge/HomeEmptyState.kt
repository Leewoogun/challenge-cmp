package com.lwg.challenge.designsystem.components.challenge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.components.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 홈 화면 챌린지 영역의 빈 상태 카드.
 *
 * design.md §2 / §3 / §4.3 — 신규 사용자([HomeEmptyStateType.FIRST_USER]) 와
 * 기존 사용자의 진행 중 챌린지 0개([HomeEmptyStateType.NO_ACTIVE_CHALLENGE]) 분기.
 *
 * 친구/랭킹 등 다른 화면의 빈 상태에도 재사용 가능하도록 컴포넌트 단위로 분리.
 *
 * @param type 빈 상태 분기.
 * @param onClickAddFriend "친구 등록" CTA 클릭.
 * @param onClickCreateChallenge "챌린지 만들기" CTA 클릭.
 */
@Composable
fun HomeEmptyState(
    type: HomeEmptyStateType,
    onClickAddFriend: () -> Unit,
    onClickCreateChallenge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (headline, subline) = textsFor(type)
    val primary = ChallengeTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = primary,
                )
            }

            VerticalGap(16.dp)

            Text(
                text = headline,
                style = ChallengeTheme.typography.bold16,
                color = ChallengeTheme.colorScheme.onBackground,
            )

            VerticalGap(4.dp)

            Text(
                text = subline,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )

            VerticalGap(20.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconTextButton(
                    text = "친구 등록",
                    icon = Icons.Filled.PersonAdd,
                    onClick = onClickAddFriend,
                    modifier = Modifier.weight(1f),
                    style = IconTextButtonStyle.Outlined,
                )
                IconTextButton(
                    text = "챌린지 만들기",
                    icon = Icons.Filled.SportsKabaddi,
                    onClick = onClickCreateChallenge,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun VerticalGap(height: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.height(height))
}

private fun textsFor(type: HomeEmptyStateType): Pair<String, String> = when (type) {
    HomeEmptyStateType.FIRST_USER -> "아직 진행 중인 챌린지가 없어요" to "친구를 등록하고 첫 약속을 걸어보세요"
    HomeEmptyStateType.NO_ACTIVE_CHALLENGE -> "진행 중인 챌린지가 없어요" to "새 챌린지를 시작해 다시 불을 붙여보세요"
}
