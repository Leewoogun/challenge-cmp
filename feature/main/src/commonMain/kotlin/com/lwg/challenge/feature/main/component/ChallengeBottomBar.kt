package com.lwg.challenge.feature.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.navigation.Route
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun ChallengeBottomBar(
    currentRoute: NavKey?,
    onTabSelected: (Route) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ChallengeTheme.colorScheme.background.card.copy(alpha = 0.95f),
        contentColor = ChallengeTheme.colorScheme.text.primary,
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
                color = ChallengeTheme.colorScheme.line.divider,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val selectedItem = BottomNavItem.find(currentRoute)
                BottomNavItem.entries.forEach { item ->
                    BottomNavTab(
                        item = item,
                        selected = item == selectedItem,
                        onClick = { onTabSelected(item.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color: Color = if (selected) {
        ChallengeTheme.colorScheme.point
    } else {
        ChallengeTheme.colorScheme.text.secondary
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
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

@Preview
@Composable
private fun ChallengeBottomBarPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            ChallengeBottomBar(
                currentRoute = Route.Home,
                onTabSelected = {},
            )
        }
    }
}

@Preview
@Composable
private fun BottomNavTabSelectedPreview() {
    ChallengeTheme {
        Row(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            BottomNavTab(
                item = BottomNavItem.HOME,
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun BottomNavTabUnselectedPreview() {
    ChallengeTheme {
        Row(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            BottomNavTab(
                item = BottomNavItem.RANKING,
                selected = false,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
