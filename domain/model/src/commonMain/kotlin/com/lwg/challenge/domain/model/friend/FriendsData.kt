package com.lwg.challenge.domain.model.friend

/**
 * 친구 탭에 필요한 데이터 묶음 — 받은 요청 + 친구 목록.
 */
data class FriendsData(
    val receivedRequests: List<FriendRequest>,
    val friends: List<Friend>,
)
