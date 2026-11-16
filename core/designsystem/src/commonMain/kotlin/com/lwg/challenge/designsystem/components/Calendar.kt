package com.lwg.challenge.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.calendar.CalendarConfig
import com.lwg.challenge.designsystem.components.calendar.CalendarDay
import com.lwg.challenge.designsystem.components.calendar.CalendarDayCell
import com.lwg.challenge.designsystem.components.calendar.CalendarHeader
import com.lwg.challenge.designsystem.components.calendar.CalendarPaging
import com.lwg.challenge.designsystem.components.calendar.CalendarSelection
import com.lwg.challenge.designsystem.components.calendar.CalendarStyle
import com.lwg.challenge.designsystem.components.calendar.CalendarWeekDayRow
import com.lwg.challenge.designsystem.components.calendar.CalendarYearMonthPicker
import com.lwg.challenge.designsystem.components.calendar.DayCellHeight
import com.lwg.challenge.designsystem.components.calendar.RangeBand
import com.lwg.challenge.designsystem.components.calendar.coerceIn
import com.lwg.challenge.designsystem.components.calendar.firstOfMonth
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * 디자인 시스템 캘린더. 선택값은 호출부가 소유하고, 표시 중인 달(주)·연월 선택 화면 전환은 내부에서 관리한다.
 *
 * 좌우 스와이프 또는 화살표로 이동하며 [CalendarConfig.boundary] 밖으로는 넘어가지 않는다.
 *
 * @param selection 선택 방식과 현재 선택값. 처음 보여줄 달은 [CalendarSelection.anchorDate] 가 없으면 [today] 기준이다.
 * @param onSelectionChange 날짜를 눌렀을 때 [CalendarSelection.select] 가 계산한 다음 선택값
 * @param today 오늘 표시 기준. 기본값은 KST 오늘이다.
 */
@Composable
fun ChallengeCalendar(
    selection: CalendarSelection,
    onSelectionChange: (CalendarSelection) -> Unit,
    modifier: Modifier = Modifier,
    config: CalendarConfig = CalendarConfig(),
    today: LocalDate = remember { nowKst().date },
) {
    CalendarContent(
        selection = selection,
        config = config,
        today = today,
        onDateClick = { date -> onSelectionChange(selection.select(date, config)) },
        modifier = modifier,
    )
}

/**
 * 날짜 하나만 고르는 캘린더.
 */
@Composable
fun ChallengeCalendar(
    selectedDate: LocalDate?,
    onDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    config: CalendarConfig = CalendarConfig(),
    today: LocalDate = remember { nowKst().date },
) {
    CalendarContent(
        selection = CalendarSelection.Single(selectedDate),
        config = config,
        today = today,
        onDateClick = onDateSelect,
        modifier = modifier,
    )
}

@Composable
private fun CalendarContent(
    selection: CalendarSelection,
    config: CalendarConfig,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 페이지 번호가 boundary·style 기준으로 매겨지므로, 둘이 바뀌면 페이저 상태를 새로 만든다
    key(config.boundary, config.style, config.firstDayOfWeek) {
        val paging = remember { CalendarPaging(config) }
        val pagerState = rememberPagerState(
            initialPage = paging.pageOf((selection.anchorDate ?: today).coerceIn(config.boundary)),
            pageCount = { paging.pageCount },
        )
        val scope = rememberCoroutineScope()
        var isPickerOpen by rememberSaveable { mutableStateOf(false) }
        var pickerYear by rememberSaveable { mutableIntStateOf(0) }

        val displayedMonth = paging.displayMonthOf(pagerState.currentPage)
        val showPicker = isPickerOpen && config.yearMonthSelection

        Column(modifier = modifier.fillMaxWidth()) {
            if (showPicker) {
                CalendarHeader(
                    title = "${pickerYear}년",
                    isTitleExpandable = true,
                    isExpanded = true,
                    isPreviousEnabled = pickerYear > config.boundary.start.year,
                    isNextEnabled = pickerYear < config.boundary.endInclusive.year,
                    previousDescription = "이전 해",
                    nextDescription = "다음 해",
                    onTitleClick = { isPickerOpen = false },
                    onPreviousClick = { pickerYear-- },
                    onNextClick = { pickerYear++ },
                )
            } else {
                val unitLabel = if (config.style == CalendarStyle.Month) "달" else "주"
                CalendarHeader(
                    title = "${displayedMonth.year}년 ${displayedMonth.month.ordinal + 1}월",
                    isTitleExpandable = config.yearMonthSelection,
                    isExpanded = false,
                    isPreviousEnabled = pagerState.currentPage > 0,
                    isNextEnabled = pagerState.currentPage < paging.pageCount - 1,
                    previousDescription = "이전 $unitLabel",
                    nextDescription = "다음 $unitLabel",
                    onTitleClick = {
                        pickerYear = displayedMonth.year
                        isPickerOpen = true
                    },
                    onPreviousClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    onNextClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = showPicker,
                transitionSpec = {
                    fadeIn(tween(durationMillis = 220)) togetherWith
                        fadeOut(tween(durationMillis = 120)) using
                        SizeTransform(clip = false)
                },
            ) { picker ->
                if (picker) {
                    CalendarYearMonthPicker(
                        year = pickerYear,
                        displayedMonth = displayedMonth,
                        boundary = config.boundary,
                        onMonthClick = { firstOfMonth ->
                            scope.launch {
                                pagerState.scrollToPage(paging.pageOf(firstOfMonth.coerceIn(config.boundary)))
                            }
                            isPickerOpen = false
                        },
                    )
                } else {
                    Column {
                        CalendarWeekDayRow(firstDayOfWeek = config.firstDayOfWeek)
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) { page ->
                            val weeks = remember(page, selection, config, today) {
                                buildCalendarWeeks(paging, page, selection, config, today)
                            }
                            CalendarPage(
                                weeks = weeks,
                                showAdjacentMonthDays = config.showAdjacentMonthDays,
                                onDateClick = onDateClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarPage(
    weeks: List<List<CalendarDay>>,
    showAdjacentMonthDays: Boolean,
    onDateClick: (LocalDate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEachIndexed { index, day ->
                    if (day.isInPage || showAdjacentMonthDays) {
                        CalendarDayCell(
                            day = day,
                            isRowStart = index == 0,
                            isRowEnd = index == week.lastIndex,
                            onClick = onDateClick,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).height(DayCellHeight))
                    }
                }
            }
        }
    }
}

private fun buildCalendarWeeks(
    paging: CalendarPaging,
    page: Int,
    selection: CalendarSelection,
    config: CalendarConfig,
    today: LocalDate,
): List<List<CalendarDay>> {
    val displayedMonth = paging.displayMonthOf(page)
    return paging.weeksOf(page).map { week ->
        week.map { date ->
            CalendarDay(
                date = date,
                isInPage = config.style == CalendarStyle.Week || date.firstOfMonth == displayedMonth,
                isSelectable = config.isSelectable(date),
                isSelected = selection.isSelected(date),
                isToday = date == today,
                rangeBand = (selection as? CalendarSelection.Range)?.bandOf(date) ?: RangeBand.None,
            )
        }
    }
}

private fun CalendarSelection.Range.bandOf(date: LocalDate): RangeBand = when {
    !isComplete -> RangeBand.None
    isStart(date) -> RangeBand.Start
    isEnd(date) -> RangeBand.End
    isBetween(date) -> RangeBand.Middle
    else -> RangeBand.None
}

private val PreviewToday = LocalDate(2026, 11, 13)

@Composable
private fun CalendarPreviewFrame(title: String, content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.text.secondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(ChallengeTheme.colorScheme.background.card)
                    .padding(16.dp),
            ) {
                content()
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ChallengeCalendarSinglePreview() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate(2026, 11, 20)) }
    CalendarPreviewFrame(title = "Single · 오늘부터 30일") {
        ChallengeCalendar(
            selectedDate = selectedDate,
            onDateSelect = { selectedDate = it },
            config = CalendarConfig(boundary = PreviewToday..PreviewToday.plus(30, DateTimeUnit.DAY)),
            today = PreviewToday,
        )
    }
}

@ThemePreviews
@Composable
private fun ChallengeCalendarRangePreview() {
    var selection by remember {
        mutableStateOf<CalendarSelection>(
            CalendarSelection.Range(start = LocalDate(2026, 11, 13), end = LocalDate(2026, 11, 25)),
        )
    }
    CalendarPreviewFrame(title = "Range · 비활성 날짜 + 이웃 달 숨김") {
        ChallengeCalendar(
            selection = selection,
            onSelectionChange = { selection = it },
            config = CalendarConfig(
                disabledDates = setOf(LocalDate(2026, 11, 27)),
                showAdjacentMonthDays = false,
            ),
            today = PreviewToday,
        )
    }
}

@ThemePreviews
@Composable
private fun ChallengeCalendarMultiplePreview() {
    var selection by remember {
        mutableStateOf<CalendarSelection>(
            CalendarSelection.Multiple(setOf(LocalDate(2026, 11, 3), LocalDate(2026, 11, 13), LocalDate(2026, 11, 19))),
        )
    }
    CalendarPreviewFrame(title = "Multiple · 월요일 시작") {
        ChallengeCalendar(
            selection = selection,
            onSelectionChange = { selection = it },
            config = CalendarConfig(firstDayOfWeek = DayOfWeek.MONDAY),
            today = PreviewToday,
        )
    }
}

@ThemePreviews
@Composable
private fun ChallengeCalendarWeekPreview() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(PreviewToday) }
    CalendarPreviewFrame(title = "Week 스타일 · 연월 선택 끔") {
        ChallengeCalendar(
            selectedDate = selectedDate,
            onDateSelect = { selectedDate = it },
            config = CalendarConfig(style = CalendarStyle.Week, yearMonthSelection = false),
            today = PreviewToday,
        )
    }
}
