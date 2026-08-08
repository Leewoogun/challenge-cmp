package com.lwg.challenge.push

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PushEventTest {

    @Test
    fun `CHALLENGE_REQUEST 데이터를 ChallengeRequest 로 읽는다`() {
        val event = PushEvent.from(mapOf("type" to "CHALLENGE_REQUEST", "challengeId" to "7"))

        assertEquals(PushEvent.ChallengeRequest(7L), event)
    }

    @Test
    fun `CHALLENGE_ACCEPTED 데이터를 ChallengeAccepted 로 읽는다`() {
        val event = PushEvent.from(mapOf("type" to "CHALLENGE_ACCEPTED", "challengeId" to "7"))

        assertEquals(PushEvent.ChallengeAccepted(7L), event)
    }

    @Test
    fun `CHALLENGE_REJECTED 데이터를 ChallengeRejected 로 읽는다`() {
        val event = PushEvent.from(mapOf("type" to "CHALLENGE_REJECTED", "challengeId" to "7"))

        assertEquals(PushEvent.ChallengeRejected(7L), event)
    }

    @Test
    fun `모르는 type 이면 null 을 반환한다`() {
        assertNull(PushEvent.from(mapOf("type" to "REMIND", "challengeId" to "7")))
    }

    @Test
    fun `type 키가 없으면 null 을 반환한다`() {
        assertNull(PushEvent.from(mapOf("challengeId" to "7")))
    }

    @Test
    fun `challengeId 키가 없으면 null 을 반환한다`() {
        assertNull(PushEvent.from(mapOf("type" to "CHALLENGE_ACCEPTED")))
    }

    @Test
    fun `challengeId 가 숫자가 아니면 null 을 반환한다`() {
        assertNull(PushEvent.from(mapOf("type" to "CHALLENGE_ACCEPTED", "challengeId" to "abc")))
    }

    @Test
    fun `challengeId 가 빈 문자열이면 null 을 반환한다`() {
        assertNull(PushEvent.from(mapOf("type" to "CHALLENGE_ACCEPTED", "challengeId" to "")))
    }

    @Test
    fun `KEYS 는 소비 후 지워야 할 키를 모두 담는다`() {
        assertTrue(PushEvent.KEY_TYPE in PushEvent.KEYS)
        assertTrue(PushEvent.KEY_CHALLENGE_ID in PushEvent.KEYS)
        assertEquals(2, PushEvent.KEYS.size)
    }
}
