package com.lwg.challenge.feature.mypage.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약서 보관함의 월 섹션 헤더. design.md §2.3.2.
 *
 * `stickyHeader` 로 쓰이므로 **자체 불투명 배경이 필수**다 — 배경이 없으면 스크롤되는 카드가
 * 헤더 글자 뒤로 비쳐 두 텍스트가 겹친다. 좌우 gutter 도 이 컴포넌트가 직접 내서(호출부의
 * `contentPadding` 이 아니라) 배경이 화면 끝까지 닿게 한다.
 *
 * @param topSpacing 이전 월 섹션과의 추가 간격. 배경 **안쪽**에 넣어야 sticky 상태에서 그 자리로
 *   카드가 비치지 않는다.
 */
@Composable
internal fun ArchiveMonthHeader(
    label: String,
    countText: String,
    modifier: Modifier = Modifier,
    topSpacing: Dp = 0.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChallengeTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .padding(top = topSpacing + 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ChallengeTheme.typography.bold14,
            color = ChallengeTheme.colorScheme.onBackground,
        )
        Text(
            text = countText,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun ArchiveMonthHeaderPreview() {
    ChallengeTheme {
        Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveMonthHeader(label = "2026년 8월", countText = "3건")
            ArchiveMonthHeader(label = "2026년 7월", countText = "1건", topSpacing = 12.dp)
        }
    }
}
