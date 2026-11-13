package com.lwg.challenge.feature.home.component

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
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 진행 중인 챌린지가 없을 때 홈에 띄우는 빈 상태 카드.
 *
 * @param onClickAddFriend "친구 등록" CTA 클릭.
 * @param onClickCreateChallenge "챌린지 만들기" CTA 클릭.
 */
@Composable
internal fun NoChallengeSection(
    onClickAddFriend: () -> Unit,
    onClickCreateChallenge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = ChallengeTheme.colorScheme.point

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
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

            VerticalSpacer(16.dp)

            Text(
                text = "진행 중인 챌린지가 없어요",
                style = ChallengeTheme.typography.bold16,
                color = ChallengeTheme.colorScheme.text.primary,
            )

            VerticalSpacer(4.dp)

            Text(
                text = "친구에게 새 챌린지를 걸어보세요",
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.text.secondary,
            )

            VerticalSpacer(20.dp)

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

@ThemePreviews
@Composable
private fun NoChallengeSectionPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
        ) {
            NoChallengeSection(
                onClickAddFriend = {},
                onClickCreateChallenge = {},
            )
        }
    }
}
