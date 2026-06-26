@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.friends.contract

import androidx.compose.runtime.Immutable
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserSearchResult
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * 검색 결과 1행 UI State.
 *
 * relation 별 액션 라벨 / 활성화 / 취소 버튼 노출 여부를 `get()` 으로 캡슐화.
 * (UI State 파싱 규칙 — challenge-app/CLAUDE.md 104-114)
 */
@Immutable
data class FriendSearchItemState(
    internal val model: UserSearchResult
) {

    val userId: Long get() = model.userId
    val nickname: String get() = model.nickname
    val profileImageUrl: String? get() = model.profileImageUrl
    val relation: Relation get() = model.relation
    val pendingRequestId: Long? get() = model.pendingRequestId

    /** 주 액션 버튼 라벨 (relation 분기). */
    val actionLabel: String
        get() = when (model.relation) {
            Relation.NONE -> "친구 요청"
            Relation.REQUEST_SENT -> "요청 보냄"
            Relation.REQUEST_RECEIVED -> "수락"
            Relation.FRIEND -> "이미 친구"
            Relation.REJECTED -> "다시 요청"
        }

    /** FRIEND 상태에서는 비활성. */
    val isActionEnabled: Boolean get() = model.relation != Relation.FRIEND

    /** REQUEST_SENT 일 때만 "X 취소" 노출. */
    val showCancel: Boolean get() = model.relation == Relation.REQUEST_SENT
}

/**
 * 친구 목록 1행 UI State.
 *
 * @property sinceText "친구가 된 지 X일" 같은 간단한 상대 표기.
 *
 * `now` 는 ViewModel 의 combine 시점에 1회 측정해서 주입한다 —
 * Composable 재구성마다 `Clock.System.now()` 를 호출하면 ItemState 동일성이 깨지고
 * 테스트가 불가능해진다.
 */
@Immutable
data class FriendItemState(
    internal val model: Friend,
    private val now: Instant,
) {

    val userId: Long get() = model.userId
    val nickname: String get() = model.nickname
    val profileImageUrl: String? get() = model.profileImageUrl
    val since: Instant get() = model.since

    /** 친구가 된 시점을 한국어 상대 표기로 변환 (간단 버전 — 일 단위). */
    val sinceText: String get() = formatSince(model.since, now)

    companion object {
        internal fun formatSince(since: Instant, now: Instant): String {
            val diff = now - since
            return when {
                diff < 1.days -> "오늘 친구가 됨"
                diff < 7.days -> "${diff.inWholeDays}일 전 친구가 됨"
                diff < 30.days -> "${diff.inWholeDays / 7}주 전 친구가 됨"
                diff < 365.days -> "${diff.inWholeDays / 30}달 전 친구가 됨"
                else -> "${diff.inWholeDays / 365}년 전 친구가 됨"
            }
        }
    }
}

/**
 * 받은 친구 요청 1행 UI State.
 *
 * `now` 는 ViewModel 측에서 측정해서 주입 (FriendItemState 동일).
 */
@Immutable
data class FriendRequestItemState(
    internal val model: FriendRequest,
    private val now: Instant,
) {

    val requestId: Long get() = model.requestId
    val fromUserId: Long get() = model.fromUser.userId
    val nickname: String get() = model.fromUser.nickname
    val profileImageUrl: String? get() = model.fromUser.profileImageUrl
    val requestedAt: Instant get() = model.requestedAt

    /** 요청 시각 한국어 상대 표기. */
    val requestedAtText: String
        get() = FriendItemState.formatSince(model.requestedAt, now)
            .replace("친구가 됨", "요청")
}
