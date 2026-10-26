package com.lwg.challenge.feature.mypage.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.mypage.archive.PreviewArchiveHistories
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiState
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 조회에 성공한 달의 본문. 0건은 계정 전체 0건(CTA)과 그 달만 0건(빈 달) 두 화면으로 갈린다. */
@Composable
internal fun ArchiveMonthBody(
    uiState: ArchiveUiState.Data,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
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

@Preview
@Composable
private fun ArchiveMonthBodyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveMonthBody(
                uiState = ArchiveUiState.Data(
                    selectedMonth = YearMonth(2026, 8),
                    challenges = PreviewArchiveHistories,
                    firstArchivedMonth = YearMonth(2025, 1),
                    canGoNext = false,
                    isThisMonth = true,
                ),
                onChallengeClick = {},
                onCreateChallengeClick = {},
            )
        }
    }
}
