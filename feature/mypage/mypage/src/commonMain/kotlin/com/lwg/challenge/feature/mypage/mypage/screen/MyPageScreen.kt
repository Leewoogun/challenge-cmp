package com.lwg.challenge.feature.mypage.mypage.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.feature.mypage.mypage.component.MyPageMenuCard
import com.lwg.challenge.feature.mypage.mypage.component.ThemeSettingCard
import com.lwg.challenge.feature.mypage.mypage.component.ProfileCard
import com.lwg.challenge.feature.mypage.mypage.component.WithdrawText
import com.lwg.challenge.feature.mypage.mypage.contract.MyPageUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 마이페이지 탭 본문. 바텀바는 `MainScreen` 이 그리므로 여기서 그리지 않는다(이중 인셋). */
@Composable
internal fun MyPageScreen(
    uiState: MyPageUiState.Data,
    onArchiveClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
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
            nickname = uiState.userInfo?.nickname.orEmpty(),
            profileImageUrl = uiState.userInfo?.profileImageUrl,
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

        ThemeSettingCard(
            isDarkSelected = uiState.isDarkThemeSelected,
            isLightSelected = uiState.isLightThemeSelected,
            onThemeModeSelected = onThemeModeSelected,
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

        // 전체 로딩으로 덮으면 로그아웃까지 막히므로 탈퇴 중에는 이 항목만 잠근다.
        WithdrawText(
            onClick = onWithdrawClick,
            enabled = !uiState.isWithdrawing,
        )
    }
}

@Preview
@Composable
private fun MyPageScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
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
                onThemeModeSelected = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenEmptyRecordPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
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
                onThemeModeSelected = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenRecordFailedPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
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
                onThemeModeSelected = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenAllDrawPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
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
                onThemeModeSelected = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenLongNicknamePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
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
                onThemeModeSelected = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPageScreenLightPreview() {
    ChallengeTheme(darkTheme = false) {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            MyPageScreen(
                uiState = MyPageUiState.Data(
                    record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3),
                    userInfo = UserInfo(
                        id = 1,
                        kakaoId = 1,
                        nickname = "이우건",
                        profileImageUrl = null,
                    ),
                    themeMode = ThemeMode.LIGHT,
                ),
                onArchiveClick = {},
                onLogoutClick = {},
                onWithdrawClick = {},
                onThemeModeSelected = {},
            )
        }
    }
}
