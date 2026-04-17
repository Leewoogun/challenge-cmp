package com.lwg.challenge.feature.main.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.lwg.challenge.navigation.Route

enum class BottomNavItem(
    val route: Route,
    val icon: ImageVector,
    val label: String,
) {
    HOME(
        route = Route.HomeRoute.Main,
        icon = Icons.Default.Home,
        label = "Home",
    ),
    EX1(
        route = Route.Ex1Route.Main,
        icon = Icons.Default.Search,
        label = "Ex1",
    ),
    EX2(
        route = Route.Ex2Route.Main,
        icon = Icons.Default.Favorite,
        label = "Ex2",
    ),
    EX3(
        route = Route.Ex3Route.Main,
        icon = Icons.Default.Settings,
        label = "Ex3",
    ),
}
