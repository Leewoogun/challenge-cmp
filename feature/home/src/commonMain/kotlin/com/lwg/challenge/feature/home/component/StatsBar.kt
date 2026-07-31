package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 홈 화면 상단의 4분할 전적 카드.
 *
 * design.md §4.2 props 명세 그대로.
 * - 4개 셀: 승 / 패 / 무 / 연승
 * - [muted] 이면 4개 셀 전부 muted 톤, 아니면 셀별 의미 색
 *   (승=primary, 패=error, 무=onBackground, 연승=warning). 판정은 State 책임.
 */
@Composable
internal fun StatsBar(
    win: Int,
    lose: Int,
    draw: Int,
    streakText: String,
    muted: Boolean,
    modifier: Modifier = Modifier,
) {
    val mutedColor = ChallengeTheme.colorScheme.onSurfaceVariant

    val winColor = if (muted) mutedColor else ChallengeTheme.colorScheme.primary
    val loseColor = if (muted) mutedColor else ChallengeTheme.colorScheme.error
    val drawColor = if (muted) mutedColor else ChallengeTheme.colorScheme.onBackground
    val streakColor = if (muted) mutedColor else ChallengeTheme.colorScheme.warning

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color = ChallengeTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = ChallengeTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatCell(label = "승", value = win.toString(), valueColor = winColor)
        CellDivider()
        StatCell(label = "패", value = lose.toString(), valueColor = loseColor)
        CellDivider()
        StatCell(label = "무", value = draw.toString(), valueColor = drawColor)
        CellDivider()
        StatCell(label = "연승", value = streakText, valueColor = streakColor)
    }
}

@Composable
private fun RowScope.StatCell(
    label: String,
    value: String,
    valueColor: Color,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = ChallengeTheme.typography.bold18,
            color = valueColor,
        )
        Text(
            text = label,
            style = ChallengeTheme.typography.medium10,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CellDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(ChallengeTheme.colorScheme.outline),
    )
}

@Preview
@Composable
private fun StatsBarPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            StatsBar(win = 12, lose = 3, draw = 1, streakText = "4🔥", muted = false)
        }
    }
}

@Preview
@Composable
private fun StatsBarEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            StatsBar(win = 0, lose = 0, draw = 0, streakText = "0", muted = true)
        }
    }
}
