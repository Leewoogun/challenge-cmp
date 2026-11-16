package com.lwg.challenge.designsystem.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

private const val MONTH_COLUMNS = 4

/**
 * 한 해의 12개월 그리드. 연도 이동은 [CalendarHeader] 가 맡는다.
 *
 * @param displayedMonth 현재 캘린더가 보고 있는 달(1일). 같은 연·월 칸을 강조한다.
 */
@Composable
internal fun CalendarYearMonthPicker(
    year: Int,
    displayedMonth: LocalDate,
    boundary: ClosedRange<LocalDate>,
    onMonthClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Month.entries.chunked(MONTH_COLUMNS).forEach { rowMonths ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowMonths.forEach { month ->
                    val firstOfMonth = LocalDate(year, month, 1)
                    MonthChip(
                        text = "${month.ordinal + 1}월",
                        isSelected = firstOfMonth == displayedMonth,
                        enabled = firstOfMonth.overlaps(boundary),
                        onClick = { onMonthClick(firstOfMonth) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun LocalDate.overlaps(boundary: ClosedRange<LocalDate>): Boolean =
    this <= boundary.endInclusive && this >= boundary.start.firstOfMonth

@Composable
private fun MonthChip(
    text: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ChallengeTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier.background(ChallengeTheme.brushes.fire)
                } else {
                    Modifier.background(colors.background.field)
                },
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = if (isSelected) ChallengeTheme.typography.bold14 else ChallengeTheme.typography.medium14,
            color = when {
                isSelected -> colors.text.onPoint
                enabled -> colors.text.primary
                else -> colors.calendar.disabledText
            },
        )
    }
}

@ThemePreviews
@Composable
private fun CalendarYearMonthPickerPreview() {
    ChallengeTheme {
        CalendarYearMonthPicker(
            year = 2026,
            displayedMonth = LocalDate(2026, 11, 1),
            boundary = LocalDate(2026, 3, 15)..LocalDate(2027, 3, 15),
            onMonthClick = {},
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
        )
    }
}
