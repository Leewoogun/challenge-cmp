package com.lwg.challenge.domain.model.challenge

import kotlinx.datetime.YearMonth

/**
 * 계약서 보관함 한 달치.
 *
 * @property month 서버가 **실제로 조회한** 달. 앱이 요청한 달이 아니라 응답이 말하는 달이라,
 *   상한(이번 달) 판정과 늦게 도착한 stale 응답 폐기를 이 값으로 한다.
 * @property firstArchivedMonth 가장 오래된 종료 챌린지의 달. `null` 은 결측이 아니라 **종료한
 *   챌린지가 하나도 없다**는 뜻이라, 이전 달로 이동할 수 있는지와 *"이 달만 비었다"* vs
 *   *"기록이 아예 없다"* 를 둘 다 이 값이 결정한다. 사용자 전역 값이라 요청한 달과 무관하게
 *   매 응답 같은 값이 온다.
 * @property histories [month] 한 달치 기록. 서버가 준 `challengeDate` 내림차순을 보존한다.
 */
data class ChallengeArchive(
    val month: YearMonth,
    val firstArchivedMonth: YearMonth?,
    val histories: List<ChallengeHistory>,
)
