package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.button.SelectedButton
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.create.contract.DeadlineOption
import kotlinx.datetime.LocalDate

// 3등분 폭에 "12/31(목) 24:00" 이 한 줄로 들어가도록 아이콘을 빼고 좌우 여백을 줄인다
private val DeadlineCardPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)

/**
 * 마감 선택 3-up 카드 (오늘 자정 / 내일 자정 / 날짜 선택).
 *
 * @param onCustomClick 날짜 선택 카드. 캘린더를 연다.
 * @param enabled false 면 클릭을 막고 컴포넌트 전체를 50% 로 흐린다(제출 중).
 */
@Composable
internal fun DeadlineSelector(
    selected: DeadlineOption,
    today: LocalDate,
    tomorrow: LocalDate,
    todayHintText: String,
    tomorrowHintText: String,
    customHintText: String,
    onDateSelect: (LocalDate) -> Unit,
    onCustomClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        Text(
            text = "마감",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.secondary,
        )

        VerticalSpacer(6.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DeadlineOption.entries.forEach { option ->
                SelectedButton(
                    text = option.label,
                    supportingText = when (option) {
                        DeadlineOption.TODAY -> todayHintText
                        DeadlineOption.TOMORROW -> tomorrowHintText
                        DeadlineOption.CUSTOM -> customHintText
                    },
                    selected = option == selected,
                    enabled = enabled,
                    onClick = {
                        when (option) {
                            DeadlineOption.TODAY -> onDateSelect(today)
                            DeadlineOption.TOMORROW -> onDateSelect(tomorrow)
                            DeadlineOption.CUSTOM -> onCustomClick()
                        }
                    },
                    contentPadding = DeadlineCardPadding,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        VerticalSpacer(6.dp)

        Text(
            text = "선택한 자정까지가 챌린지 기간이야",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.secondary,
        )
    }
}

@Composable
private fun DeadlineSelectorPreviewBox(
    selected: DeadlineOption,
    customHintText: String = PREVIEW_CUSTOM_EMPTY_HINT,
    enabled: Boolean = true,
) {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            DeadlineSelector(
                selected = selected,
                today = LocalDate(2026, 11, 16),
                tomorrow = LocalDate(2026, 11, 17),
                todayHintText = PREVIEW_TODAY_HINT,
                tomorrowHintText = PREVIEW_TOMORROW_HINT,
                customHintText = customHintText,
                onDateSelect = {},
                onCustomClick = {},
                enabled = enabled,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun DeadlineSelectorTodayPreview() {
    DeadlineSelectorPreviewBox(selected = DeadlineOption.TODAY)
}

@ThemePreviews
@Composable
private fun DeadlineSelectorTomorrowPreview() {
    DeadlineSelectorPreviewBox(selected = DeadlineOption.TOMORROW)
}

@ThemePreviews
@Composable
private fun DeadlineSelectorCustomPreview() {
    DeadlineSelectorPreviewBox(selected = DeadlineOption.CUSTOM, customHintText = "12/31(목) 24:00")
}

@ThemePreviews
@Composable
private fun DeadlineSelectorDisabledPreview() {
    DeadlineSelectorPreviewBox(selected = DeadlineOption.TODAY, enabled = false)
}

private const val PREVIEW_TODAY_HINT = "11/16(월) 24:00"
private const val PREVIEW_TOMORROW_HINT = "11/17(화) 24:00"
private const val PREVIEW_CUSTOM_EMPTY_HINT = "최대 3개월"
