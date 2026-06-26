package com.lwg.challenge.feature.main.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.lwg.challenge.navigation.Route

enum class BottomNavItem(
    val route: Route,
    val icon: ImageVector,
    val label: String,
) {
    HOME(
        route = Route.HomeRoute.Main,
        icon = Icons.Filled.Home,
        label = "홈",
    ),
    FRIENDS(
        route = Route.FriendsRoute.Main,
        icon = Icons.Filled.Group,
        label = "친구",
    ),
    RANKING(
        route = Route.RankingRoute.Main,
        icon = Icons.Filled.EmojiEvents,
        label = "랭킹",
    ),
    MYPAGE(
        route = Route.MyPageRoute.Main,
        icon = Icons.Filled.Person,
        label = "MY",
    ),
}
