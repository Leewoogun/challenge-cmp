package com.lwg.challenge.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * 화면 route 정의. feature 모듈 구조를 미러링한다 —
 * 단일 모듈 feature 는 평탄한 data object, 하위 모듈로 나뉜 feature(friends, challenge)만
 * 중첩 sealed interface 로 둔다.
 */
sealed interface Route : NavKey {

    @Serializable
    data object Splash : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Ranking : Route

    @Serializable
    data object MyPage : Route

    /** 알림 목록. home-feed 헤더 Bell 진입. 본 feature 범위 밖 — stub PlaceholderScreen. */
    @Serializable
    data object Notifications : Route

    @Serializable
    sealed interface Friends : Route {

        @Serializable
        data object List : Friends

        @Serializable
        data object Search : Friends
    }

    @Serializable
    sealed interface Challenge : Route {

        /** 챌린지 생성. home-feed FAB / 빈 상태 "챌린지 만들기" CTA 진입. */
        @Serializable
        data object Create : Challenge

        /** 챌린지 상세(계약서 + 양측 서명). home-feed 카드 탭 시 진입. */
        @Serializable
        data class Detail(val challengeId: Long) : Challenge

        /**
         * 상대방 맹세 화면. 홈 '받은 도전장' 수락 탭 진입.
         * 수락+서명이 단일 원자 요청이라 이 화면을 통과해야 챌린지가 시작된다.
         */
        @Serializable
        data class Oath(val challengeId: Long) : Challenge
    }
}

val routeSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(Route.Splash::class)
        subclass(Route.Login::class)
        subclass(Route.Home::class)
        subclass(Route.Ranking::class)
        subclass(Route.MyPage::class)
        subclass(Route.Notifications::class)
        subclass(Route.Friends.List::class)
        subclass(Route.Friends.Search::class)
        subclass(Route.Challenge.Create::class)
        subclass(Route.Challenge.Detail::class)
        subclass(Route.Challenge.Oath::class)
    }
}
