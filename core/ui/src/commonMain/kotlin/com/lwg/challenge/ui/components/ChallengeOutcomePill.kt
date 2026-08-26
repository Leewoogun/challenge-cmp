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
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import org.jetbrains.compose.ui.tooling.preview.Preview

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
 *
 * 인증 상태(대기중/인증완료/실패)를 그리는 [VerificationStatusPill] 과 모양만 같고 축이 다르다 —
 * 한쪽은 "미션을 인증했는가", 이쪽은 "챌린지가 어떻게 끝났는가" 다. 한 카드에 둘이 동시에 나오므로
 * 통합하지 말 것.
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

/** 승패무 한 건의 색·문구·아이콘을 한 묶음으로 확정한 값. */
data class ChallengeOutcomeVisual(
    val accent: Color,
    val label: String,
    val icon: ImageVector,
)

/**
 * 판정 결과를 이 앱의 승패무 표현으로 옮기는 **단일 출처**.
 *
 * 승=primary / 패=error / 무=onBackground 는 홈 전적 카드(StatsBar) 가 이미 정해 둔 색 언어이고,
 * 실패에 [Icons.Filled.Cancel] 을 쓰는 것도 [VerificationStatusPill] 과 같다. 홈 카드와 상세 헤더가
 * 각자 매핑을 들고 있으면 같은 전적이 두 화면에서 다른 색이 되므로 여기 한 곳에만 둔다 —
 * 상세가 모듈 밖에서 호출해야 해서 `internal` 이 아닌 공개 API 다.
 *
 * [ChallengeOutcome.LOSE] 와 [ChallengeOutcome.BOTH_LOSE] 는 **색·아이콘이 같고 문구만 갈린다.**
 * 문구를 이미 확정해 들고 오는 호출부(상세 헤더)가 이 둘을 하나의 톤으로 합쳐 쓰는 근거이므로,
 * 색이나 아이콘을 갈라야 할 일이 생기면 그 호출부도 같이 봐야 한다.
 */
@Composable
fun challengeOutcomeVisualOf(outcome: ChallengeOutcome): ChallengeOutcomeVisual =
    when (outcome) {
        ChallengeOutcome.WIN -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.primary,
            label = RESULT_WIN_TEXT,
            icon = Icons.Filled.EmojiEvents,
        )
        ChallengeOutcome.LOSE -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.error,
            label = RESULT_LOSE_TEXT,
            icon = Icons.Filled.Cancel,
        )
        ChallengeOutcome.DRAW -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.onBackground,
            label = RESULT_DRAW_TEXT,
            icon = Icons.Filled.HorizontalRule,
        )
        ChallengeOutcome.BOTH_LOSE -> ChallengeOutcomeVisual(
            accent = ChallengeTheme.colorScheme.error,
            label = RESULT_BOTH_LOSE_TEXT,
            icon = Icons.Filled.Cancel,
        )
    }

@Preview
@Composable
private fun ChallengeOutcomePillWinPreview() {
    ChallengeOutcomePillPreviewContainer {
        ChallengeOutcomePill(outcome = ChallengeOutcome.WIN)
    }
}

@Preview
@Composable
private fun ChallengeOutcomePillLosePreview() {
    ChallengeOutcomePillPreviewContainer {
        ChallengeOutcomePill(outcome = ChallengeOutcome.LOSE)
    }
}

@Preview
@Composable
private fun ChallengeOutcomePillDrawPreview() {
    ChallengeOutcomePillPreviewContainer {
        ChallengeOutcomePill(outcome = ChallengeOutcome.DRAW)
    }
}

@Preview
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
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            content()
        }
    }
}
