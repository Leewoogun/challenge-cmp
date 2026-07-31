package com.lwg.challenge.remote.mapper

/**
 * 목록 매핑 결과. mapper 는 순수 함수라 `onError` 에 접근할 수 없으므로,
 * 치명 필드(deadline) 파싱 실패로 걸러진 개수를 반환값에 실어 repository 가 발화를 판단한다.
 *
 * @property items 매핑에 성공한 항목
 * @property droppedCount 파싱 실패로 제외된 항목 수
 */
data class MappedList<T>(
    val items: List<T>,
    val droppedCount: Int,
)
