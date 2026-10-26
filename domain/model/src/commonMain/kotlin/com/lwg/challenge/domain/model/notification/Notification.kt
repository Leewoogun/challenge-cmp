package com.lwg.challenge.domain.model.notification

import kotlinx.datetime.LocalDateTime

/**
 * 알림 목록 1건.
 *
 * @property notificationId 알림 식별자 (목록 key)
 * @property type 알림 종류. 새 타입이 추가돼도 목록 전체가 깨지지 않게 enum 이 아닌 String 으로 받는다
 * @property body 화면에 그리는 문장
 * @property referenceId 알림이 가리키는 대상 식별자. 대상이 없는 타입이면 null
 * @property referencePending 참조 대상이 아직 응답을 기다리는가. `true` 대기 / `false` 처리·만료·종료 /
 *   `null` 대기 개념이 없는 타입. 세 값을 한쪽으로 접지 않는다
 * @property createdAt 알림 생성 시각 (KST)
 */
data class Notification(
    val notificationId: Long,
    val type: String,
    val body: String,
    val referenceId: Long?,
    val referencePending: Boolean?,
    val createdAt: LocalDateTime,
)
