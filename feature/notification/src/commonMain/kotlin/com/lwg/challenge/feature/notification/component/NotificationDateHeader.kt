package com.lwg.challenge.feature.notification.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 날짜 그룹 헤더. design.md §3.6.2.
 *
 * 신규 토큰 없이 정본의 섹션 헤더 문법(`text-sm font-bold` + `px-1`)을 그대로 옮긴다 — 좌우 24dp 는
 * 카드 gutter 20dp + 4dp 다. 배경도 카드 크롬도 없다.
 *
 * 🔴 **건수(`{n}건`)를 붙이지 않는다.** 정본 섹션 헤더에는 있지만 거긴 *처리할 대상의 수*라 행동을
 * 바꾼다. 지난 날짜의 알림 수는 아무 행동도 바꾸지 않는다.
 *
 * 🔴 **`stickyHeader` 가 아니다.** 앱에 선례가 0건이고, 알림의 날짜 그룹은 하루 몇 건으로 짧아
 * sticky 의 값(긴 그룹에서 헤더를 붙잡아 두는 것)이 나오지 않는다 — 거의 항상 바뀌는 상태로 떠 있게
 * 되고, 상단바가 이미 고정 크롬이라 가시 영역만 준다.
 *
 * @param modifier 그룹 사이 위 여백(16dp)을 **호출부가** 얹는다. 첫 그룹의 위 여백은 목록
 *   `contentPadding` 이 이미 냈으므로 여기서 또 주면 두 배가 된다.
 */
@Composable
internal fun NotificationDateHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp),
        style = ChallengeTheme.typography.bold14,
        color = ChallengeTheme.colorScheme.onBackground,
    )
}

/** 세 문구가 같은 자리·같은 무게로 놓이는지 본다. 그룹 사이 16dp 는 목록이 얹으므로 여기엔 없다. */
@Preview
@Composable
private fun NotificationDateHeaderPreview() {
    ChallengeTheme {
        Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            NotificationDateHeader(label = "오늘")
            NotificationDateHeader(label = "어제", modifier = Modifier.padding(top = 16.dp))
            NotificationDateHeader(label = "8월 28일", modifier = Modifier.padding(top = 16.dp))
        }
    }
}
