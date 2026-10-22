package com.lwg.challenge.feature.challenge.detail.contract

import androidx.compose.runtime.Immutable
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.ui.components.RESULT_BOTH_LOSE_TEXT
import com.lwg.challenge.ui.components.RESULT_DRAW_TEXT
import com.lwg.challenge.ui.components.RESULT_LOSE_TEXT
import com.lwg.challenge.ui.components.RESULT_WIN_TEXT
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import kotlinx.datetime.LocalDateTime

internal const val DEADLINE_REMAINING_PREFIX = "남은 시간: "

internal const val DEADLINE_AWAITING_JUDGEMENT_TEXT = "판정 대기 중"

enum class HeadlineTone {

    /** 마감 전. 아직 손쓸 수 있어 행동을 촉구한다. */
    URGENT,

    /** 아직 결과가 없는 중립 — 마감 후 판정 전. 기다리는 중이라 시계를 든다. */
    NEUTRAL,

    /** 내가 이겼다. */
    WIN,

    /** 내가 졌거나 양측이 모두 졌다. */
    LOSE,

    /** 무승부. */
    DRAW,
}

@Immutable
data class HeadlineDisplay(val text: String, val tone: HeadlineTone)

internal fun headlineDisplay(
    result: ChallengeOutcome?,
    deadline: LocalDateTime,
    now: LocalDateTime,
): HeadlineDisplay = when {
    result != null -> result.toHeadline()

    deadline <= now -> HeadlineDisplay(
        text = DEADLINE_AWAITING_JUDGEMENT_TEXT,
        tone = HeadlineTone.NEUTRAL,
    )

    else -> HeadlineDisplay(
        text = DEADLINE_REMAINING_PREFIX + deadline.toRelativeKoreanString(now),
        tone = HeadlineTone.URGENT,
    )
}

private fun ChallengeOutcome.toHeadline(): HeadlineDisplay = when (this) {
    ChallengeOutcome.WIN -> HeadlineDisplay(RESULT_WIN_TEXT, HeadlineTone.WIN)
    ChallengeOutcome.LOSE -> HeadlineDisplay(RESULT_LOSE_TEXT, HeadlineTone.LOSE)
    ChallengeOutcome.DRAW -> HeadlineDisplay(RESULT_DRAW_TEXT, HeadlineTone.DRAW)
    ChallengeOutcome.BOTH_LOSE -> HeadlineDisplay(RESULT_BOTH_LOSE_TEXT, HeadlineTone.LOSE)
}
