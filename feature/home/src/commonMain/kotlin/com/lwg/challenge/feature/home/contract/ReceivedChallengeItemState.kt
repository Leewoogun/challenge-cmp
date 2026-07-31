@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.home.contract

import androidx.compose.runtime.Immutable
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.utils.datetime.KST
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.hours

/**
 * 받은 도전장 1건 UI State.
 *
 * `now` 는 ViewModel 의 매핑 시점에 1회 측정해서 주입한다 —
 * Composable 재구성마다 시계를 읽으면 ItemState 동일성이 깨지고
 * 테스트가 불가능해진다 (`FriendRequestItemState` 선례).
 */
@Immutable
data class ReceivedChallengeItemState(
    internal val model: ReceivedChallenge,
    private val now: LocalDateTime,
) {

    val challengeId: Long get() = model.challengeId
    val challengerNickname: String get() = model.challengerNickname
    val challengerProfileImageUrl: String? get() = model.challengerProfileImageUrl
    val challengerMission: String get() = model.challengerMission
    val betContent: String get() = model.betContent

    val titleText: String get() = "${model.challengerNickname}의 도전장"
    val missionLabelText: String get() = "${model.challengerNickname}의 미션"
    val betText: String get() = "내기: ${model.betContent}"

    /** `toRelativeKoreanString()` 출력 그대로. `" 남음"` 같은 접미사를 붙이면 "곧 마감 남음" 이 된다. */
    val deadlineText: String get() = model.deadline.toRelativeKoreanString(now)

    /** 마감 임박(강조 색 전환) 판정. 문자열 파싱이 아니라 KST 순간으로 환산한 간격 비교로 한다. */
    val isUrgent: Boolean get() = (model.deadline.toInstant(KST) - now.toInstant(KST)) <= 1.hours
}
