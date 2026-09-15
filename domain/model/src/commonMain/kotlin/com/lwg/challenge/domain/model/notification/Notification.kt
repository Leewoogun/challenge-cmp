package com.lwg.challenge.domain.model.notification

import kotlinx.datetime.LocalDateTime

/**
 * 알림 목록 1건.
 *
 * @property notificationId 알림 식별자 (목록 key)
 * @property type 알림 종류. **enum 이 아니라 String 이다** — 서버 enum 에 9종이 선언돼 있는데 실제
 *   발송은 5종뿐이라(2026-09-05 기준) 값 추가가 예정돼 있고, typed 로 받으면 새 타입 한 행이
 *   응답 **전체**의 역직렬화를 깨뜨려 알림 화면이 통째로 빈다. 모르는 값의 표시·탭 가능 여부는
 *   화면이 판정한다. `ActiveChallengeDto.status` 가 같은 판단의 선례다.
 * @property body 화면에 그리는 문장. 서버가 발송 시점에 박제한다
 * @property referenceId 알림이 가리키는 대상 식별자. 대상이 없는 타입이면 null
 * @property referencePending 참조 대상이 아직 응답을 기다리는가. `true` 대기 /
 *   `false` 처리·만료·종료·삭제 / `null` 이 타입에는 대기라는 개념이 없음. 세 값이 서로 다른 사실이라
 *   한쪽으로 접으면 안 된다 — `null` 을 `false` 로 접으면 대기 개념이 없는 타입이 통째로 막히고,
 *   `true` 로 접으면 이미 끝난 대상이 열린다
 * @property createdAt 알림 생성 시각 (KST). 표시용 경과 시간은 표시 시점에 계산
 */
data class Notification(
    val notificationId: Long,
    val type: String,
    val body: String,
    val referenceId: Long?,
    val referencePending: Boolean?,
    val createdAt: LocalDateTime,
)
