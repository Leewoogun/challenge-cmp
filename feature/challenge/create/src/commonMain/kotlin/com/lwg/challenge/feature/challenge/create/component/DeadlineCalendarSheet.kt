package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.ChallengeCalendar
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.calendar.CalendarConfig
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import kotlinx.datetime.LocalDate

/** 마감 날짜를 고르는 바텀시트. 범위 밖 날짜는 캘린더가 탭을 막는다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeadlineCalendarSheet(
    challengeDate: LocalDate,
    today: LocalDate,
    selectableDates: ClosedRange<LocalDate>,
    onDateSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ChallengeTheme.colorScheme.background.card,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ChallengeTheme.colorScheme.line.divider) },
    ) {
        DeadlineCalendarSheetContent(
            challengeDate = challengeDate,
            today = today,
            selectableDates = selectableDates,
            onDateSelect = onDateSelect,
        )
    }
}

@Composable
private fun DeadlineCalendarSheetContent(
    challengeDate: LocalDate,
    today: LocalDate,
    selectableDates: ClosedRange<LocalDate>,
    onDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
        Text(
            text = "마감 날짜 선택",
            style = ChallengeTheme.typography.bold18,
            color = ChallengeTheme.colorScheme.text.primary,
        )

        VerticalSpacer(4.dp)

        Text(
            text = "오늘부터 3개월 뒤까지 고를 수 있어",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.secondary,
        )

        VerticalSpacer(16.dp)

        ChallengeCalendar(
            selectedDate = challengeDate,
            onDateSelect = onDateSelect,
            config = CalendarConfig(boundary = selectableDates),
            today = today,
        )
    }
}

@ThemePreviews
@Composable
private fun DeadlineCalendarSheetContentPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            DeadlineCalendarSheetContent(
                challengeDate = LocalDate(2026, 11, 20),
                today = LocalDate(2026, 11, 16),
                selectableDates = LocalDate(2026, 11, 16)..LocalDate(2027, 2, 16),
                onDateSelect = {},
                modifier = Modifier.background(ChallengeTheme.colorScheme.background.card),
            )
        }
    }
}
