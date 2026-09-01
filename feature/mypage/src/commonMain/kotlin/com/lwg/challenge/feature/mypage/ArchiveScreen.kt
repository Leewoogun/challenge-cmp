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
                // 과거 하한이 폐기돼 `<` 는 항상 열려 있다 — 기록이 없는 달로도 계속 갈 수 있고 그때는
                // 빈 달 화면이 나온다. 컴포넌트는 파라미터를 그대로 두는데, 경계를 표현하는 능력까지
                // 없애면 하한이 되살아날 때 컴포넌트를 다시 뜯어야 한다.
                canGoPrevious = true,
                canGoNext = month.canGoNext,
                onPreviousClick = onPreviousMonthClick,
                onNextClick = onNextMonthClick,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val content = uiState.content) {
                // 도달하지 않는 가지다. 월 전환이 본문을 비우는 대신 이전 달을 유지하게 되면서
                // Blank 를 만드는 경로가 없어졌다 (ArchiveContent.Blank). 타입이 남아 있어
                // exhaustive 를 위해 자리만 지킨다.
                ArchiveContent.Blank -> Unit

                ArchiveContent.Loading -> ArchiveLoadingScreen()

                // 0건과 실패는 다른 사실이라 실패를 빈 상태로 그리지 않는다 — 증거 보관 화면에서
                // "기록이 하나도 없다" 는 거짓이 가장 나쁘다 (design.md §2.5.4.1).
                ArchiveContent.Failure -> ArchiveFailureScreen(onRetryClick = onRetryClick)

                is ArchiveContent.Data -> ArchiveMonthBody(
                    uiState = uiState,
                    content = content,
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
 * 가르는 값은 [ArchiveUiState.showsNoRecordsEver] 다 — 계정 전체가 0건이면서 **지금 보고 있는 달이
 * 이번 달**일 때만 CTA 화면이고, 나머지 0건은 전부 빈 달 화면이다.
 *
 * 둘을 하나로 합치면 기록이 있는 사용자에게 *"아직 끝낸 챌린지가 없어요"* 라고 **거짓말**을 하게
 * 된다. 증거 보관 화면에서 그 거짓이 가장 나쁘다.
 *
 * 화면은 `firstArchivedMonth` 를 직접 비교하지 않는다 — 판별은 전부 [ArchiveUiState] 의 계산
 * 프로퍼티가 끝내 놓고 여기서는 읽기만 한다. 그 월 값으로 이동을 막는 오용을 구조적으로 멀리 둔다.
 */
@Composable
private fun ArchiveMonthBody(
    uiState: ArchiveUiState,
    content: ArchiveContent.Data,
    onChallengeClick: (Long) -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
    val month = uiState.month

    when {
        !content.isEmpty -> ArchiveChallengeList(
            challenges = content.challenges,
            onChallengeClick = onChallengeClick,
        )

        // 하한이 없어져 기록 0건 사용자도 과거 달로 갈 수 있게 됐고, 그래서 CTA 발동이 좁아졌다 —
        // 기록이 0건이어도 **과거 달에서는 빈 달 화면**이다. "챌린지 만들기" 는 지금 행동할 수 있는
        // 자리에서만 맞는 말이고, 5월을 들여다보는 사용자는 회고 중이지 챌린지를 만들러 온 게 아니다.
        // 첫 진입이 항상 이번 달이라 이렇게 좁혀도 노출 기회는 잃지 않는다.
        //
        // month == null 을 여기서 함께 흡수한다. 아래 빈 달 화면이 월 제목을 필요로 하는데, 그 순간
        // (첫 로딩·첫 로딩 실패)에는 본문이 Data 가 아니라 애초에 이 가지에 닿지 않는다.
        uiState.showsNoRecordsEver || month == null ->
            ArchiveNoArchiveEver(onCreateChallengeClick = onCreateChallengeClick)

        else -> ArchiveEmptyMonth(
            title = month.emptyMonthTitle,
            subtitle = uiState.emptyMonthSubtitle,
        )
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
 * **첫 로딩 실패에서만 그려진다.** 월 전환 실패는 이전 달을 화면에 남긴 채 스낵바로만 알리므로 이
 * 본문에 닿지 않는다 ([ArchiveContent.Failure]). 그때 `month == null` 이라 월 이동 바도 없고,
 * 여기 "다시 시도" 가 유일한 복구 수단이다.
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

/** 이번 달(8월)을 보고 있는 기본 상태 — 상한이라 `>` 만 비활성이고 `<` 는 언제나 열려 있다. */
@Preview
@Composable
private fun ArchiveScreenPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(),
            content = ArchiveContent.Data(PreviewArchiveHistories),
            firstArchivedMonth = PreviewFirstArchivedMonth,
        ),
    )
}

/**
 * 응답이 [ArchiveViewModel] 의 힌트 지연을 넘겨 늦은 월 전환 — **위 기본 프리뷰와 라벨도 카드도
 * 같다.** 달라지는 것은 건수 자리의 "불러오는 중" 하나뿐이다.
 *
 * 이 화면이 이번 개정의 요지다: 전환 중에도 화면이 이전 달을 **온전히** 말하므로 두 달이 섞이지
 * 않고, 힌트가 이미 있던 슬롯에 들어가므로 기하도 흔들리지 않는다.
 */
@Preview
@Composable
private fun ArchiveScreenTransitioningPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(),
            content = ArchiveContent.Data(PreviewArchiveHistories),
            firstArchivedMonth = PreviewFirstArchivedMonth,
            isTransitioning = true,
        ),
    )
}

/** A형 빈 달 — 첫 기록(2025년 1월)보다 뒤라 더 가면 기록이 있는 달이 나온다. */
@Preview
@Composable
private fun ArchiveScreenEmptyMonthPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 3)),
            content = ArchiveContent.Data(emptyList()),
            firstArchivedMonth = PreviewFirstArchivedMonth,
        ),
    )
}

/**
 * B형 빈 달 — 표시월(3월)이 첫 기록(5월)보다 앞이라 부제가 첫 기록 월을 알린다.
 * 이 방향엔 영원히 아무것도 없다는 사실을, 차단 대신 이 한 줄이 말한다.
 */
@Preview
@Composable
private fun ArchiveScreenEmptyMonthBeforeFirstRecordPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 3)),
            content = ArchiveContent.Data(emptyList()),
            firstArchivedMonth = YearMonth(2026, 5),
        ),
    )
}

/** 기록 전무 — 빈 달이 아니라 계정 전체가 0건이고 **이번 달**이다. 여기만 CTA 가 붙는다. */
@Preview
@Composable
private fun ArchiveScreenNoArchiveEverPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(),
            content = ArchiveContent.Data(emptyList()),
            firstArchivedMonth = null,
        ),
    )
}

/**
 * 기록 0건인 계정이 과거 달로 걸어 들어간 모습 — **CTA 가 없다.** 위 기록 전무 프리뷰와 데이터는
 * 같고 달만 다른데 화면이 갈리는 것이 이번 개정의 요점이다. 말할 "첫 기록" 이 없으므로 부제는 A형이다.
 */
@Preview
@Composable
private fun ArchiveScreenNoArchiveEverPastMonthPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 3)),
            content = ArchiveContent.Data(emptyList()),
            firstArchivedMonth = null,
        ),
    )
}

/**
 * 바가 있는 채로 실패한 모습. **지금은 도달할 수 없다** — 월 전환 실패가 화면을 덮는 대신 이전 달을
 * 남기게 되면서, 실패 본문이 나오는 순간은 바가 아직 없는 첫 로딩뿐이다. 화면에 성공한 목록이 있을
 * 때 그것을 실패 카드로 덮지 않기로 한 판단을 눈으로 고정하려고 남긴다.
 */
@Preview
@Composable
private fun ArchiveScreenFailurePreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 5)),
            content = ArchiveContent.Failure,
            firstArchivedMonth = PreviewFirstArchivedMonth,
        ),
    )
}

/**
 * 같은 이유로 도달할 수 없는 조합 — 바가 있는 로딩이다. 월 전환은 스피너 대신 이전 달을 유지하고
 * 건수 슬롯에 힌트만 얹는다 (위 전환 중 프리뷰).
 */
@Preview
@Composable
private fun ArchiveScreenLoadingPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(selected = YearMonth(2026, 5)),
            content = ArchiveContent.Loading,
            firstArchivedMonth = PreviewFirstArchivedMonth,
        ),
    )
}

/** 첫 로딩 실패 — 바가 없어 "다시 시도" 가 유일한 복구 수단인, 실패 본문의 유일한 도달 경로다. */
@Preview
@Composable
private fun ArchiveScreenFirstLoadFailurePreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = null,
            content = ArchiveContent.Failure,
            firstArchivedMonth = null,
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
        ArchiveUiState(
            month = null,
            content = ArchiveContent.Loading,
            firstArchivedMonth = null,
        ),
    )
}

@Preview
@Composable
private fun ArchiveScreenLongTextPreview() {
    ArchiveScreenPreviewContainer(
        ArchiveUiState(
            month = previewCursor(),
            content = ArchiveContent.Data(PreviewLongTextArchiveHistories),
            firstArchivedMonth = PreviewFirstArchivedMonth,
        ),
    )
}

private val PreviewFirstArchivedMonth = YearMonth(2025, 1)

private fun previewCursor(
    selected: YearMonth = YearMonth(2026, 8),
    latest: YearMonth = YearMonth(2026, 8),
) = ArchiveMonthCursor(selected = selected, latest = latest)

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
