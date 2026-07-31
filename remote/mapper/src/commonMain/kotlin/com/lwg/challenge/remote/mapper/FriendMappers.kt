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

/**
 * Friend DTO → Domain 매핑.
 *
 * 시간 파싱은 DTO 직렬화기(WireLocalDateTimeSerializer)가 하고, 실패는 null 로 도착한다.
 * `since` / `requestedAt` 은 치명 필드가 아니라 null 이어도 항목을 유지한다.
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
    since = since,
)

fun ReceivedFriendRequestsResponse.toFriendRequests(): List<FriendRequest> =
    data.requests.map(FriendRequestDto::toFriendRequest)

fun FriendRequestDto.toFriendRequest(): FriendRequest = FriendRequest(
    requestId = id,
    fromUser = fromUser.toUserBrief(),
    requestedAt = requestedAt,
)

fun UserBriefDto.toUserBrief(): UserBrief = UserBrief(
    userId = id,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
)
