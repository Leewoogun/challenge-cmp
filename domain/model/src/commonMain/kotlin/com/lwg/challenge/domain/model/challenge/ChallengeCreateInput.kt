package com.lwg.challenge.domain.model.challenge

import com.lwg.challenge.domain.model.signature.Signature
import kotlinx.datetime.LocalDate

/**
 * 챌린지 신청 입력값.
 *
 * @property opponentId 도전장을 받을 친구의 사용자 식별자
 * @property myMission 내가 수행할 미션 (1~100자)
 * @property penalty 내기 내용 (1~100자)
 * @property challengeDate 마감 날짜(KST). 이 날짜가 끝나는 순간이 마감이며, 날짜만 보내고 마감 순간은 서버가 만든다
 * @property signature 챌린저(나)의 영혼의 맹세 서명. 빈 서명은 저장 시도 전에 거부된다
 */
data class ChallengeCreateInput(
    val opponentId: Long,
    val myMission: String,
    val penalty: String,
    val challengeDate: LocalDate,
    val signature: Signature,
)
