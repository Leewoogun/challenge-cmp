package com.lwg.challenge.designsystem.components.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

internal val DayCellHeight = 44.dp
private val DayCircleSize = 40.dp

internal enum class RangeBand {
    None,
    Start,
    Middle,
    End,
}

@Immutable
internal data class CalendarDay(
    val date: LocalDate,
    val isInPage: Boolean,
    val isSelectable: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean,
    val rangeBand: RangeBand,
) {
    val isClickable: Boolean get() = isInPage && isSelectable
}

/**
 * @param isRowStart 주의 첫 칸. 기간 띠의 왼쪽 끝을 둥글게 마감한다.
 * @param isRowEnd 주의 마지막 칸. 기간 띠의 오른쪽 끝을 둥글게 마감한다.
 */
@Composable
internal fun CalendarDayCell(
    day: CalendarDay,
    isRowStart: Boolean,
    isRowEnd: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ChallengeTheme.colorScheme
    val textColor by animateColorAsState(
        when {
            !day.isInPage || !day.isSelectable -> colors.calendar.disabledText
            day.isSelected -> colors.text.onPoint
            day.isToday -> colors.text.point
            day.date.dayOfWeek == DayOfWeek.SUNDAY -> colors.calendar.sundayText
            day.date.dayOfWeek == DayOfWeek.SATURDAY -> colors.calendar.saturdayText
            else -> colors.text.primary
        },
    )

    Box(
        modifier = modifier.height(DayCellHeight),
        contentAlignment = Alignment.Center,
    ) {
        if (day.isInPage) {
            RangeBandBackground(band = day.rangeBand, isRowStart = isRowStart, isRowEnd = isRowEnd)
        }

        Box(
            modifier = Modifier
                .size(DayCircleSize)
                .clip(CircleShape)
                .then(if (day.isSelected && day.isInPage) Modifier.background(ChallengeTheme.brushes.fire) else Modifier)
                .clickable(enabled = day.isClickable) { onClick(day.date) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.day.toString(),
                style = if (day.isSelected) ChallengeTheme.typography.bold14 else ChallengeTheme.typography.medium14,
                color = textColor,
            )
            if (day.isToday && day.isInPage) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 5.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (day.isSelected) colors.icon.onPoint else colors.point),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.RangeBandBackground(band: RangeBand, isRowStart: Boolean, isRowEnd: Boolean) {
    val bandModifier = Modifier
        .height(DayCircleSize)
        .background(ChallengeTheme.colorScheme.calendar.rangeBackground)

    when (band) {
        RangeBand.None -> Unit
        RangeBand.Start -> if (!isRowEnd) {
            Box(modifier = bandModifier.fillMaxWidth(0.5f).align(Alignment.CenterEnd))
        }
        RangeBand.End -> if (!isRowStart) {
            Box(modifier = bandModifier.fillMaxWidth(0.5f).align(Alignment.CenterStart))
        }
        RangeBand.Middle -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DayCircleSize)
                .clip(
                    RoundedCornerShape(
                        topStartPercent = if (isRowStart) 50 else 0,
                        bottomStartPercent = if (isRowStart) 50 else 0,
                        topEndPercent = if (isRowEnd) 50 else 0,
                        bottomEndPercent = if (isRowEnd) 50 else 0,
                    ),
                )
                .background(ChallengeTheme.colorScheme.calendar.rangeBackground),
        )
    }
}

@ThemePreviews
@Composable
private fun CalendarDayCellPreview() {
    val base = CalendarDay(
        date = LocalDate(2026, 11, 13),
        isInPage = true,
        isSelectable = true,
        isSelected = false,
        isToday = false,
        rangeBand = RangeBand.None,
    )
    val days = listOf(
        base,
        base.copy(isToday = true),
        base.copy(isSelected = true),
        base.copy(isSelected = true, isToday = true),
        base.copy(isSelected = true, rangeBand = RangeBand.Start),
        base.copy(rangeBand = RangeBand.Middle),
        base.copy(isSelected = true, rangeBand = RangeBand.End),
        base.copy(isSelectable = false),
        base.copy(date = LocalDate(2026, 11, 15)),
        base.copy(date = LocalDate(2026, 11, 14)),
    )

    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
        ) {
            days.forEachIndexed { index, day ->
                CalendarDayCell(
                    day = day,
                    isRowStart = index == 0,
                    isRowEnd = index == days.lastIndex,
                    onClick = {},
                    modifier = Modifier.size(width = 44.dp, height = DayCellHeight),
                )
            }
        }
    }
}

