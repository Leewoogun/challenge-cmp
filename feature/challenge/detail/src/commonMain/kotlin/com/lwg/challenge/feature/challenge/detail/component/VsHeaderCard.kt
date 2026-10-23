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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.feature.challenge.detail.contract.HeadlineTone
import com.lwg.challenge.ui.components.ProfileImage
import com.lwg.challenge.ui.components.challengeOutcomeVisualOf
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * @param forfeitText 기권으로 끝났을 때 헤드라인 아래에 붙는 안내 한 줄.
 */
@Composable
internal fun VsHeaderCard(
    myNickname: String,
    myProfileImageUrl: String?,
    opponentNickname: String,
    opponentProfileImageUrl: String?,
    headlineText: String,
    headlineTone: HeadlineTone,
    modifier: Modifier = Modifier,
    forfeitText: String? = null,
) {
    val visual = headlineVisualOf(headlineTone)
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
                PartyColumn(nickname = myNickname, profileImageUrl = myProfileImageUrl)

                VsColumn()

                PartyColumn(nickname = opponentNickname, profileImageUrl = opponentProfileImageUrl)
            }

            VerticalSpacer(16.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = visual.accent,
                )
                Text(
                    text = headlineText,
                    style = ChallengeTheme.typography.medium14,
                    color = visual.accent,
                )
            }

            if (forfeitText != null) {
                VerticalSpacer(4.dp)
                Text(
                    text = forfeitText,
                    modifier = Modifier.fillMaxWidth(),
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private data class HeadlineVisual(
    val accent: Color,
    val icon: ImageVector,
)

@Composable
private fun headlineVisualOf(tone: HeadlineTone): HeadlineVisual = when (tone) {
    HeadlineTone.URGENT -> HeadlineVisual(
        accent = ChallengeTheme.colorScheme.warning,
        icon = Icons.Filled.Schedule,
    )

    HeadlineTone.NEUTRAL -> HeadlineVisual(
        accent = ChallengeTheme.colorScheme.onSurfaceVariant,
        icon = Icons.Filled.Schedule,
    )

    HeadlineTone.WIN -> outcomeVisual(ChallengeOutcome.WIN)

    HeadlineTone.LOSE -> outcomeVisual(ChallengeOutcome.LOSE)

    HeadlineTone.DRAW -> outcomeVisual(ChallengeOutcome.DRAW)
}

@Composable
private fun outcomeVisual(outcome: ChallengeOutcome): HeadlineVisual =
    with(challengeOutcomeVisualOf(outcome)) { HeadlineVisual(accent = accent, icon = icon) }

@Composable
private fun PartyColumn(nickname: String, profileImageUrl: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProfileImage(
            imageUrl = profileImageUrl,
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
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            myProfileImageUrl = null,
            opponentNickname = "우건",
            opponentProfileImageUrl = null,
            headlineText = "남은 시간: 5시간 32분",
            headlineTone = HeadlineTone.URGENT,
        )
    }
}

@Preview
@Composable
private fun VsHeaderCardAfterDeadlinePreview() {
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            myProfileImageUrl = null,
            opponentNickname = "우건",
            opponentProfileImageUrl = null,
            headlineText = "판정 대기 중",
            headlineTone = HeadlineTone.NEUTRAL,
        )
    }
}

@Preview
@Composable
private fun VsHeaderCardWinPreview() {
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            myProfileImageUrl = null,
            opponentNickname = "우건",
            opponentProfileImageUrl = null,
            headlineText = "승리",
            headlineTone = HeadlineTone.WIN,
        )
    }
}

@Preview
@Composable
private fun VsHeaderCardLosePreview() {
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            myProfileImageUrl = null,
            opponentNickname = "우건",
            opponentProfileImageUrl = null,
            headlineText = "패배",
            headlineTone = HeadlineTone.LOSE,
        )
    }
}

@Preview
@Composable
private fun VsHeaderCardDrawPreview() {
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            myProfileImageUrl = null,
            opponentNickname = "우건",
            opponentProfileImageUrl = null,
            headlineText = "무승부",
            headlineTone = HeadlineTone.DRAW,
        )
    }
}

/** 판정 완료 + 기권 안내 — 결과 한 줄 아래에 어떻게 끝났는지가 한 단계 낮은 위계로 붙는다. */
@Preview
@Composable
private fun VsHeaderCardForfeitedPreview() {
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            myProfileImageUrl = null,
            opponentNickname = "우건",
            opponentProfileImageUrl = null,
            headlineText = "승리",
            headlineTone = HeadlineTone.WIN,
            forfeitText = "우건님이 기권했어요",
        )
    }
}

@Composable
private fun VsHeaderCardPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            content()
        }
    }
}
