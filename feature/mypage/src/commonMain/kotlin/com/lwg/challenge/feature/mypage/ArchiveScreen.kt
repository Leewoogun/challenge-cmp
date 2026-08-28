package com.lwg.challenge.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.mypage.component.ArchiveChallengeCard
import com.lwg.challenge.feature.mypage.component.ArchiveEmptyMonth
import com.lwg.challenge.feature.mypage.component.ArchiveMonthNavBar
import com.lwg.challenge.feature.mypage.contract.ArchiveChallengeItemState
import com.lwg.challenge.feature.mypage.contract.ArchiveContent
import com.lwg.challenge.feature.mypage.contract.ArchiveMonthCursor
import com.lwg.challenge.feature.mypage.contract.ArchiveUiState
import com.lwg.challenge.ui.components.EmptyStateCard
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약서 보관함 본문. design.md §2.3.
 *
 * 월 이동 바는 목록 **바깥에 고정**이라 스크롤하지 않는다. 목록 안에 두면 카드를 끝까지 내려본
 * 사용자가 달을 바꾸려고 맨 위까지 되돌아가야 하고, 로딩·실패 상태에는 목록 자체가 없어 이동 수단이
 * 함께 사라진다 — 조회에 실패한 달에 갇힌다.
 *
 * 바를 그릴지 말지가 `month != null` 하나로 갈린다. null 인 순간은 첫 로딩과 첫 로딩 실패뿐이고,
 * 그때는 라벨도 경계도 참인 값이 없어 그릴 것이 없다 ([ArchiveUiState.month]).
 */
@Composable
internal fun ArchiveScreen(
    uiState: ArchiveUiState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val month = uiState.month
        if (month != null) {
            ArchiveMonthNavBar(
                label = month.label,
                countText = uiState.countText,
                canGoPrevious = month.canGoPrevious,
                canGoNext = month.canGoNext,
                onPreviousClick = onPreviousMonthClick,
                onNextClick = onNextMonthClick,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val content = uiState.content) {
                // 아무것도 그리지 않는다. 월 전환 직후 300ms 는 본문을 비워만 두는 구간이라
                // 스피너도, "이 달엔 없다" 도 아직 그릴 때가 아니다 (ArchiveContent.Blank).
                ArchiveContent.Blank -> Unit

                ArchiveContent.Loading -> ArchiveLoadingScreen()

                // 0건과 실패는 다른 사실이라 실패를 빈 상태로 그리지 않는다 — 증거 보관 화면에서
                // "기록이 하나도 없다" 는 거짓이 가장 나쁘다 (design.md §2.5.4.1).
                ArchiveContent.Failure -> ArchiveFailureScreen(onRetryClick = onRetryClick)

                is ArchiveContent.Data -> ArchiveMonthBody(
                    content = content,
                    month = month,
                    onChallengeClick = onChallengeClick,
                    onCreateChallengeClick = onCreateChallengeClick,
                )
            }
        }
    }
}

/**
 * 조회에 성공한 달의 본문. 0건이 **두 화면으로 갈린다.**
 *
 * 가르는 값은 [ArchiveMonthCursor.earliest] 다 — non-null 이면 다른 달에 기록이 있다는 뜻이라
 * *"이 달만 비었다"* 이고, null 이면 종료한 챌린지가 계정 전체에 하나도 없다.
 *
 * 둘을 하나로 합치면 기록이 있는 사용자에게 *"아직 끝낸 챌린지가 없어요"* 라고 **거짓말**을 하게
 * 된다. 증거 보관 화면에서 그 거짓이 가장 나쁘다.
 *
 * CTA 도 기록 전무에서만 맞는다. 빈 달의 사용자는 기록을 **찾는 중**이라 "챌린지 만들기" 가 그를
 * 보관함 밖으로 내보낸다 — 동선이 반대다.
 */
@Composable
private fun ArchiveMonthBody(
    content: ArchiveContent.Data,
    month: ArchiveMonthCursor?,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
    when {
        !content.isEmpty -> ArchiveChallengeList(
            challenges = content.challenges,
            onChallengeClick = onChallengeClick,
        )

        month != null && month.earliest != null -> ArchiveEmptyMonth(title = month.emptyMonthTitle)

        else -> ArchiveNoArchiveEver(onCreateChallengeClick = onCreateChallengeClick)
    }
}

/**
 * 좌우 gutter 를 `contentPadding` 이 아니라 항목마다 넣는 이유는 1차 판단을 그대로 승계한다 —
 * 카드가 좌우로 스크롤 인디케이터와 겹치지 않게 카드 자신이 여백을 낸다.
 *
 * `contentPadding` 의 세로 8dp 는 사유가 바뀌었다: 1차엔 첫 월 헤더의 윗숨이었지만 지금은
 * **월 이동 바의 divider 바로 아래에 첫 카드가 붙지 않게** 하는 여백이다.
 */
@Composable
private fun ArchiveChallengeList(
    challenges: List<ArchiveChallengeItemState>,
    onChallengeClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(items = challenges, key = { it.challengeId }) { item ->
            ArchiveChallengeCard(
                item = item,
                onClick = { onChallengeClick(item.challengeId) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }
    }
}

/** 종료한 챌린지가 계정 전체에 하나도 없는 상태. design.md §2.5.1. */
@Composable
private fun ArchiveNoArchiveEver(onCreateChallengeClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        EmptyStateCard(
            title = "아직 끝낸 챌린지가 없어요",
            subtitle = "챌린지가 끝나면 계약서가 여기 쌓여요",
            ctaLabel = "챌린지 만들기",
            icon = Icons.Filled.Description,
            onClickCta = onCreateChallengeClick,
            // 기본값이 PersonAdd 라 그냥 두면 "챌린지 만들기" 에 친구 추가 아이콘이 붙는다.
            ctaIcon = Icons.Filled.SportsKabaddi,
        )
    }
}

/**
 * 조회 실패 본문. design.md §2.5.4.1.
 *
 * 빈 상태 카드와 골격을 공유하되 아이콘까지 바꾸는 이유: 둘 다 "목록이 없는 화면" 이라 문구만 다르면
 * 0건인지 못 불러온 건지 한눈에 갈리지 않는다.
 *
 * 월 이동이 생기면서 실패가 **특정 달에 귀속**된다 — 옆 달로 넘어가는 것도 복구 수단이라 월 이동 바는
 * 실패 상태에서도 살아 있다. 예외는 첫 로딩 실패뿐이다. 그때는 `month == null` 이라 바를 그릴 수
 * 없고 "다시 시도" 만 남는다.
 */
@Composable
internal fun ArchiveFailureScreen(onRetryClick: () -> Unit) {
    // 빈 상태 카드와 같은 자리에 놓는다(목록의 contentPadding + 항목 gutter 와 동일) —
    // 재시도로 상태가 바뀔 때 카드가 다른 높이에서 다시 그려지면 튄다.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        EmptyStateCard(
            title = "보관함을 불러오지 못했어요",
            subtitle = "잠시 후 다시 시도해주세요",
            ctaLabel = "다시 시도",
            icon = Icons.Filled.CloudOff,
            onClickCta = onRetryClick,
            // 기본값이 PersonAdd 라 그냥 두면 "다시 시도" 에 친구 추가 아이콘이 붙는다.
            ctaIcon = Icons.Filled.Refresh,
        )
    }
}

@Composable
internal fun ArchiveLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
    }
}

/** 이번 달(8월)을 보고 있는 기본 상태 — 상한이라 `>` 가 비활성이다. */
@Preview
@Composable
private fun ArchiveScreenPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(),
            content = ArchiveContent.Data(PreviewArchiveHistories),
        ),
    )
}

/** 기록이 있는 달들 사이의 빈 달 — CTA 없이 "화살표로 다른 달을 볼 수 있어요" 만 남는다. */
@Preview
@Composable
private fun ArchiveScreenEmptyMonthPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 3)),
            content = ArchiveContent.Data(emptyList()),
        ),
    )
}

/** 기록 전무 — `earliest` 가 null 이라 빈 달이 아니라 계정 전체가 0건이고, 여기만 CTA 가 붙는다. */
@Preview
@Composable
private fun ArchiveScreenNoArchiveEverPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(earliest = null),
            content = ArchiveContent.Data(emptyList()),
        ),
    )
}

/** 두 번째 이후 실패 — 바가 살아 있어 옆 달로 넘어가는 것도 복구 수단이다. */
@Preview
@Composable
private fun ArchiveScreenFailurePreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 5)),
            content = ArchiveContent.Failure,
        ),
    )
}

@Preview
@Composable
private fun ArchiveScreenLoadingPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 5)),
            content = ArchiveContent.Loading,
        ),
    )
}

/**
 * 첫 로딩 — 아직 서버가 이번 달을 알려주지 않아 **월 이동 바가 아예 없다.**
 * 없는 정보를 위해 빈 바를 미리 잡아 두지 않는다는 판단을 눈으로 고정한다.
 */
@Preview
@Composable
private fun ArchiveScreenFirstLoadPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(month = null, content = ArchiveContent.Loading),
    )
}

@Preview
@Composable
private fun ArchiveScreenLongTextPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(),
            content = ArchiveContent.Data(PreviewLongTextArchiveHistories),
        ),
    )
}

private fun previewCursor(
    selected: YearMonth = YearMonth(2026, 8),
    latest: YearMonth = YearMonth(2026, 8),
    earliest: YearMonth? = YearMonth(2025, 11),
) = ArchiveMonthCursor(selected = selected, latest = latest, earliest = earliest)

@Composable
private fun ArchiveScreenPreviewContainer(uiState: ArchiveUiState) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveScreen(
                uiState = uiState,
                onPreviousMonthClick = {},
                onNextMonthClick = {},
                onChallengeClick = {},
                onCreateChallengeClick = {},
                onRetryClick = {},
            )
        }
    }
}
