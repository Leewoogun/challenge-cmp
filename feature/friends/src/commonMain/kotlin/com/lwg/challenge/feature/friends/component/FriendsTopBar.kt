package com.lwg.challenge.feature.friends.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 탭 상단 헤더 — design.md §3.2.
 *
 * 1차 1단계: 좌측 "친구" 라벨만 노출. 친구 0명 빈 상태에서는 UserPlus/Search 액션 진입점을
 * 빈 상태 카드 CTA로 일원화하므로 액션 슬롯을 두지 않는다.
 * 1차 2단계 진입 시 `actions: (@Composable RowScope.() -> Unit)? = null` 슬롯을 1번만 추가하여
 * UserPlus 버튼 + 검색 input 영역을 외부 주입할 수 있도록 확장한다.
 */
@Composable
internal fun FriendsTopBar(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChallengeTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = "친구",
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.onBackground,
            )
        }
    }
}

@Preview
@Composable
private fun FriendsTopBarPreview() {
    ChallengeTheme {
        FriendsTopBar()
    }
}
