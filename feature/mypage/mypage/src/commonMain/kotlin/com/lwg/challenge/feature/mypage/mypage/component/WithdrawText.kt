package com.lwg.challenge.feature.mypage.mypage.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 회원탈퇴 진입점. design.md §1.5.2.
 *
 * 버튼이 아니라 텍스트 1줄이다 — 우발적 탭을 줄이려고 시각 위계를 낮춘 것이고,
 * 의도적 탭에 대한 경고는 확인 다이얼로그가 맡는다.
 */
@Composable
internal fun WithdrawText(
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
            text = "회원 탈퇴",
            style = ChallengeTheme.typography.medium12,
            // error 를 쓰지 않는다 — 이 화면에서 error 는 로그아웃이 이미 쓰고 있고,
            // 더 위험한 행위에 같은 색을 주면 둘 중 무엇이 더 무거운지 화면이 말하지 못한다.
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun WithdrawTextPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            WithdrawText(onClick = {})
        }
    }
}

@Preview
@Composable
private fun WithdrawTextDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            WithdrawText(onClick = {}, enabled = false)
        }
    }
}
