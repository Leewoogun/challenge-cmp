package com.lwg.challenge.feature.mypage.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * 비활성 표현은 **컨테이너와 아이콘 두 축**이다 — 활성은 `secondary` 배경 위의 진한 아이콘이고,
 * 비활성은 **배경이 통째로 빠지고** 아이콘도 옅어진다. 이전 서술("비활성은 아이콘 `alpha 0.4f`")은
 * 여기서 정정한다: 그 판은 **한 축뿐**이었다. 앱의 비활성 관례(`NormalButton` 의
 * `disabledContainerColor`/`disabledContentColor`)는 배경과 내용이 함께 흐려지는 두 축인데, 이
 * 화살표에는 애초에 컨테이너가 없어 content 축만 베껴 온 상태였다. 그래서 버튼은 배경째 흐려지며
 * "못 누른다" 를 말하는 동안 화살표는 아이콘만 옅어져 같은 말을 절반만 했다. 원인은 농도가 아니라
 * **신호 개수**였다.
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
 *
 *   건수만 오는 것은 아니다. 월 전환이 늦어지면 이 자리에 "불러오는 중" 이 온다
 *   (`ArchiveUiState.countText`) — 슬롯을 늘 유지한 덕분에 로딩 힌트가 **새 자리를 만들지 않고**
 *   들어갈 수 있다. 컴포넌트는 무엇이 오든 한 줄로 그리기만 한다.
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                // background 가 아니라 surface — 상단바와 같은 색이라 상단바와 월 바가 하나의
                // 크롬 블록으로 읽히고, 그 아래 카드 목록과 층이 갈린다.
                .background(ChallengeTheme.colorScheme.surface)
                // 8dp 는 임의값이 아니다: 8 + (48 - 24) / 2 = 20 이라 48dp 터치 영역 안의 24dp 아이콘
                // 바깥 모서리가 카드 gutter(20dp)와 정확히 같은 세로선에 선다. 아이콘 뒤에 40dp 배경이
                // 생겼어도 이 정렬은 그대로다 — 배경은 아이콘보다 8dp 씩 더 나가 화면 끝에서 12dp 에
                // 서지만, 카드와 줄을 맞추는 기준은 여전히 글자·아이콘이다.
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonthArrowButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 달",
                enabled = canGoPrevious,
                onClick = onPreviousClick,
            )

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

            MonthArrowButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 달",
                enabled = canGoNext,
                onClick = onNextClick,
            )
        }

        // divider 를 호출부에 맡기지 않고 이 컴포넌트가 소유한다 — 없으면 첫 카드가 화살표에 바로 붙어
        // 카드가 바의 일부처럼 보인다.
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}

/**
 * 월 이동 화살표 한 개. 활성/비활성을 **배경과 아이콘 두 축**으로 말한다.
 *
 * 눌림 표현을 직접 그리는 이유: 탭한 순간 **월 라벨이 움직이지 않기** 때문이다. ViewModel 이 응답이
 * 올 때까지 이전 달을 그대로 두므로(`ArchiveViewModel.monthFlow`) 상태 전환은 탭 피드백이 되지
 * 못하고, 그 몫을 이 버튼이 진다. 입력 반응과 상태 전환을 분리한 것이 그 설계의 요지다.
 */
@Composable
private fun MonthArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    IconButton(onClick = onClick, enabled = enabled, interactionSource = interactionSource) {
        // 40dp 배경은 **시각 신호이지 터치 영역이 아니다.** 터치 타깃은 바깥 IconButton 의 48dp 그대로고
        // 이 Box 는 그 안에 얹힌 그림일 뿐이라, 배경이 붙고 빠져도 슬롯 기하가 흔들리지 않는다 —
        // 좌우 8dp 패딩·바 높이 64dp·아이콘 24dp 는 전부 이전 판과 같은 값이다.
        //
        // secondary 는 앱에 이미 있는 토큰이다(마이페이지 메뉴 아이콘 박스가 같은 어휘를 쓴다).
        // 이 화살표 하나 때문에 새 토큰이나 새 색을 만들지 않는다.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (enabled) ChallengeTheme.colorScheme.secondary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // 배경 **위에** 얹는다. IconButton 기본 ripple 은 버튼 자신에게 걸려 이 불투명한 40dp
            // 배경 아래에 깔리므로 활성 상태에서는 보이지 않는다.
            //
            // **비활성에는 눌림 표현이 없다**(`enabled &&`). 정상적으로 끝에 도달해 못 누르는 버튼이
            // 반응하면 오류로 읽힌다 — 비활성 탭에 토스트·햅틱을 주지 않는 규약과 같은 이유다.
            //
            // 스케일·이동은 쓰지 않는다. 빈 달을 건너뛰지 않아 연타가 정상 사용인데, 배경이 축소·복원을
            // 반복하면 *"움직이면 안 되는 요소"* 가 계속 움직인다. state layer 는 아무것도 옮기지 않고
            // 명멸만 한다.
            //
            // 0.12f 는 Material 3 pressed state layer 표준값이고, 앱 최빈 tint 알파(0.10f)와 이질적이지
            // 않다. 앱에 `collectIsPressedAsState` 선례가 0건이라 상속할 관례가 없어 플랫폼 표준을 그대로
            // 가져왔다. ⚠️ 아래 비활성 0.25f 와 함께 실기 확인이 필요하다 — 둘은 반대 방향의 요구다.
            if (enabled && pressed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            color = ChallengeTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                        ),
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                // 0.4f 에서 0.25f 로 내렸다. 0.4 는 **배경이 함께 흐려질 때**의 값인데(NormalButton 의
                // disabledContentColor) 여기는 배경이 아예 사라지므로, 아이콘 혼자 더 내려가야 활성과의
                // 대비가 버튼과 같은 정도로 벌어진다.
                //
                // 실기 확인이 필요한 값이다 — 보이되 눌릴 것 같지는 않아야 한다. 0.2 미만은 다크
                // 테마에서 사실상 숨김이 되므로 그 아래로는 내리지 않는다.
                //
                // ⚠️ 위 눌림 0.12f 와 **함께** 봐야 한다. 하나는 "반응한다" 를, 하나는 "못 누른다" 를
                // 같은 어휘(onBackground 알파)로 말하는 반대 방향의 요구라 따로 맞추면 어긋난다.
                tint = if (enabled) {
                    ChallengeTheme.colorScheme.onBackground
                } else {
                    ChallengeTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                },
            )
        }
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

/**
 * `<` 가 막힌 모습. 2차 개정에서 과거 하한이 폐기돼 **보관함은 지금 이 상태를 만들지 않는다** —
 * 컴포넌트가 아직 과거 방향 경계를 표현할 수 있다는 것만 붙든다.
 */
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

/**
 * 양쪽이 모두 막혀 살아있는 조작이 없는 모습. 기록이 전부 이번 달인 계정에서 실제로 나오던 화면이고,
 * 사용자가 이것을 **고장으로 읽어** 2차 개정에서 과거 하한을 폐기했다. 지금은 도달할 수 없는 상태이며
 * 다시 만들지 않기 위한 기록으로만 남긴다.
 */
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

/**
 * 응답이 늦는 월 전환 — 건수 자리에 힌트가 들어온다. 라벨은 **아직 이전 달**이고, 위 프리뷰들과
 * 나란히 놓고 보면 바의 높이도 화살표 위치도 그대로다.
 */
@Preview
@Composable
private fun ArchiveMonthNavBarLoadingHintPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 8월",
            countText = "불러오는 중",
            canGoPrevious = true,
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
