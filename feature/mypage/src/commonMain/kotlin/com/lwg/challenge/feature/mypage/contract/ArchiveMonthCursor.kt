package com.lwg.challenge.feature.mypage.contract

import androidx.compose.runtime.Immutable
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plusMonth

/**
 * 보관함이 지금 보고 있는 달과, 그 달에서 어디로 움직일 수 있는지.
 *
 * 월 이동의 경계 규칙은 전부 이 타입이 소유한다 — 화면과 ViewModel 은 여기서 계산된
 * [canGoPrevious]·[canGoNext] 를 읽기만 하고 스스로 경계를 판단하지 않는다.
 *
 * [previous]·[next] 가 경계에서 예외 대신 `this` 를 돌려주는 이유: 비활성 버튼을 눌러도 아무 일이
 * 없어야 하고, 던지면 호출부가 이동 전에 경계 검사를 한 번 더 하게 되어 같은 규칙의 사본이 두 곳에
 * 생긴다. 경계 판단이 갈라지는 순간 한쪽만 고친 버그가 나오므로 판단은 이 타입 안에만 둔다.
 *
 * 기록이 없는 달도 건너뛰지 않고 그대로 지나간다. 이 타입은 '그 달에 기록이 있는지' 를 아예 모르며
 * 이는 의도된 설계다 — 데이터 유무로 이동 폭이 달라지면 한 번 누르면 한 달이 간다는 달력의 시간
 * 흐름과 어긋나고, 사용자는 자신이 몇 월을 건너뛰었는지 알 수 없게 된다.
 *
 * @property selected 지금 보고 있는 달.
 * @property latest 이동 상한이자 이번 달. 미래 달에는 종료된 챌린지가 존재할 수 없어 열어 둘 이유가 없다.
 * @property earliest 이동 하한이자 가장 오래된 종료 챌린지가 속한 달. `null` 은 종료한 챌린지가 하나도
 *  없는 사용자를 뜻하며, 그때 이전 이동이 막히는 것은 정상이다 — 뒤에 아무것도 없는 달들을 무한히
 *  거슬러 올라가게 두지 않는다.
 */
@Immutable
data class ArchiveMonthCursor(
    val selected: YearMonth,
    val latest: YearMonth,
    val earliest: YearMonth?,
) {

    val label: String get() = "${selected.year}년 ${selected.month.number}월"

    /**
     * 빈 달 본문의 제목. Composable 에서 문자열을 조립하지 않는다는 규칙 때문에 화면이 아니라
     * [label] 과 같은 자리에서 완성해 넘긴다.
     *
     * 제목에 월을 넣는 것이 빈 달 화면의 핵심 결정이다. 빈 달을 연속으로 지날 때 화면에서 바뀌는 것이
     * 작은 월 라벨 하나뿐이면 탭이 먹었는지 확신할 수 없어 같은 자리를 다시 누르게 된다. 화면 한가운데
     * bold16 이 함께 바뀌면 그게 **입력 피드백** 역할을 하므로, 월 라벨과의 정보 중복이 아니다.
     */
    val emptyMonthTitle: String get() = "${selected.month.number}월엔 끝낸 챌린지가 없어요"

    val canGoPrevious: Boolean get() = earliest != null && selected > earliest

    val canGoNext: Boolean get() = selected < latest

    fun previous(): ArchiveMonthCursor =
        if (canGoPrevious) copy(selected = selected.minusMonth()) else this

    fun next(): ArchiveMonthCursor =
        if (canGoNext) copy(selected = selected.plusMonth()) else this
}
