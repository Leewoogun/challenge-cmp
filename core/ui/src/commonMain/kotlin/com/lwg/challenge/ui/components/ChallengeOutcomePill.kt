package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome

/** 내가 이긴 판정. 관점을 모르는 화면은 이 단어 앞에 승자 닉네임만 붙여 같은 어휘를 쓴다. */
const val RESULT_WIN_TEXT = "승리"

/** 내가 진 판정. 둘 다 못 한 경우는 [RESULT_BOTH_LOSE_TEXT] 로 따로 말한다. */
const val RESULT_LOSE_TEXT = "패배"

/** 어느 쪽에서 봐도 같은 문장이라 관점을 알든 모르든 그대로 쓴다. */
const val RESULT_DRAW_TEXT = "무승부"

/** 둘 다 못 했다. [RESULT_LOSE_TEXT] 로 뭉개면 내가 진 것과 아무도 못 한 것이 같아 보인다. */
const val RESULT_BOTH_LOSE_TEXT = "양측 패배"

/**
 * 판정 결과(승리/패배/무승부/양측 패배)를 아이콘 + 라벨 캡슐로 표시한다.
 */
@Composable
fun ChallengeOutcomePill(
    outcome: ChallengeOutcome,
    modifier: Modifier = Modifier,
) {
    val visual = challengeOutcomeVisualOf(outcome)
    Surface(
        modifier = modifier,
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

data class ChallengeOutcomeVisual(
    val accent: Color,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun challengeOutcomeVisualOf(outcome: ChallengeOutcome): ChallengeOutcomeVisual =
    when (outcome) {
        ChallengeOutcome.WIN -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.point,
            label = RESULT_WIN_TEXT,
            icon = Icons.Filled.EmojiEvents,
        )
        ChallengeOutcome.LOSE -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.error,
            label = RESULT_LOSE_TEXT,
            icon = Icons.Filled.Cancel,
        )
        ChallengeOutcome.DRAW -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.text.primary,
            label = RESULT_DRAW_TEXT,
            icon = Icons.Filled.HorizontalRule,
        )
        ChallengeOutcome.BOTH_LOSE -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.error,
            label = RESULT_BOTH_LOSE_TEXT,
            icon = Icons.Filled.Cancel,
        )
    }

@ThemePreviews
@Composable
private fun ChallengeOutcomePillWinPreview() {
    ChallengeOutcomePillPreviewContainer {
        ChallengeOutcomePill(outcome = ChallengeOutcome.WIN)
    }
}

@ThemePreviews
@Composable
private fun ChallengeOutcomePillLosePreview() {
    ChallengeOutcomePillPreviewContainer {
        ChallengeOutcomePill(outcome = ChallengeOutcome.LOSE)
    }
}

@ThemePreviews
@Composable
private fun ChallengeOutcomePillDrawPreview() {
    ChallengeOutcomePillPreviewContainer {
        ChallengeOutcomePill(outcome = ChallengeOutcome.DRAW)
    }
}

@ThemePreviews
@Composable
private fun ChallengeOutcomePillBothLosePreview() {
    ChallengeOutcomePillPreviewContainer {
        ChallengeOutcomePill(outcome = ChallengeOutcome.BOTH_LOSE)
    }
}

@Composable
private fun ChallengeOutcomePillPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            content()
        }
    }
}
