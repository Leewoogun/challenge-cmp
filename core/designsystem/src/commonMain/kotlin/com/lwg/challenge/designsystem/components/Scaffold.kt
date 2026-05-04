package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.statusbar.SetStatusBarAppearance
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * StatusBar 색상과 TopBar 색상을 통합 관리하는 Scaffold.
 *
 * - Android: EdgeToEdge 모드에서 StatusBar 영역을 statusBarColor로 채움
 * - iOS: Safe Area 위 StatusBar 영역을 statusBarColor로 채움
 * - 양 플랫폼 모두 동일한 시각적 결과를 보장
 *
 * 구조:
 * ┌──────────────────────┐
 * │ statusBarColor 배경   │ ← StatusBar 영역 (시계, 배터리 등)
 * ├──────────────────────┤
 * │ topBar()             │ ← TopBar 영역 (statusBarColor 배경 유지)
 * ├──────────────────────┤
 * │                      │
 * │ content()            │ ← containerColor 배경
 * │                      │
 * ├──────────────────────┤
 * │ bottomBar()          │
 * └──────────────────────┘
 * │ NavigationBar 패딩    │ ← 하단 시스템 영역
 */
@Composable
fun ChallengeScaffold(
    modifier: Modifier = Modifier,
    statusBarColor: Color = ChallengeTheme.colorScheme.background,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    containerColor: Color = ChallengeTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable ColumnScope.() -> Unit,
) {
    SetStatusBarAppearance(statusBarColor)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(color = statusBarColor)
                    .statusBarsPadding(),
            ) {
                topBar()
            }
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = WindowInsets(0.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(contentPadding),
        ) {
            content()
        }
    }
}
