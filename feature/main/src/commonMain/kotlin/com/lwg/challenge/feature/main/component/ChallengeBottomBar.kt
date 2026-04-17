package com.lwg.challenge.feature.main.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.lwg.challenge.navigation.Route

@Composable
internal fun ChallengeBottomBar(
    currentRoute: NavKey?,
    onTabSelected: (Route) -> Unit,
) {
    NavigationBar {
        BottomNavItem.entries.forEach { destination ->
            NavigationBarItem(
                selected = when (currentRoute) {
                    is Route.HomeRoute -> destination == BottomNavItem.HOME
                    is Route.Ex1Route -> destination == BottomNavItem.EX1
                    is Route.Ex2Route -> destination == BottomNavItem.EX2
                    is Route.Ex3Route -> destination == BottomNavItem.EX3
                    else -> false
                },
                onClick = { onTabSelected(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(text = destination.label) },
            )
        }
    }
}
