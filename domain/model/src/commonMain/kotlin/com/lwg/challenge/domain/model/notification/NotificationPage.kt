package com.lwg.challenge.domain.model.notification

/**
 * 알림 목록 한 쪽.
 *
 * @property notifications 최신순. 서버가 준 순서를 앱이 재정렬하지 않는다
 * @property nextCursor 다음 쪽 요청에 **그대로 되돌려 보낼** 값. `null` 이면 끝이다.
 *   불리언 `hasNext` 가 아니라 값 자체를 담는 이유는, 불리언이면 앱이 *"마지막 항목의 id 를
 *   커서로 쓴다"* 는 **규칙의 사본**을 갖게 되고 서버가 커서 표현을 바꾸는 날 그 사본이 조용히
 *   틀리기 때문이다
 */
data class NotificationPage(
    val notifications: List<Notification>,
    val nextCursor: Long?,
)
