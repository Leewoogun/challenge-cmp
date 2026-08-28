package com.lwg.challenge.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.feature.mypage.component.MyPageMenuCard
import com.lwg.challenge.feature.mypage.component.ProfileCard
import com.lwg.challenge.feature.mypage.component.WithdrawText
import com.lwg.challenge.feature.mypage.contract.MyPageUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 마이페이지 탭 본문. design.md §1.0.
 *
 * 바텀바는 `MainScreen` 이 그리므로 여기서 그리지 않는다 — 그리면 바가 두 개가 되고 인셋이 이중 적용된다.
 */
@Composable
internal fun MyPageScreen(
    uiState: MyPageUiState.Data,
    onArchiveClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileCard(
            nickname = uiState.nickname,
            summary = uiState.recordSummary,
            record = uiState.record,
            muted = uiState.isRecordMuted,
        )

        VerticalSpacer(12.dp)

        MyPageMenuCard(
            icon = Icons.Filled.Description,
            label = "계약서 보관함",
            description = "과거 영혼의 맹세 열람",
            onClick = onArchiveClick,
        )

        VerticalSpacer(8.dp)

        IconTextButton(
            text = "로그아웃",
            icon = Icons.AutoMirrored.Filled.Logout,
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = ChallengeTheme.colorScheme.error,
        )

        VerticalSpacer(24.dp)

        // 탈퇴 중에는 이 항목만 잠근다. 전체 화면 로딩으로 덮으면 성공한 데이터와 로그아웃까지
        // 못 쓰게 된다 (§1.7).
        WithdrawText(
            onClick = onWithdrawClick,
            enabled = !uiState.isWithdrawing,
        )
    }
}

@Composable
internal fun MyPageLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
    }
}

@Preview
@Composable
private fun MyPageLoadingScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MyPageLoadingScreen()
        }
    }
}

@Preview
@Composable
private fun MyPageScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MyPageScreen(
                uiState = MyPageUiState.Data(
                    record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3),
                    userInfo = UserInfo(
                        id = 1,
                        kakaoId = 1,
                        nickname = "이우건",
                        profileImageUrl = null,
                    ),
                ),
                onArchiveClick = {},
                onLogoutClick = {},
                onWithdrawClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenEmptyRecordPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MyPageScreen(
                uiState = MyPageUiState.Data(
                    record = UserRecord.Empty,
                    userInfo = UserInfo(
                        id = 1,
                        kakaoId = 1,
                        nickname = "이우건",
                        profileImageUrl = null,
                    ),
                ),
                onArchiveClick = {},
                onLogoutClick = {},
                onWithdrawClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenRecordFailedPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MyPageScreen(
                uiState = MyPageUiState.Data(
                    record = null,
                    userInfo = UserInfo(
                        id = 1,
                        kakaoId = 1,
                        nickname = "이우건",
                        profileImageUrl = null,
                    ),
                ),
                onArchiveClick = {},
                onLogoutClick = {},
                onWithdrawClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenAllDrawPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MyPageScreen(
                uiState = MyPageUiState.Data(
                    record = UserRecord(win = 0, lose = 0, draw = 3, currentStreak = 0),
                    userInfo = UserInfo(
                        id = 1,
                        kakaoId = 1,
                        nickname = "이우건",
                        profileImageUrl = null,
                    ),
                ),
                onArchiveClick = {},
                onLogoutClick = {},
                onWithdrawClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenLongNicknamePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MyPageScreen(
                uiState = MyPageUiState.Data(
                    record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3),
                    userInfo = UserInfo(
                        id = 1,
                        kakaoId = 1,
                        nickname = "오늘도지지않는불꽃같은도전자이우건입니다",
                        profileImageUrl = null,
                    ),
                ),
                onArchiveClick = {},
                onLogoutClick = {},
                onWithdrawClick = {},
            )
        }
    }
}
