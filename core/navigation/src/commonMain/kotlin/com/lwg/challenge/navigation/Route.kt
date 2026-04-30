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
    sealed interface Ex1Route : Route {

        @Serializable
        data object Main : Ex1Route
    }

    @Serializable
    sealed interface Ex2Route : Route {

        @Serializable
        data object Main : Ex2Route
    }

    @Serializable
    sealed interface Ex3Route : Route {

        @Serializable
        data object Main : Ex3Route
    }
}

val routeSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(Route.HomeRoute.Main::class)
        subclass(Route.LoginRoute.Main::class)
        subclass(Route.SplashRoute.Main::class)
        subclass(Route.Ex1Route.Main::class)
        subclass(Route.Ex2Route.Main::class)
        subclass(Route.Ex3Route.Main::class)
    }
}
