package com.lwg.challenge.domain.model.challenge

/**
 * 챌린지 마감 기준.
 *
 * 클라이언트는 마감 timestamp 를 직접 계산해 보내지 않고 이 enum 만 전송한다.
 * 기기 시계 조작·타임존 불일치를 차단하기 위해 실제 시각 환산(KST 고정)은 서버가 전담한다.
 *
 * @property label 선택지에 노출하는 표기.
 * @property TODAY 오늘 자정 마감
 * @property TOMORROW 내일 자정 마감
 */
enum class DeadlineType(val label: String) {
    TODAY("오늘 자정"),
    TOMORROW("내일 자정"),
}
