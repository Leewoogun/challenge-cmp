package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.remote.model.friend.FriendDto
import com.lwg.challenge.remote.model.friend.FriendListData
import com.lwg.challenge.remote.model.friend.FriendRequestDto
import com.lwg.challenge.remote.model.friend.FriendsResponse
import com.lwg.challenge.remote.model.friend.ReceivedFriendRequestsResponse
import com.lwg.challenge.remote.model.friend.ReceivedRequestListData
import com.lwg.challenge.remote.model.friend.UserBriefDto
import com.lwg.challenge.remote.model.friend.UserSearchDto
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FriendMappersTest {

    private val friendDto = FriendDto(
        id = 42L,
        nickname = "민수",
        profileImageUrl = null,
        since = LocalDateTime(2026, 6, 20, 12, 34, 56),
    )

    private val requestDto = FriendRequestDto(
        id = 901L,
        fromUser = UserBriefDto(id = 42L, nickname = "민수", profileImageUrl = null),
        requestedAt = LocalDateTime(2026, 6, 24, 18, 0, 0),
    )

    @Test
    fun `since 정상 wire 포맷 - LocalDateTime 으로 파싱`() {
        val friends = FriendsResponse(FriendListData(friends = listOf(friendDto))).toFriends()

        val friend = friends.single()
        assertEquals(42L, friend.userId)
        assertEquals("민수", friend.nickname)
        assertEquals(LocalDateTime(2026, 6, 20, 12, 34, 56), friend.since)
    }

    @Test
    fun `since 가 null - 항목은 유지`() {
        val friends = FriendsResponse(
            FriendListData(friends = listOf(friendDto.copy(since = null))),
        ).toFriends()

        val friend = friends.single()
        assertEquals(42L, friend.userId)
        assertNull(friend.since)
    }

    @Test
    fun `requestedAt 정상 wire 포맷 - LocalDateTime 으로 파싱`() {
        val requests = ReceivedFriendRequestsResponse(
            ReceivedRequestListData(requests = listOf(requestDto)),
        ).toFriendRequests()

        val request = requests.single()
        assertEquals(901L, request.requestId)
        assertEquals(42L, request.fromUser.userId)
        assertEquals(LocalDateTime(2026, 6, 24, 18, 0, 0), request.requestedAt)
    }

    @Test
    fun `requestedAt 이 null - 항목은 유지`() {
        val requests = ReceivedFriendRequestsResponse(
            ReceivedRequestListData(requests = listOf(requestDto.copy(requestedAt = null))),
        ).toFriendRequests()

        val request = requests.single()
        assertEquals(901L, request.requestId)
        assertNull(request.requestedAt)
    }

    @Test
    fun `Relation 알 수 없는 값 - NONE fallback`() {
        val result = UserSearchDto(
            id = 42L,
            nickname = "민수",
            relation = "SOMETHING_NEW",
        ).toUserSearchResult()

        assertEquals(Relation.NONE, result.relation)
    }
}
