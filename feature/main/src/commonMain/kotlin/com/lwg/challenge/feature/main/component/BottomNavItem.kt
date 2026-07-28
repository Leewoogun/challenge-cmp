package com.lwg.challenge.feature.main.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
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
    ;

    companion object {
        /**
         * [route] 가 속한 바텀 탭. 탭에 해당하지 않으면 null 이며, 바텀바 노출 여부 판단에도 쓴다.
         *
         * `Route.Friends` 가 아닌 `Route.Friends.List` 로 좁힌 이유 —
         * 탭 화면에서 push 로 진입하는 하위 화면(Friends.Search 등)은 탭 소속이 아니므로
         * 바텀바를 숨겨야 한다.
         */
        fun find(route: NavKey?): BottomNavItem? = when (route) {
            is Route.Home -> HOME
            is Route.Friends.List -> FRIENDS
            is Route.Ranking -> RANKING
            is Route.MyPage -> MYPAGE
            else -> null
        }
    }
}
