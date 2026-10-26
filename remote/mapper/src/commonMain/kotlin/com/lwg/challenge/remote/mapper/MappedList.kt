package com.lwg.challenge.remote.mapper

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
