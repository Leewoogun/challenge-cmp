@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.domain.model.friend

import kotlin.time.Instant

/**
 * 받은 친구 요청 1건 (PENDING).
 *
 * @property requestId `friendships.id` (PENDING row)
 * @property fromUser 요청 보낸 사용자
 * @property requestedAt 요청 시각 (friendships.created_at)
 */
data class FriendRequest(
    val requestId: Long,
    val fromUser: UserBrief,
    val requestedAt: Instant,
)

/**
 * 친구 요청 발신자의 최소 프로필 (id, nickname, profileImageUrl).
 */
data class UserBrief(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
)
