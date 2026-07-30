package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 마감 기준 선택 2-up 카드. design.md §4.2.
 *
 * 2지선다이므로 각 옵션에 라디오 시맨틱을 부여한다.
 *
 * @param todayHintText "오늘 자정"의 실제 마감 시각. 포맷팅 금지 규칙 + 진입 시 1회 계산(§8 #14)이라 밖에서 받는다.
 * @param tomorrowHintText "내일 자정"의 실제 마감 시각.
 * @param enabled false 면 클릭을 막고 컴포넌트 전체를 50% 로 흐린다(제출 중).
 */
@Composable
internal fun DeadlineSelector(
    value: DeadlineType,
    onValueChange: (DeadlineType) -> Unit,
    todayHintText: String,
    tomorrowHintText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        Text(
            text = "마감",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )

        VerticalSpacer(6.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DeadlineType.entries.forEach { type ->
                DeadlineOption(
                    type = type,
                    hintText = when (type) {
                        DeadlineType.TODAY -> todayHintText
                        DeadlineType.TOMORROW -> tomorrowHintText
                    },
                    selected = type == value,
                    enabled = enabled,
                    onClick = { onValueChange(type) },
                )
            }
        }

        VerticalSpacer(6.dp)

        Text(
            text = "선택한 자정까지가 챌린지 기간이야",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RowScope.DeadlineOption(
    type: DeadlineType,
    hintText: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val primary = ChallengeTheme.colorScheme.primary
    val shape = RoundedCornerShape(12.dp)
    val labelColor = if (selected) primary else ChallengeTheme.colorScheme.onSurfaceVariant
    val hintColor = if (selected) {
        primary.copy(alpha = 0.80f)
    } else {
        ChallengeTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(60.dp)
            .clip(shape)
            .background(if (selected) primary.copy(alpha = 0.10f) else ChallengeTheme.colorScheme.secondary)
            .then(
                if (selected) {
                    Modifier.border(border = BorderStroke(1.dp, primary), shape = shape)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = labelColor,
                )

                Text(
                    text = type.label,
                    style = ChallengeTheme.typography.medium14,
                    color = labelColor,
                )
            }

            Text(
                text = hintText,
                style = ChallengeTheme.typography.medium12,
                color = hintColor,
            )
        }
    }
}

private val DeadlineType.label: String
    get() = when (this) {
        DeadlineType.TODAY -> "오늘 자정"
        DeadlineType.TOMORROW -> "내일 자정"
    }

@Preview
@Composable
private fun DeadlineSelectorTodayPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            DeadlineSelector(
                value = DeadlineType.TODAY,
                onValueChange = {},
                todayHintText = PREVIEW_TODAY_HINT,
                tomorrowHintText = PREVIEW_TOMORROW_HINT,
            )
        }
    }
}

@Preview
@Composable
private fun DeadlineSelectorTomorrowPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            DeadlineSelector(
                value = DeadlineType.TOMORROW,
                onValueChange = {},
                todayHintText = PREVIEW_TODAY_HINT,
                tomorrowHintText = PREVIEW_TOMORROW_HINT,
            )
        }
    }
}

@Preview
@Composable
private fun DeadlineSelectorDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            DeadlineSelector(
                value = DeadlineType.TODAY,
                onValueChange = {},
                todayHintText = PREVIEW_TODAY_HINT,
                tomorrowHintText = PREVIEW_TOMORROW_HINT,
                enabled = false,
            )
        }
    }
}

private const val PREVIEW_TODAY_HINT = "7/28 24:00"
private const val PREVIEW_TOMORROW_HINT = "7/29 24:00"
