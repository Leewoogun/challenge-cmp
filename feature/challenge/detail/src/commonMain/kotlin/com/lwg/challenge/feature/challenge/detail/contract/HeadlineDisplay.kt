package com.lwg.challenge.feature.challenge.detail.contract

import androidx.compose.runtime.Immutable
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ChallengeResult
import com.lwg.challenge.ui.components.RESULT_BOTH_LOSE_TEXT
import com.lwg.challenge.ui.components.RESULT_DRAW_TEXT
import com.lwg.challenge.ui.components.RESULT_LOSE_TEXT
import com.lwg.challenge.ui.components.RESULT_WIN_TEXT
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
 * 헤더 표기의 강조 수준. 색·아이콘 토큰은 state 에 담을 수 없어 표현 상태로만 내리고 화면이 매핑한다.
 *
 * [NEUTRAL] 과 [DECIDED_NEUTRAL] 은 둘 다 색을 어느 쪽으로도 기울이지 않는다. 그래도 값을 가른 이유는
 * **아이콘이 갈리기 때문**이다 — 색만 보면 하나로 합쳐도 되지만, 그러면 결과가 이미 나온 문구에 아직
 * 기다리는 중이라는 시계가 붙는다.
 */
enum class HeadlineTone {

    /** 마감 전. 아직 손쓸 수 있어 행동을 촉구한다. */
    URGENT,

    /** 아직 결과가 없는 중립 — 마감 후 판정 전. 기다리는 중이라 시계를 든다. */
    NEUTRAL,

    /** 결과는 있는데 관점을 모르는 중립 — 누군가는 이겼으니 시계가 아니고, 그게 보는 사람 것인지 몰라 색만 거둔다. */
    DECIDED_NEUTRAL,

    /** 내가 이겼다. */
    WIN,

    /** 내가 졌거나 양측이 모두 졌다. */
    LOSE,

    /** 무승부. */
    DRAW,
}

/**
 * 헤더 한 자리에 들어갈 문구와 강조를 **한 묶음으로** 확정한 값.
 *
 * 둘을 따로 만들면 서로 다른 순간의 시계·서로 다른 판정을 보게 돼 "판정 대기 중" 이 촉구색으로
 * 나오거나 패배 문구에 승리색이 붙는 조합이 생긴다. 한 함수가 이 타입을 통째로 돌려주면 그 어긋남이
 * 구조적으로 불가능해진다.
 */
@Immutable
data class HeadlineDisplay(val text: String, val tone: HeadlineTone)

/**
 * 결과를 어느 관점으로 그릴지.
 *
 * 관점 판정은 내 userId 를 아는 ViewModel 에서 이미 끝나 있다 — [headlineDisplay] 도 화면도
 * "내가 어느 쪽인가" 를 다시 묻지 않는다.
 */
sealed interface ResultView {

    /** 내가 어느 쪽인지 안다 — 승패를 내 것으로 말한다. */
    data class Mine(val outcome: ChallengeOutcome) : ResultView

    /**
     * 관점 미상 — 역할 기준 실명으로만 말한다.
     *
     * 서명·미션 라벨이 관점을 모를 때 "나" 를 버리고 실명으로 낮추는 것과 같은 폴백이다.
     */
    data class ByRole(
        val result: ChallengeResult,
        val challengerNickname: String,
        val opponentNickname: String,
    ) : ResultView
}

/**
 * 헤더 한 자리에 들어갈 문구·강조를 한 번에 확정한다. 남은 시간 → 판정 대기 → 판정 결과가 시간 순으로
 * 같은 자리를 물려받는다.
 *
 * 분기 순서가 곧 계약이다. [result] 를 시계보다 먼저 보므로, `status` 가 `COMPLETED` 인데 [result] 가
 * `null` 인 조합 — 서버 불변식(`status == COMPLETED ⟺ result != null`)이 깨진 경우 — 은 방어 분기
 * 없이 [DEADLINE_AWAITING_JUDGEMENT_TEXT] 로 떨어진다. 없는 결과를 상태 문자열에서 날조하는 것보다
 * 아직 안 나왔다고 말하는 쪽이 정직하고, 서버가 이미 가진 판정 규칙을 앱에 두 번째로 심지 않는다.
 *
 * @param now 호출부가 읽은 시각. 기본값을 두지 않는 이유는 문구와 톤이 서로 다른 순간의 시계를 보면
 *   "판정 대기 중" 이 촉구색으로 나올 수 있기 때문이다 — 반환을 [HeadlineDisplay] 로 묶은 것과 같은
 *   이유로, 시계도 호출부가 한 번 읽은 값을 그대로 쓴다.
 */
internal fun headlineDisplay(
    result: ResultView?,
    deadline: LocalDateTime,
    now: LocalDateTime,
): HeadlineDisplay = when {
    result != null -> resultHeadline(result)

    deadline <= now -> HeadlineDisplay(
        text = DEADLINE_AWAITING_JUDGEMENT_TEXT,
        tone = HeadlineTone.NEUTRAL,
    )

    else -> HeadlineDisplay(
        text = DEADLINE_REMAINING_PREFIX + deadline.toRelativeKoreanString(now),
        tone = HeadlineTone.URGENT,
    )
}

private fun resultHeadline(result: ResultView): HeadlineDisplay = when (result) {
    is ResultView.Mine -> result.outcome.toHeadline()
    is ResultView.ByRole -> result.toHeadline()
}

private fun ChallengeOutcome.toHeadline(): HeadlineDisplay = when (this) {
    ChallengeOutcome.WIN -> HeadlineDisplay(RESULT_WIN_TEXT, HeadlineTone.WIN)
    ChallengeOutcome.LOSE -> HeadlineDisplay(RESULT_LOSE_TEXT, HeadlineTone.LOSE)
    ChallengeOutcome.DRAW -> HeadlineDisplay(RESULT_DRAW_TEXT, HeadlineTone.DRAW)
    ChallengeOutcome.BOTH_LOSE -> HeadlineDisplay(RESULT_BOTH_LOSE_TEXT, HeadlineTone.LOSE)
}

/**
 * 관점을 모를 때의 표기. 승패에 [HeadlineTone.DECIDED_NEUTRAL] 을 쓴다.
 *
 * 누가 이겼는지는 알아도 그게 **보는 사람에게** 좋은 소식인지는 모른다. 승리색을 칠하면 진 사람에게
 * 축하색을 보여줄 수 있다. 그렇다고 [HeadlineTone.NEUTRAL] 로 내리면 결과가 이미 난 문구에 판정을
 * 기다리는 시계가 붙으므로, 색만 거두고 결과가 났다는 사실은 남기는 값을 따로 쓴다.
 *
 * 무승부와 양측 패배는 어느 쪽에서 봐도 같은 사실이라 톤을 그대로 쓴다.
 *
 * 관점 미상일 때 "나" 대신 실명으로 낮추는 라벨 폴백과 같은 판단이다.
 */
private fun ResultView.ByRole.toHeadline(): HeadlineDisplay = when (result) {
    ChallengeResult.CHALLENGER_WIN -> HeadlineDisplay(
        text = "$challengerNickname $RESULT_WIN_TEXT",
        tone = HeadlineTone.DECIDED_NEUTRAL,
    )

    ChallengeResult.OPPONENT_WIN -> HeadlineDisplay(
        text = "$opponentNickname $RESULT_WIN_TEXT",
        tone = HeadlineTone.DECIDED_NEUTRAL,
    )

    ChallengeResult.DRAW -> HeadlineDisplay(RESULT_DRAW_TEXT, HeadlineTone.DRAW)
    ChallengeResult.BOTH_LOSE -> HeadlineDisplay(RESULT_BOTH_LOSE_TEXT, HeadlineTone.LOSE)
}
