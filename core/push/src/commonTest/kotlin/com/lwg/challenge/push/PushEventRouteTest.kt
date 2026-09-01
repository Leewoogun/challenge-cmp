package com.lwg.challenge.push

import com.lwg.challenge.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals

class PushEventRouteTest {

    @Test
    fun `ChallengeRequest 는 홈으로 간다`() {
        assertEquals(Route.Home, PushEvent.ChallengeRequest(7L).toRoute())
    }

    @Test
    fun `ChallengeAccepted 는 챌린지 상세로 간다`() {
        assertEquals(Route.Challenge.Detail(7L), PushEvent.ChallengeAccepted(7L).toRoute())
    }

    @Test
    fun `ChallengeRejected 는 홈으로 간다`() {
        assertEquals(Route.Home, PushEvent.ChallengeRejected(7L).toRoute())
    }

    @Test
    fun `OpponentVerified 는 챌린지 상세로 간다`() {
        assertEquals(Route.Challenge.Detail(7L), PushEvent.OpponentVerified(7L).toRoute())
    }
}
