package com.lwg.challenge.feature.challenge.detail.contract

import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import kotlinx.datetime.LocalDateTime

/** 마감 전 남은 시간 앞에 붙는 접두어. 접두어 부착은 ViewModel 이 끝내고 화면은 그리기만 한다. */
internal const val DEADLINE_REMAINING_PREFIX = "남은 시간: "

/**
 * 마감 직후 ~ 자정 배치 판정 전 구간의 표기.
 *
 * [toRelativeKoreanString] 은 이 구간에도 "마감" 을 내는데, "남은 시간: 마감" 은 남은 시간을 묻는
 * 문장에 끝났다는 답을 넣은 꼴이라 접두어 없는 별개 문장으로 갈아 끼운다.
 */
internal const val DEADLINE_AWAITING_JUDGEMENT_TEXT = "판정 대기 중"

/**
 * 마감 표기의 강조 수준. 색 토큰은 state 에 담을 수 없어 표현 상태로만 내리고 화면이 매핑한다.
 */
enum class DeadlineTone {

    /** 마감 전. */
    URGENT,

    /** 마감 후. */
    NEUTRAL,
}

/**
 * 마감 표기 문장. 마감 전후로 접두어 유무까지 갈리므로 여기서 한 문장으로 확정한다.
 *
 * @param now 호출부가 읽은 시각. 기본값을 두지 않는 이유는 문자열과 [deadlineDisplayTone] 이
 *   서로 다른 순간의 시계를 보면 "판정 대기 중" 이 촉구색으로 나올 수 있기 때문이다.
 */
internal fun deadlineDisplayText(deadline: LocalDateTime, now: LocalDateTime): String =
    if (deadline <= now) {
        DEADLINE_AWAITING_JUDGEMENT_TEXT
    } else {
        DEADLINE_REMAINING_PREFIX + deadline.toRelativeKoreanString(now)
    }

/**
 * 마감 전 warning 은 "서둘러라" 는 행동 촉구다. 마감 후에는 사용자가 할 수 있는 게 없어
 * 촉구색을 그대로 두면 없는 행동을 재촉하게 되므로 톤을 거둔다.
 */
internal fun deadlineDisplayTone(deadline: LocalDateTime, now: LocalDateTime): DeadlineTone =
    if (deadline <= now) DeadlineTone.NEUTRAL else DeadlineTone.URGENT
