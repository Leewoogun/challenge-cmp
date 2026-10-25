package com.lwg.challenge.feature.mypage.archive.contract

import androidx.compose.runtime.Immutable
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plusMonth

/**
 * 보관함이 지금 보고 있는 달과, 그 달에서 어디로 움직일 수 있는지.
 *
 * 월 이동의 경계 규칙은 전부 이 타입이 소유한다 — 화면과 ViewModel 은 여기서 계산된 [canGoNext] 를
 * 읽기만 하고 스스로 경계를 판단하지 않는다.
 *
 * **과거 방향에는 하한이 없다.** 기록이 없는 달도 계속 거슬러 올라갈 수 있고, 그때는 빈 달 화면이
 * 나온다. 사용자 확정 사항이다.
 *
 * 2차 개정에서 하한을 폐기한 이유: 하한이 '최초 기록월' 이던 시절, 종료 기록이 전부 이번 달인 계정
 * — 신규 서비스에서 가장 흔한 초기 상태다 — 에서 **하한과 상한이 같은 달로 겹쳐 양쪽 화살표가 동시에
 * 죽었다.** 카드는 보이는데 아무것도 움직이지 않으니 사용자가 화면을 **고장으로 읽었다.** 경계가
 * 옳게 계산돼도 사용자에게는 멈춘 화면 하나로만 보인다.
 *
 * 남은 경계는 상한뿐이고 그건 **데이터 사실**이다 — `COMPLETED` 의 `challengeDate` 는 구조상 오늘
 * 이하라 미래 달에는 카드가 존재할 수 없다. 그래서 이제 이 화면에서 **비활성이 뜻하는 것은 그 하나**다.
 *
 * [next] 가 경계에서 예외 대신 `this` 를 돌려주는 이유: 비활성 버튼을 눌러도 아무 일이 없어야 하고,
 * 던지면 호출부가 이동 전에 경계 검사를 한 번 더 하게 되어 같은 규칙의 사본이 두 곳에 생긴다.
 * 경계 판단이 갈라지는 순간 한쪽만 고친 버그가 나오므로 판단은 이 타입 안에만 둔다.
 *
 * [previous] 는 이론상 [YearMonth] 하한(약 -999,999,999년)에서 `DateTimeArithmeticException` 을
 * 던진다. 탭으로 거기까지 가려면 120억 번쯤 눌러야 하므로 **방어하지 않는다** — 관측된 버그도,
 * 도달 가능한 입력도 아니다.
 *
 * 기록이 없는 달도 건너뛰지 않고 그대로 지나간다. 이 타입은 '그 달에 기록이 있는지' 를 아예 모르며
 * 이는 의도된 설계다 — 데이터 유무로 이동 폭이 달라지면 한 번 누르면 한 달이 간다는 달력의 시간
 * 흐름과 어긋나고, 사용자는 자신이 몇 월을 건너뛰었는지 알 수 없게 된다. 그 성질 덕분에 이번 개정도
 * 하한을 지우는 것 하나로 끝났다 — 데이터를 참조하는 경로가 애초에 없었다.
 *
 * @property selected 지금 보고 있는 달.
 * @property latest 이동 상한이자 이번 달. 미래 달에는 종료된 챌린지가 존재할 수 없어 열어 둘 이유가 없다.
 */
@Immutable
data class ArchiveMonthCursor(
    val selected: YearMonth,
    val latest: YearMonth,
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

    /**
     * 이번 달인가. `ArchiveUiState.showsNoRecordsEver` 의 CTA 발동 조건에 쓰인다.
     *
     * [canGoNext] 의 부정과 지금은 같은 값이지만 `!canGoNext` 로 대신하지 않는다 — **묻는 질문이
     * 다르다.** 하나는 *"어디로 갈 수 있나"* 이고 하나는 *"지금 서 있는 달이 언제인가"* 다. 상한 규칙이
     * 바뀌는 순간(예: 다음 달을 미리 열어 두는 결정) 둘은 갈라지고, 그때 CTA 조건이 이동 경계를 따라
     * 조용히 딸려가면 안 된다.
     */
    val isLatest: Boolean get() = selected == latest

    val canGoNext: Boolean get() = selected < latest

    fun previous(): ArchiveMonthCursor = copy(selected = selected.minusMonth())

    fun next(): ArchiveMonthCursor =
        if (canGoNext) copy(selected = selected.plusMonth()) else this
}
