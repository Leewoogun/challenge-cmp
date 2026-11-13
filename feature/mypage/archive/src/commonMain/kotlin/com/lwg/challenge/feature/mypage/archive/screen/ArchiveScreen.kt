package com.lwg.challenge.feature.mypage.archive.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.feature.mypage.archive.PreviewArchiveHistories
import com.lwg.challenge.feature.mypage.archive.PreviewLongTextArchiveHistories
import com.lwg.challenge.feature.mypage.archive.component.ArchiveChallengeList
import com.lwg.challenge.feature.mypage.archive.component.ArchiveEmptyMonth
import com.lwg.challenge.feature.mypage.archive.component.ArchiveMonthNavBar
import com.lwg.challenge.feature.mypage.archive.component.ArchiveNoArchiveEver
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiState
import kotlinx.datetime.YearMonth

/** 계약서 보관함 본문. 월 이동 바는 목록 밖에 고정한다 — 목록 안에 두면 끝까지 내린 뒤 달을 바꾸기 어렵다. */
@Composable
internal fun ArchiveScreen(
    uiState: ArchiveUiState.Data,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ArchiveMonthNavBar(
            label = uiState.monthLabel,
            countText = uiState.countText,
            canGoPrevious = true,
            canGoNext = uiState.canGoNext,
            onPreviousClick = onPreviousMonthClick,
            onNextClick = onNextMonthClick,
        )

        // 0건은 계정 전체 0건(CTA)과 그 달만 0건(빈 달) 두 화면으로 갈린다.
        Box(modifier = Modifier.weight(1f)) {
            when {
                !uiState.isEmpty -> ArchiveChallengeList(
                    challenges = uiState.challenges,
                    onChallengeClick = onChallengeClick,
                )

                uiState.showsNoRecordsEver -> ArchiveNoArchiveEver(onCreateChallengeClick = onCreateChallengeClick)

                else -> ArchiveEmptyMonth(
                    title = uiState.emptyMonthTitle,
                    subtitle = uiState.emptyMonthSubtitle,
                )
            }
        }
    }
}

/** 이번 달 기본 상태 — 상한이라 `>` 만 비활성이다. */
@ThemePreviews
@Composable
private fun ArchiveScreenPreview() {
    ArchiveScreenPreviewContainer(previewData(challenges = PreviewArchiveHistories))
}

/** 빈 달(A형) — 첫 기록보다 뒤라 더 가면 기록이 있다. */
@ThemePreviews
@Composable
private fun ArchiveScreenEmptyMonthPreview() {
    ArchiveScreenPreviewContainer(previewData(selected = YearMonth(2026, 3)))
}

/** 빈 달(B형) — 첫 기록(5월)보다 앞이라 부제가 첫 기록 월을 알린다. */
@ThemePreviews
@Composable
private fun ArchiveScreenEmptyMonthBeforeFirstRecordPreview() {
    ArchiveScreenPreviewContainer(
        previewData(selected = YearMonth(2026, 3), firstArchivedMonth = YearMonth(2026, 5)),
    )
}

/** 기록 전무 + 이번 달 — 여기만 CTA 가 붙는다. */
@ThemePreviews
@Composable
private fun ArchiveScreenNoArchiveEverPreview() {
    ArchiveScreenPreviewContainer(previewData(firstArchivedMonth = null))
}

/** 기록 전무 + 과거 달 — 데이터는 같아도 CTA 없이 빈 달 화면이다. */
@ThemePreviews
@Composable
private fun ArchiveScreenNoArchiveEverPastMonthPreview() {
    ArchiveScreenPreviewContainer(previewData(selected = YearMonth(2026, 3), firstArchivedMonth = null))
}

@ThemePreviews
@Composable
private fun ArchiveScreenLongTextPreview() {
    ArchiveScreenPreviewContainer(previewData(challenges = PreviewLongTextArchiveHistories))
}

private val PreviewThisMonth = YearMonth(2026, 8)

private fun previewData(
    selected: YearMonth = PreviewThisMonth,
    challenges: List<ChallengeHistory> = emptyList(),
    firstArchivedMonth: YearMonth? = YearMonth(2025, 1),
) = ArchiveUiState.Data(
    selectedMonth = selected,
    challenges = challenges,
    firstArchivedMonth = firstArchivedMonth,
    canGoNext = selected < PreviewThisMonth,
    isThisMonth = selected == PreviewThisMonth,
)

@Composable
private fun ArchiveScreenPreviewContainer(uiState: ArchiveUiState.Data) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            ArchiveScreen(
                uiState = uiState,
                onPreviousMonthClick = {},
                onNextMonthClick = {},
                onChallengeClick = {},
                onCreateChallengeClick = {},
            )
        }
    }
}
