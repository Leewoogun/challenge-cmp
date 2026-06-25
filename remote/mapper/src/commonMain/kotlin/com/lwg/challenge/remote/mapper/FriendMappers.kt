@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.remote.model.friend.FriendDto
import com.lwg.challenge.remote.model.friend.FriendRequestDto
import com.lwg.challenge.remote.model.friend.FriendsResponse
import com.lwg.challenge.remote.model.friend.ReceivedFriendRequestsResponse
import com.lwg.challenge.remote.model.friend.UserBriefDto
import com.lwg.challenge.remote.model.friend.UserSearchDto
import com.lwg.challenge.remote.model.friend.UserSearchResponse
import kotlin.time.Instant

/**
 * Friend DTO → Domain 매핑.
 *
 * 시간 변환: DTO 의 `String` (ISO-8601 UTC) → Domain 의 [Instant].
 * 파싱 실패 시 [Instant.DISTANT_PAST] (백엔드 계약상 발생하면 안 되지만 방어적 처리).
 *
 * Relation 매핑: 알 수 없는 값은 [Relation.NONE] 으로 fallback ([Relation.fromCode]).
 */

fun UserSearchResponse.toUserSearchResults(): List<UserSearchResult> =
    data.users.map(UserSearchDto::toUserSearchResult)

fun UserSearchDto.toUserSearchResult(): UserSearchResult = UserSearchResult(
    userId = id,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    relation = Relation.fromCode(relation),
    pendingRequestId = pendingRequestId,
)

fun FriendsResponse.toFriends(): List<Friend> =
    data.friends.map(FriendDto::toFriend)

fun FriendDto.toFriend(): Friend = Friend(
    userId = id,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    since = parseInstant(since),
)

fun ReceivedFriendRequestsResponse.toFriendRequests(): List<FriendRequest> =
    data.requests.map(FriendRequestDto::toFriendRequest)

fun FriendRequestDto.toFriendRequest(): FriendRequest = FriendRequest(
    requestId = id,
    fromUser = fromUser.toUserBrief(),
    requestedAt = parseInstant(requestedAt),
)

fun UserBriefDto.toUserBrief(): UserBrief = UserBrief(
    userId = id,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
)

private fun parseInstant(value: String): Instant = runCatching {
    Instant.parse(value)
}.getOrDefault(Instant.DISTANT_PAST)
