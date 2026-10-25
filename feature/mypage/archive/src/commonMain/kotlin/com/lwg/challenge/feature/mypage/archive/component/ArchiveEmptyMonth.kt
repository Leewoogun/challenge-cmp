package com.lwg.challenge.feature.mypage.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 선택한 달에만 기록이 없을 때의 본문. 문구·치수는 `design.md §2.5.1a` 확정값을 따른다.
 *
 * 계정 전체 기록이 0건인 상태(§2.5.1)와는 **다른 화면**이다. 그쪽은 `EmptyStateCard` 와
 * "챌린지 만들기" CTA 를 쓴다. 둘을 하나로 합치면 다른 달에 기록이 있는 사용자에게
 * "아직 끝낸 챌린지가 없어요" 라고 거짓말을 하게 된다.
 *
 * `EmptyStateCard` 를 검토했고 기각했다. CTA 가 필수 파라미터인데 빈 달에 놓을 CTA 가 없다 —
 * "챌린지 만들기" 는 동선이 반대고(이 사용자는 기록을 찾는 중이지 챌린지가 없어서 온 게 아니다),
 * "이전 달 보기" 는 월 이동 바의 `<` 와 같은 목적지를 카드 안에 복제한다. `EmptyStateCard` 에
 * nullable CTA 를 여는 선택도 하지 않는다 — 이미 출시된 공용 컴포넌트를 두 번째 사용처 하나 때문에
 * 일반화하지 않는다.
 *
 * 카드 크롬(radius·outline·패딩)은 일부러 벗었다. `EmptyStateCard` 는 종착지의 어휘인데 **빈 달은
 * 경유지**다. 무거운 카드가 월을 넘길 때마다 나타났다 사라지면 정상적인 경유가 매번 사건처럼 보인다.
 * 대신 일러스트 문법(아이콘 박스·간격·타이포)은 `EmptyStateCard` 와 똑같이 두어 새 방언이 아니라
 * 같은 어휘의 가벼운 판임을 유지한다.
 *
 * 두 부제 모두 방향·위치를 지정하지 않는 이유는 둘이다:
 * 1. "위 화살표"·"상단의" 같은 위치 표현은 스크린리더에 아무 의미가 없고, 큰 폰트 스케일에서는 실제
 *    위치도 달라진다.
 * 2. "이전 달로 넘겨보세요" 처럼 방향을 지정하면 **어느 쪽으로 가라는 말인지가 곧 거짓이 된다** —
 *    B형이 붙는 자리는 그 방향에 아무것도 없는 자리다. B형은 방향을 시키는 대신 사실만 말하고,
 *    A형의 "화살표로" 는 좌우 어느 쪽도 지정하지 않아 어느 달에서도 거짓이 아니다.
 *
 * 실패 화면(`CloudOff` + "보관함을 불러오지 못했어요")과 아이콘까지 다르게 두는 이유는 1차 판단을
 * 그대로 승계한다 — 둘 다 "목록이 없는 화면" 이라 문구만 다르면 0건인지 못 불러온 건지 한눈에
 * 갈리지 않는다.
 *
 * @param title 월이 들어간 완성된 제목(예: "8월엔 끝낸 챌린지가 없어요").
 *   `ArchiveMonthCursor.emptyMonthTitle` 이 만들어 넘긴다.
 * @param subtitle 제목 아래 한 줄. `ArchiveUiState.emptyMonthSubtitle` 이 아래 둘 중 하나로 골라
 *   넘긴다:
 *   - **A형** — 기록 구간 안의 빈 달: "화살표로 다른 달을 볼 수 있어요"
 *   - **B형** — 첫 기록보다 이전 달: "첫 기록은 {Y}년 {M}월이에요"
 *
 *   **A형 부제를 B형 자리에 쓰면 거짓말이 된다.** *"다른 달을 볼 수 있어요"* 는 **더 가면 뭔가 있다**
 *   로 읽히는데, 첫 기록보다 이전 방향에는 **영원히 아무것도 없다.** 과거 하한이 있던 시절엔 비활성
 *   화살표가 그 사실을 말해 줬다. 2차 개정에서 차단을 없앴으므로 **이 한 줄이 그 역할을 받는다** —
 *   막지 않되 알린다.
 *
 *   B형에 연도를 붙이는 이유: 하한이 없어 사용자가 여러 해 전으로 걸어 들어갈 수 있다. "3월" 만으로는
 *   그게 어느 해의 3월인지 알 수 없다.
 *
 *   기록이 **0건이면 B형을 쓰지 않는다** — 말할 "첫 기록" 자체가 없다. 그 계정의 과거 달은 A형이다.
 */
@Composable
internal fun ArchiveEmptyMonth(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val primary = ChallengeTheme.colorScheme.primary

    // 세로 가운데 정렬을 쓰지 않는다 — 목록 높이가 달마다 달라 본문이 위아래로 튀면 월을 넘길 때마다
    // 같은 빈 화면이 다른 자리에 나타난다. 바 아래 64dp 내려온 자리에 고정한다.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 64.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = primary,
            )
        }

        VerticalSpacer(16.dp)

        Text(
            text = title,
            style = ChallengeTheme.typography.bold16,
            color = ChallengeTheme.colorScheme.onBackground,
        )

        VerticalSpacer(4.dp)

        Text(
            text = subtitle,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A형 — 기록 구간 안의 빈 달. 이 방향으로 더 가면 기록이 있는 달이 나온다. */
@Preview
@Composable
private fun ArchiveEmptyMonthPreview() {
    ArchiveEmptyMonthPreviewContainer {
        ArchiveEmptyMonth(
            title = "8월엔 끝낸 챌린지가 없어요",
            subtitle = "화살표로 다른 달을 볼 수 있어요",
        )
    }
}

/** B형 — 첫 기록보다 이전 달. 더 가도 아무것도 없다는 사실을 부제가 대신 말한다. */
@Preview
@Composable
private fun ArchiveEmptyMonthBeforeFirstRecordPreview() {
    ArchiveEmptyMonthPreviewContainer {
        ArchiveEmptyMonth(
            title = "3월엔 끝낸 챌린지가 없어요",
            subtitle = "첫 기록은 2026년 5월이에요",
        )
    }
}

@Composable
private fun ArchiveEmptyMonthPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            content()
        }
    }
}
