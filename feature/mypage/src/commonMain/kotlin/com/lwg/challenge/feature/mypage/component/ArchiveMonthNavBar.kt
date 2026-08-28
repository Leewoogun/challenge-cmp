package com.lwg.challenge.feature.mypage.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.HorizontalDivider
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 보관함의 월 이동 컨트롤. 1차의 [ArchiveMonthHeader] 를 대체한다.
 *
 * 월 헤더의 성격이 바뀐 컴포넌트다 — 1차에서는 전체 목록을 월별로 끊어 읽히게 하는 **라벨**이었지만,
 * 지금은 무엇을 조회할지 정하는 **이동 컨트롤**이다. 그래서 `stickyHeader` 로 목록 안에 들어가지 않고
 * 목록 **바깥에 고정**으로 놓인다. 고정인 이유는 둘이다:
 * 1. 목록과 함께 스크롤되면 카드를 끝까지 내려본 사용자가 달을 바꾸려고 맨 위까지 되돌아가야 한다.
 * 2. 로딩·실패 상태에는 목록 자체가 없다. 헤더가 목록 안에 있으면 그 상태에서 이동 수단까지 같이
 *    사라져 조회에 실패한 달에 갇힌다.
 *
 * 경계에 닿은 화살표를 숨기지 않고 흐리게만 두는 이유: 숨기면 화살표가 빠진 만큼 가운데 라벨이
 * 좌우로 흔들리고, '여기가 끝' 이라는 사실이 화면에서 사라져 왜 안 눌리는지 알 수 없게 된다.
 * 첫 진입이 항상 이번 달이라 [canGoNext] 가 false 인 상태는 예외가 아니라 기본값이다.
 *
 * 비활성은 [IconButton] 의 `enabled` 로 표현한다. 콜백을 빈 람다로 바꿔 막으면 시각적으로만 꺼지고
 * 접근성 트리에는 여전히 누를 수 있는 버튼으로 남는다.
 *
 * 시각 세부는 `design.md §2.3.3` 확정값을 따른다.
 *
 * 행동 규약 두 가지:
 * - **비활성 화살표를 탭했을 때 토스트·스낵바·햅틱으로 알리지 않는다.** 비활성 표시가 이미 '여기가 끝'
 *   을 말하고 있고, 누를 때마다 메시지가 뜨면 정상적으로 끝에 도달한 것이 오류처럼 읽힌다. 아무 일도
 *   일어나지 않는 것이 정답이다.
 * - **월 라벨은 탭 대상이 아니다 — 월 선택 피커를 만들지 않는다.** 사용자 확정이 '월 1칸씩 순차 이동'
 *   이고 피커는 그 범위 밖인 캘린더 축이다.
 *
 * @param countText 그 달의 기록 수. 0건인 달에도 "0건" 을 그대로 그린다 — 건수 줄을 조건부로 감추면
 *   바 높이가 줄어 화살표가 위로 올라온다. 빈 달을 건너뛰지 않기로 확정돼 화살표 연타가 정상 사용인데,
 *   버튼이 조금이라도 움직이면 그 연타가 어긋난다. 빈 달 화면이 '없다' 를 이미 말하므로 "0건" 은
 *   중복이지만, 중복 한 단어보다 움직이는 버튼이 나쁘다.
 */
@Composable
internal fun ArchiveMonthNavBar(
    label: String,
    countText: String,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeArrow = ChallengeTheme.colorScheme.onBackground
    val inactiveArrow = ChallengeTheme.colorScheme.onBackground.copy(alpha = 0.4f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                // background 가 아니라 surface — 상단바와 같은 색이라 상단바와 월 바가 하나의
                // 크롬 블록으로 읽히고, 그 아래 카드 목록과 층이 갈린다.
                .background(ChallengeTheme.colorScheme.surface)
                // 8dp 는 임의값이 아니다: 8 + (48 - 24) / 2 = 20 이라 48dp 터치 영역 안의 24dp 아이콘
                // 바깥 모서리가 카드 gutter(20dp)와 정확히 같은 세로선에 선다.
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousClick, enabled = canGoPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "이전 달",
                    modifier = Modifier.size(24.dp),
                    tint = if (canGoPrevious) activeArrow else inactiveArrow,
                )
            }

            // 가운데가 남는 폭을 전부 먹어야 라벨 길이(1월 ↔ 12월)가 달라져도 화살표가 좌우 끝에 고정된다.
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = ChallengeTheme.typography.bold16,
                    color = ChallengeTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = countText,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            IconButton(onClick = onNextClick, enabled = canGoNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "다음 달",
                    modifier = Modifier.size(24.dp),
                    tint = if (canGoNext) activeArrow else inactiveArrow,
                )
            }
        }

        // divider 를 호출부에 맡기지 않고 이 컴포넌트가 소유한다 — 없으면 첫 카드가 화살표에 바로 붙어
        // 카드가 바의 일부처럼 보인다.
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
private fun ArchiveMonthNavBarPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 5월",
            countText = "3건",
            canGoPrevious = true,
            canGoNext = true,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** 첫 진입 상태 — 이번 달이라 다음 이동이 막혀 있다. */
@Preview
@Composable
private fun ArchiveMonthNavBarAtLatestPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 8월",
            countText = "2건",
            canGoPrevious = true,
            canGoNext = false,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** 가장 오래된 기록이 있는 달 — 더 거슬러 올라갈 수 없다. */
@Preview
@Composable
private fun ArchiveMonthNavBarAtEarliestPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2025년 11월",
            countText = "1건",
            canGoPrevious = false,
            canGoNext = true,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** 종료한 챌린지가 하나도 없는 신규 사용자 — 양쪽이 모두 막혀 살아있는 조작이 없다. */
@Preview
@Composable
private fun ArchiveMonthNavBarNoMovementPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 8월",
            countText = "0건",
            canGoPrevious = false,
            canGoNext = false,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** 기록이 있는 달들 사이의 빈 달 — 0건에도 바 높이가 그대로라 화살표가 움직이지 않는다. */
@Preview
@Composable
private fun ArchiveMonthNavBarZeroCountPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 3월",
            countText = "0건",
            canGoPrevious = true,
            canGoNext = true,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

@Composable
private fun ArchiveMonthNavBarPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            content()
        }
    }
}
