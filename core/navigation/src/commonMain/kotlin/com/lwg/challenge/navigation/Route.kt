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

    /**
     * 계약서 보관함 — 종료된 챌린지 목록. 마이페이지 메뉴에서 push 로 들어가는 하위 화면이다.
     *
     * `:feature:mypage` 는 단일 모듈이라 위 규칙대로 중첩하지 않고 평탄한 data object 로 둔다.
     */
    @Serializable
    data object Archive : Route

    /** 알림 목록. home-feed 헤더 Bell 에서 push 로 들어가는 하위 화면이다. */
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

        /**
         * 인증 사진 촬영·제출. 챌린지 상세의 인증 CTA 진입.
         *
         * @property isReplace 이미 인증한 뒤 사진을 교체하러 들어왔는지. 완료 문구만 가른다.
         *   진입 시점에 상세가 이미 아는 값이라 서버에 다시 묻지 않고 인자로 실어 보낸다.
         */
        @Serializable
        data class Verify(val challengeId: Long, val isReplace: Boolean) : Challenge
    }
}

val routeSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(Route.Splash::class)
        subclass(Route.Login::class)
        subclass(Route.Home::class)
        subclass(Route.Ranking::class)
        subclass(Route.MyPage::class)
        subclass(Route.Archive::class)
        subclass(Route.Notifications::class)
        subclass(Route.Friends.List::class)
        subclass(Route.Friends.Search::class)
        subclass(Route.Challenge.Create::class)
        subclass(Route.Challenge.Detail::class)
        subclass(Route.Challenge.Oath::class)
        subclass(Route.Challenge.Verify::class)
    }
}
