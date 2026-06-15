package com.lwg.challenge.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

sealed interface Route : NavKey {

    @Serializable
    sealed interface HomeRoute : Route {

        @Serializable
        data object Main : HomeRoute
    }

    @Serializable
    sealed interface LoginRoute : Route {

        @Serializable
        data object Main : LoginRoute
    }

    @Serializable
    sealed interface SplashRoute : Route {

        @Serializable
        data object Main : SplashRoute
    }

    @Serializable
    sealed interface FriendsRoute : Route {

        @Serializable
        data object Main : FriendsRoute
    }

    @Serializable
    sealed interface RankingRoute : Route {

        @Serializable
        data object Main : RankingRoute
    }

    @Serializable
    sealed interface MyPageRoute : Route {

        @Serializable
        data object Main : MyPageRoute
    }

    /**
     * 챌린지 상세. home-feed 카드 탭 시 진입.
     * 본 feature 범위 밖 — stub PlaceholderScreen 만 등록.
     */
    @Serializable
    sealed interface ChallengeDetailRoute : Route {

        @Serializable
        data class Main(val challengeId: Long) : ChallengeDetailRoute
    }

    /**
     * 챌린지 생성. home-feed FAB / 빈 상태 "챌린지 만들기" CTA 진입.
     * 본 feature 범위 밖 — stub PlaceholderScreen.
     */
    @Serializable
    sealed interface ChallengeCreateRoute : Route {

        @Serializable
        data object Main : ChallengeCreateRoute
    }

    /**
     * 알림 목록. home-feed 헤더 Bell 진입.
     * 본 feature 범위 밖 — stub PlaceholderScreen.
     */
    @Serializable
    sealed interface NotificationsRoute : Route {

        @Serializable
        data object Main : NotificationsRoute
    }
}

val routeSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(Route.HomeRoute.Main::class)
        subclass(Route.LoginRoute.Main::class)
        subclass(Route.SplashRoute.Main::class)
        subclass(Route.FriendsRoute.Main::class)
        subclass(Route.RankingRoute.Main::class)
        subclass(Route.MyPageRoute.Main::class)
        subclass(Route.ChallengeDetailRoute.Main::class)
        subclass(Route.ChallengeCreateRoute.Main::class)
        subclass(Route.NotificationsRoute.Main::class)
    }
}
