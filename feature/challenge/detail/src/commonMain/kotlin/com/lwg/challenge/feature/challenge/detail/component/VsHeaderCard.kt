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
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.lwg.challenge.ui.components.ProfilePlaceholder
import com.lwg.challenge.ui.components.challengeOutcomeVisualOf
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 상세 최상단 대결 헤더. 양측 아바타를 "VS" 로 마주 세우고 그 아래 헤드라인 한 줄을 붙인다.
 *
 * 헤드라인 문구·강조를 파라미터로 받는 이유: 이 한 자리의 주인이 "남은 시간: 5시간 32분"(마감 전) →
 * "판정 대기 중"(마감 후) → "승리"(판정 후) 로 바뀌는데, 무엇을 그릴지는 시각 비교뿐 아니라
 * **내가 어느 쪽인가**(관점) 까지 알아야 정해진다. 둘 다 Composable 밖에서 이미 끝나 있어, 여기서는
 * [headlineTone] 을 색·아이콘으로 옮기는 일만 한다.
 *
 * 상호작용이 없다 — 아바타는 표시 전용이라 탭 콜백을 뚫지 않는다.
 *
 * @param forfeitText 기권으로 끝났을 때 헤드라인 아래에 붙는 안내 한 줄. 기권이 아니면 `null` 이고
 *   그 자리가 통째로 빠진다.
 */
@Composable
internal fun VsHeaderCard(
    myNickname: String,
    opponentNickname: String,
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

            // 결과가 아니라 결과가 **어떻게 나왔는지**에 붙는 주석이라 헤드라인의 톤 색(visual.accent)을
            // 쓰지 않는다 — 같은 색을 주면 결과 문구와 경쟁하고, 승패를 말하는 자리가 두 줄로 늘어난
            // 것처럼 읽힌다. 크기도 한 단계 낮춘다.
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

/**
 * 헤드라인 톤을 색·아이콘으로 옮긴다. 색만 따로 고르면 "승리" 에 시계 아이콘이 남는 조합이 생겨
 * 둘을 한 자리에서 같이 집는다.
 *
 * [Icons.Filled.Schedule] 은 **아직 결과가 없을 때만** 쓴다 — 그래서 색이 같은 중립이라도
 * [HeadlineTone.NEUTRAL] 과 [HeadlineTone.DECIDED_NEUTRAL] 이 다른 아이콘으로 갈린다.
 *
 * 승/패/무 세 톤은 홈 카드와 같은 승패무를 말하므로 [challengeOutcomeVisualOf] 에 위임한다 —
 * 상세만 매핑을 따로 들고 있으면 같은 결과가 두 화면에서 다른 색이 된다. 나머지 세 톤은 결과가
 * 아니라 **마감 전·판정 전·관점 미상** 이라는 상세 고유의 사정이라 여기 남는다.
 */
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

    // 누군가는 이겼다는 사실은 참이라 트로피를 쓰고, 그게 보는 사람 것인지 몰라 색만 거둔다.
    HeadlineTone.DECIDED_NEUTRAL -> HeadlineVisual(
        accent = ChallengeTheme.colorScheme.onSurfaceVariant,
        icon = Icons.Filled.EmojiEvents,
    )

    HeadlineTone.WIN -> outcomeVisual(ChallengeOutcome.WIN)

    // 이 톤 하나가 LOSE 와 BOTH_LOSE 를 함께 받는다. 둘은 색·아이콘이 같고 **문구만** 갈리는데
    // 그 문구는 state 가 이미 확정해 [headlineText] 로 넘겨주므로, 여기서 대표로 LOSE 를 넣어도
    // 화면에 새는 차이가 없다. label 을 버리는 것이 이 위임이 성립하는 조건이다.
    HeadlineTone.LOSE -> outcomeVisual(ChallengeOutcome.LOSE)

    HeadlineTone.DRAW -> outcomeVisual(ChallengeOutcome.DRAW)
}

@Composable
private fun outcomeVisual(outcome: ChallengeOutcome): HeadlineVisual =
    with(challengeOutcomeVisualOf(outcome)) { HeadlineVisual(accent = accent, icon = icon) }

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
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            opponentNickname = "우건",
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
            opponentNickname = "우건",
            headlineText = "판정 대기 중",
            headlineTone = HeadlineTone.NEUTRAL,
        )
    }
}

@Preview
@Composable
private fun VsHeaderCardDecidedNeutralPreview() {
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "지훈",
            opponentNickname = "우건",
            headlineText = "우건 승리",
            headlineTone = HeadlineTone.DECIDED_NEUTRAL,
        )
    }
}

@Preview
@Composable
private fun VsHeaderCardWinPreview() {
    VsHeaderCardPreviewContainer {
        VsHeaderCard(
            myNickname = "나",
            opponentNickname = "우건",
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
            opponentNickname = "우건",
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
            opponentNickname = "우건",
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
            opponentNickname = "우건",
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
