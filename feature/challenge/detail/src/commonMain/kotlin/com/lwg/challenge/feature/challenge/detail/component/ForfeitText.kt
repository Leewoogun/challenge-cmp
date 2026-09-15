package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_ENTRY_TEXT
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 기권 진입점.
 *
 * 버튼이 아니라 텍스트 1줄이다 — 우발적 탭을 줄이려고 시각 위계를 낮춘 것이고,
 * 의도적 탭에 대한 경고는 확인 다이얼로그가 맡는다 (마이페이지 `WithdrawText` 와 같은 근거).
 */
@Composable
internal fun ForfeitText(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        // fillMaxWidth 를 주지 않는다. 대신 세로 패딩으로 48dp 터치 타깃을 확보한다 —
        // 높이를 줄여서 누르기 어렵게 만들면 스크린리더·운동 장애 사용자에게만 어려워진다.
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = FORFEIT_ENTRY_TEXT,
            style = ChallengeTheme.typography.medium12,
            // 마이페이지의 회원 탈퇴는 같은 화면의 로그아웃이 error 를 먼저 쓰고 있어 색을 내렸다.
            // 이 화면에서 error 를 쓰는 것은 "패배" 헤드라인뿐인데, 그 헤드라인과 이 진입점은 같은
            // 화면에 함께 나올 수 없다 — 진입점은 IN_PROGRESS 에서만 열리고 패배 헤드라인은 판정이
            // 끝난 뒤에만 나온다. 그래서 여기서 error 는 비어 있고, 화면에서 가장 위험한 행위가
            // 그 색을 갖는다.
            color = ChallengeTheme.colorScheme.error,
        )
    }
}

@Preview
@Composable
private fun ForfeitTextPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ForfeitText(onClick = {})
        }
    }
}

@Preview
@Composable
private fun ForfeitTextDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ForfeitText(onClick = {}, enabled = false)
        }
    }
}
