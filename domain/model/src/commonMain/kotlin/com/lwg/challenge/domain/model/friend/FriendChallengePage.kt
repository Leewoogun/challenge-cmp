package com.lwg.challenge.domain.model.friend

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import kotlinx.datetime.LocalDate

/**
 * 이 친구와의 지난 챌린지 목록 한 쪽.
 *
 * @property challenges `id DESC`. [FriendPastChallenge.challengeDate] 순서가 아닐 수 있다
 * @property nextChallengeId 다음 쪽 요청에 **그대로 되돌려 보낼** 값(다음 쪽 첫 항목의 id). 마지막 항목 id 로 만들면 경계 항목이 중복된다
 * @property isPageEnd 마지막 쪽인가. 더 받을지는 이 값으로만 판단한다
 */
data class FriendChallengePage(
    val challenges: List<FriendPastChallenge>,
    val nextChallengeId: Long?,
    val isPageEnd: Boolean,
)

/**
 * 이 친구와의 종료된 챌린지 1건.
 *
 * @property challengeDate 표시용 챌린지 날짜(= 마감일). 정렬 키가 아니다
 * @property myResult 내 시점 결과. `BOTH_LOSE` 는 `LOSE` 와 별개 값이다
 */
data class FriendPastChallenge(
    val challengeId: Long,
    val challengeDate: LocalDate,
    val myMission: String,
    val friendMission: String,
    val bet: String,
    val myResult: ChallengeOutcome,
)
