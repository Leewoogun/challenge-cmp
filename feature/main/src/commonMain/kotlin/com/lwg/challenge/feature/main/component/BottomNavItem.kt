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
        route = Route.Home,
        icon = Icons.Filled.Home,
        label = "홈",
    ),
    FRIENDS(
        route = Route.Friends.List,
        icon = Icons.Filled.Group,
        label = "친구",
    ),
    RANKING(
        route = Route.Ranking,
        icon = Icons.Filled.EmojiEvents,
        label = "랭킹",
    ),
    MYPAGE(
        route = Route.MyPage,
        icon = Icons.Filled.Person,
        label = "MY",
    ),
}
