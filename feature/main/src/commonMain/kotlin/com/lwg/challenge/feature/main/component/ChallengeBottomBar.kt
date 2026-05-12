package com.lwg.challenge.feature.main.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.navigation.Route

/**
 * 메인 셸 BottomBar.
 *
 * 디자인 결정(design.md):
 * - `NavigationBar`/`NavigationBarItem` 미사용 — Material 컴포넌트가 라벨 12.sp / 56.dp 패딩을 강제하여
 *   디자인의 10.sp / py-1.5 / alpha 95% 를 맞출 수 없음.
 * - 컨테이너 색: `surface.copy(alpha=0.95f)` — backdrop-blur 는 KMP 공통 API 한계로 alpha 만 근사.
 * - 상단 1.dp `outline` 보더, 하단 `WindowInsets.safeDrawing` (iOS 홈 인디케이터 회피).
 * - Active/Inactive 는 색 분기만 (`primary` vs `onSurfaceVariant`). stroke 굵기 분기 없음.
 */
@Composable
internal fun ChallengeBottomBar(
    currentRoute: NavKey?,
    onTabSelected: (Route) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ChallengeTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = ChallengeTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                ),
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = ChallengeTheme.colorScheme.outline,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavItem.entries.forEach { item ->
                    BottomNavTab(
                        item = item,
                        selected = isSelected(currentRoute, item),
                        onClick = { onTabSelected(item.route) },
                    )
                }
            }
        }
    }
}

private fun isSelected(currentRoute: NavKey?, item: BottomNavItem): Boolean =
    when (currentRoute) {
        is Route.HomeRoute -> item == BottomNavItem.HOME
        is Route.FriendsRoute -> item == BottomNavItem.FRIENDS
        is Route.RankingRoute -> item == BottomNavItem.RANKING
        is Route.MyPageRoute -> item == BottomNavItem.MYPAGE
        else -> false
    }

@Composable
private fun BottomNavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color: Color = if (selected) {
        ChallengeTheme.colorScheme.primary
    } else {
        ChallengeTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            modifier = Modifier.size(22.dp),
            tint = color,
        )
        Text(
            text = item.label,
            style = ChallengeTheme.typography.medium10,
            color = color,
        )
    }
}
