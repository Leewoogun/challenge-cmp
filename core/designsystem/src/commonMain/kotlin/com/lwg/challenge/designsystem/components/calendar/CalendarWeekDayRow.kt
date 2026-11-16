package com.lwg.challenge.designsystem.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.utils.datetime.koreanLabel
import kotlinx.datetime.DayOfWeek

@Composable
internal fun CalendarWeekDayRow(
    firstDayOfWeek: DayOfWeek,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        weekDays(firstDayOfWeek).forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.koreanLabel,
                style = ChallengeTheme.typography.medium12,
                color = when (dayOfWeek) {
                    DayOfWeek.SUNDAY -> ChallengeTheme.colorScheme.calendar.sundayText
                    DayOfWeek.SATURDAY -> ChallengeTheme.colorScheme.calendar.saturdayText
                    else -> ChallengeTheme.colorScheme.text.secondary
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CalendarWeekDayRowPreview() {
    ChallengeTheme {
        CalendarWeekDayRow(
            firstDayOfWeek = DayOfWeek.SUNDAY,
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
        )
    }
}
