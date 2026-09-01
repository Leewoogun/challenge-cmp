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
    fun `OPPONENT_VERIFIED 데이터를 OpponentVerified 로 읽는다`() {
        val event = PushEvent.from(mapOf("type" to "OPPONENT_VERIFIED", "challengeId" to "7"))

        assertEquals(PushEvent.OpponentVerified(7L), event)
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
    fun `of 는 CHALLENGE_REQUEST 를 ChallengeRequest 로 만든다`() {
        assertEquals(PushEvent.ChallengeRequest(7L), PushEvent.of("CHALLENGE_REQUEST", 7L))
    }

    @Test
    fun `of 는 CHALLENGE_ACCEPTED 를 ChallengeAccepted 로 만든다`() {
        assertEquals(PushEvent.ChallengeAccepted(7L), PushEvent.of("CHALLENGE_ACCEPTED", 7L))
    }

    @Test
    fun `of 는 CHALLENGE_REJECTED 를 ChallengeRejected 로 만든다`() {
        assertEquals(PushEvent.ChallengeRejected(7L), PushEvent.of("CHALLENGE_REJECTED", 7L))
    }

    @Test
    fun `of 는 OPPONENT_VERIFIED 를 OpponentVerified 로 만든다`() {
        assertEquals(PushEvent.OpponentVerified(7L), PushEvent.of("OPPONENT_VERIFIED", 7L))
    }

    @Test
    fun `of 는 모르는 타입이면 null 을 낸다`() {
        assertNull(PushEvent.of("SIGN_REQUEST", 7L))
    }

    @Test
    fun `of 는 referenceId 가 null 이면 null 을 낸다`() {
        assertNull(PushEvent.of("CHALLENGE_ACCEPTED", null))
    }

    /**
     * 아래 네 단언이 리터럴인 이유: 알림 목록은 타입 리터럴을 한 건도 갖지 않고 전부 이 상수를
     * 참조한다. 그래서 상수 값이 서버 어휘와 어긋나면 아이콘·색·목적지가 **한꺼번에** 폴백으로
     * 내려앉는데, 그 자리에서는 아무것도 실패하지 않는다. 상수끼리 비교하면 값이 틀려도 통과하므로
     * 서버가 쓰는 문자열을 여기 한 곳에 박아 둔다.
     */
    @Test
    fun `TYPE_CHALLENGE_REQUEST 는 서버가 보내는 문자열과 같다`() {
        assertEquals("CHALLENGE_REQUEST", PushEvent.TYPE_CHALLENGE_REQUEST)
    }

    @Test
    fun `TYPE_CHALLENGE_ACCEPTED 는 서버가 보내는 문자열과 같다`() {
        assertEquals("CHALLENGE_ACCEPTED", PushEvent.TYPE_CHALLENGE_ACCEPTED)
    }

    @Test
    fun `TYPE_CHALLENGE_REJECTED 는 서버가 보내는 문자열과 같다`() {
        assertEquals("CHALLENGE_REJECTED", PushEvent.TYPE_CHALLENGE_REJECTED)
    }

    @Test
    fun `TYPE_OPPONENT_VERIFIED 는 서버가 보내는 문자열과 같다`() {
        assertEquals("OPPONENT_VERIFIED", PushEvent.TYPE_OPPONENT_VERIFIED)
    }

    @Test
    fun `KEYS 는 소비 후 지워야 할 키를 모두 담는다`() {
        assertTrue(PushEvent.KEY_TYPE in PushEvent.KEYS)
        assertTrue(PushEvent.KEY_CHALLENGE_ID in PushEvent.KEYS)
        assertEquals(2, PushEvent.KEYS.size)
    }
}
