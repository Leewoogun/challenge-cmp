package com.lwg.challenge.feature.mypage.component

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
 * 부제가 방향·위치를 지정하지 않는 이유는 둘이다:
 * 1. "위 화살표"·"상단의" 같은 위치 표현은 스크린리더에 아무 의미가 없고, 큰 폰트 스케일에서는 실제
 *    위치도 달라진다.
 * 2. "이전 달로 넘겨보세요" 처럼 방향을 지정하면 하한 근처에서 거짓이 된다 — 그 달에서는 `<` 가
 *    비활성이라 화면이 시키는 것을 할 수 없다. "화살표로" 는 그 경우에도 거짓이 아니다.
 *
 * 실패 화면(`CloudOff` + "보관함을 불러오지 못했어요")과 아이콘까지 다르게 두는 이유는 1차 판단을
 * 그대로 승계한다 — 둘 다 "목록이 없는 화면" 이라 문구만 다르면 0건인지 못 불러온 건지 한눈에
 * 갈리지 않는다.
 *
 * @param title 월이 들어간 완성된 제목(예: "8월엔 끝낸 챌린지가 없어요").
 *   `ArchiveMonthCursor.emptyMonthTitle` 이 만들어 넘긴다.
 */
@Composable
internal fun ArchiveEmptyMonth(
    title: String,
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
            text = "화살표로 다른 달을 볼 수 있어요",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun ArchiveEmptyMonthPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveEmptyMonth(title = "8월엔 끝낸 챌린지가 없어요")
        }
    }
}
