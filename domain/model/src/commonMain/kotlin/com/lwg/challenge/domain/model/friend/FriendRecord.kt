package com.lwg.challenge.domain.model.friend

import kotlinx.datetime.LocalDateTime

/**
 * 한 친구와의 스코어 + 진행 중 챌린지.
 *
 * @property friend 상대 친구
 * @property score 서버가 확정한 4값. 앱은 이 값들로 합계·패·승률을 파생하지 않는다
 * @property ongoing `PENDING` + `IN_PROGRESS`, 마감 임박순. 서버가 준 순서를 재정렬하지 않는다
 */
data class FriendRecord(
    val friend: UserBrief,
    val score: FriendScore,
    val ongoing: List<FriendOngoingChallenge>,
)

/**
 * @property myWins 내 관점 `WIN` 건수
 * @property friendWins 친구 관점 `WIN` 건수
 * @property draws `DRAW` 건수
 * @property bothLose `BOTH_LOSE` 건수. 내 패지만 친구 승이 아니라 [friendWins] 에 들어가지 않는다
 */
data class FriendScore(
    val myWins: Int,
    val friendWins: Int,
    val draws: Int,
    val bothLose: Int,
)

/**
 * 이 친구와 진행 중인 챌린지 1건.
 *
 * @property sentByMe 내가 신청자인가. `PENDING` 의 보낸/받은 방향은 미션 null 로 역산하지 않고 이 값으로 가른다
 * @property myMission `PENDING` 이고 내가 받은 쪽이면 아직 null
 * @property friendMission `PENDING` 이고 내가 보낸 쪽이면 아직 null
 */
data class FriendOngoingChallenge(
    val challengeId: Long,
    val status: FriendChallengeStatus,
    val sentByMe: Boolean,
    val deadline: LocalDateTime,
    val myMission: String?,
    val friendMission: String?,
    val bet: String,
)

enum class FriendChallengeStatus {
    PENDING,
    IN_PROGRESS,
}
