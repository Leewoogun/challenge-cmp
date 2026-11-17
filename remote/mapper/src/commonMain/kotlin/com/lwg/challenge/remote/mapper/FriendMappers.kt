package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendChallengePage
import com.lwg.challenge.domain.model.friend.FriendChallengeStatus
import com.lwg.challenge.domain.model.friend.FriendOngoingChallenge
import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.FriendScore
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.remote.model.friend.FriendChallengeStatusDto
import com.lwg.challenge.remote.model.friend.FriendChallengesResponse
import com.lwg.challenge.remote.model.friend.FriendDto
import com.lwg.challenge.remote.model.friend.FriendOngoingChallengeDto
import com.lwg.challenge.remote.model.friend.FriendPastChallengeDto
import com.lwg.challenge.remote.model.friend.FriendRecordResponse
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

/** `deadline` 을 못 읽은 진행 중 항목은 카드를 그릴 수 없어 제외한다(홈 진행 중 매핑과 같은 판단). */
fun FriendRecordResponse.toFriendRecord(): FriendRecord = FriendRecord(
    friend = UserBrief(
        userId = data.friend.userId,
        nickname = data.friend.nickname,
        profileImageUrl = data.friend.profileImageUrl,
    ),
    score = FriendScore(
        myWins = data.score.myWins,
        friendWins = data.score.friendWins,
        draws = data.score.draws,
        bothLose = data.score.bothLose,
    ),
    ongoing = data.ongoing.mapNotNull(FriendOngoingChallengeDto::toFriendOngoingChallenge),
)

private fun FriendOngoingChallengeDto.toFriendOngoingChallenge(): FriendOngoingChallenge? {
    val deadline = deadline ?: return null
    return FriendOngoingChallenge(
        challengeId = challengeId,
        status = status.toDomain(),
        sentByMe = sentByMe,
        deadline = deadline,
        myMission = myMission,
        friendMission = friendMission,
        bet = bet,
    )
}

private fun FriendChallengeStatusDto.toDomain(): FriendChallengeStatus = when (this) {
    FriendChallengeStatusDto.PENDING -> FriendChallengeStatus.PENDING
    FriendChallengeStatusDto.IN_PROGRESS -> FriendChallengeStatus.IN_PROGRESS
}

/** 서버가 준 순서(`id DESC`)와 [FriendChallengePage.nextChallengeId]·[FriendChallengePage.isPageEnd] 를 손대지 않고 통과시킨다. */
fun FriendChallengesResponse.toFriendChallengePage(): FriendChallengePage = FriendChallengePage(
    challenges = data.challenges.map(FriendPastChallengeDto::toFriendPastChallenge),
    nextChallengeId = data.nextChallengeId,
    isPageEnd = data.isPageEnd,
)

private fun FriendPastChallengeDto.toFriendPastChallenge(): FriendPastChallenge = FriendPastChallenge(
    challengeId = challengeId,
    challengeDate = challengeDate,
    myMission = myMission,
    friendMission = friendMission,
    bet = bet,
    myResult = myResult.toDomain(),
)
