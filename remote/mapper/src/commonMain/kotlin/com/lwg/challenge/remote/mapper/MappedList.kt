package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeArchive

/**
 * 목록 매핑 결과. mapper 는 순수 함수라 `onError` 에 접근할 수 없으므로,
 * 걸러낸 개수를 반환값에 실어 repository 가 발화를 판단한다.
 *
 * @property items 매핑에 성공한 항목
 * @property droppedCount 항목 단위로 그릴 수 없다고 판단해 제외한 건수
 *   (deadline 파싱 실패, 모르는 status 등 — 사유는 매퍼마다 다르다)
 */
data class MappedList<T>(
    val items: List<T>,
    val droppedCount: Int,
)

/**
 * 보관함 한 달치 매핑 결과. [MappedList] 와 같은 이유로 걸러낸 개수를 함께 싣는다.
 *
 * 항목 리스트가 아니라 [ChallengeArchive] 를 싣는 이유: 응답에 목록뿐 아니라 그 목록이 어느 달의
 * 것인지가 함께 오고, 둘을 쪼개면 호출부에서 다시 짝지어야 한다.
 *
 * @property droppedCount 날짜·결과를 못 읽어 제외한 건수. `month` 를 못 읽은 경우는 여기 세지 않는다 —
 *   그때는 결과 자체가 null 이다.
 */
data class MappedArchive(
    val archive: ChallengeArchive,
    val droppedCount: Int,
)
