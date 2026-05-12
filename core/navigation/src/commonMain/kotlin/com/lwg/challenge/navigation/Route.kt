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
}

val routeSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(Route.HomeRoute.Main::class)
        subclass(Route.LoginRoute.Main::class)
        subclass(Route.SplashRoute.Main::class)
        subclass(Route.FriendsRoute.Main::class)
        subclass(Route.RankingRoute.Main::class)
        subclass(Route.MyPageRoute.Main::class)
    }
}
