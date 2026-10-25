package com.lwg.challenge.feature.mypage.archive.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

/**
 * 계약서 보관함 화면 상태.
 *
 * 1차의 `sealed interface` 를 접고 **월 이동 바와 본문을 한 값에 묶은** 이유: 둘은 언제나 짝으로만
 * 바뀌어야 한다. 상태가 갈라져 있으면 *"8월 바 + 7월 카드"* 같은 조합이 타입 위에서 표현 가능해지고,
 * 표현 가능한 조합은 언젠가 화면에 나온다. 여기서 한 값으로 묶으면 그 찢어진 중간 상태가
 * 타이밍 방어가 아니라 **구조적으로** 불가능해진다.
 *
 * @property month 지금 보고 있는 달과 그 달에서 이동 가능한 범위. `null` 은 *"달이 없다"* 가 아니라
 *   **아직 서버가 이번 달을 알려주지 않았다** 는 뜻이다 — 첫 로딩과 첫 로딩 실패, 이 두 순간뿐이다.
 *   그때는 월 이동 바를 **그리지 않는다.** 라벨도 경계도 참인 값이 하나도 없어 그릴 것이 없고, 없는
 *   정보를 위해 빈 자리를 미리 잡아 두는 것보다 정보가 생길 때 자리가 함께 생기는 쪽이 거짓말을
 *   덜 한다. 첫 응답 이후로는 로딩이든 실패든 항상 non-null 이라, 한번 생긴 바가 도중에 사라지지 않는다.
 * @property content 바 아래 본문. 바와 본문이 각각 무엇을 말하는지는 [ArchiveContent] 참조.
 * @property firstArchivedMonth 이 계정에서 **가장 오래된 종료 기록이 있는 달**. 종료한 챌린지가 하나도
 *   없으면 null 이다. 요청한 달과 무관한 **사용자 전역 값**이라 매 응답 같은 값이 오고, 달을 넘나드는
 *   동안 그대로 유지된다.
 *
 *   **이 값으로 `<` 를 막지 마라.** 2차 개정에서 과거 하한을 폐기했다 — 하한이 '최초 기록월' 이던 시절
 *   기록이 전부 이번 달인 계정에서 하한과 상한이 겹쳐 양쪽 화살표가 동시에 죽었고, 사용자가 화면을
 *   고장으로 읽었다. 이 값은 **고지(문구)용이지 차단용이 아니다.** 월 값이 손에 쥐여 있으니 다시
 *   하한으로 쓰고 싶어지는데, 그게 방금 폐기한 규칙이다.
 *
 *   직전 패스에서 정확히 그 오용을 막으려 이 값을 `Boolean`(`hasArchivedRecords`)으로 좁혔다가
 *   **되돌렸다.** [emptyMonthSubtitle] 의 B형 부제가 *"첫 기록은 2026년 5월이에요"* 처럼 **월 값을
 *   문장으로 출력**해야 해서 `Boolean` 으로는 표현이 불가능하다. 오용 방지를 포기한 것이 아니라
 *   **수단을 계산 프로퍼티로 이관**했다 — 화면은 [showsNoRecordsEver] 와 [emptyMonthSubtitle] 만 읽고
 *   이 값을 직접 비교하지 않는다. 새 비교가 필요해지면 그 판단도 여기 프로퍼티로 들어와야 한다.
 *
 *   원래 `ArchiveMonthCursor.earliest` 가 겸하던 값이다. 하한이 사라지면서 그 필드가 커서의 답
 *   — *"어디로 갈 수 있나"* — 에 전혀 기여하지 않게 됐으므로 커서에서 뺐다. 거기 남겨 뒀다면
 *   **한 필드가 무관한 두 일을 하는 상태**가 되는데, 그건 이 feature 가 방금 계약에서 걷어낸 결함과
 *   똑같은 것이다.
 * @property isTransitioning 다른 달의 응답을 **기다리는 중**인가. 이 값이 true 여도 [month] 와
 *   [content] 는 **전환 전 달 그대로**다 — 바뀌는 것은 [countText] 하나뿐이라, 이 플래그는 화면을
 *   갈아 끼우지 않고 "아직 오는 중" 이라는 사실만 얹는다. 본문 전체가 로딩으로 갈리는
 *   [ArchiveContent.Loading] 과는 층이 다르다.
 *
 *   첫 로딩에는 붙지 않는다. 유지할 이전 달이 애초에 없어 그때는 본문이 통째로
 *   [ArchiveContent.Loading] 이다.
 */
@Immutable
data class ArchiveUiState(
    val month: ArchiveMonthCursor?,
    val content: ArchiveContent,
    val firstArchivedMonth: YearMonth?,
    val isTransitioning: Boolean = false,
) {

    /**
     * 빈 목록일 때 CTA 있는 *"기록 전무"* 화면을 쓸지. 아니면 빈 달 화면이다.
     *
     * 기록이 0건이어도 **이번 달일 때만** true 다. *"챌린지 만들기"* 는 **지금 행동할 수 있는 자리**
     * 에서만 맞는 말이고, 5월을 들여다보고 있는 사용자는 회고 중이지 챌린지를 만들러 온 것이 아니다.
     * 첫 진입이 항상 이번 달이라 이렇게 좁혀도 **노출 기회는 잃지 않는다.**
     */
    val showsNoRecordsEver: Boolean
        get() = firstArchivedMonth == null && month?.isLatest == true

    /**
     * 빈 달 화면의 부제. A형/B형을 가른다 — 두 문구의 뜻과 B형이 필요한 이유는
     * `ArchiveEmptyMonth` 의 KDoc 에 있다.
     *
     * 화면이 [firstArchivedMonth] 와 [month] 를 직접 비교하지 않도록 그 비교를 여기로 끌어왔다.
     * 이 비교는 **문구를 고르기 위한 것이지 이동을 막기 위한 것이 아니다.**
     */
    val emptyMonthSubtitle: String
        get() = if (firstArchivedMonth != null && month != null && month.selected < firstArchivedMonth) {
            "첫 기록은 ${firstArchivedMonth.year}년 ${firstArchivedMonth.month.number}월이에요"
        } else {
            "화살표로 다른 달을 볼 수 있어요"
        }

    /**
     * 월 이동 바의 건수 줄. 아직 몇 건인지 모르는 구간을 "0건" 으로 채우지 않는다 — 그러면 로딩 중에
     * 그 달이 비었다고 말해 버리고, 카드가 도착하는 순간 방금 한 말이 뒤집힌다.
     *
     * 값이 비어도 **바는 그 줄을 항상 그린다**(빈 문자열의 `Text`). 줄을 조건부로 감추면 바 높이가
     * 줄어 화살표가 위로 올라오는데, 빈 달을 건너뛰지 않기로 확정돼 화살표 연타가 정상 사용인
     * 화면에서 버튼이 움직이면 안 된다.
     *
     * **월 전환의 로딩 힌트가 바로 그 슬롯을 쓴다.** 바는 *"모르는 구간에도 슬롯을 유지한다"* 는 위
     * 규칙으로 이 자리를 이미 예약해 뒀다 — 값이 없을 때 침묵하며 높이만 붙들던 그 공백이, 힌트를
     * 얹을 자리를 그대로 내준다. **새 자리를 만들지 않으므로 기하가 하나도 변하지 않는다.**
     *
     * 문구가 `"불러오는 중"` 인 것도 같은 이유다 — **건수를 주장하지 않는다.** 그래서 이전 달 라벨과
     * 나란히 있어도 어긋나지 않는다. 반대로 힌트 대신 옛 *건수 숫자*를 그대로 끌고 가면, 라벨이 새
     * 달로 바뀌는 순간 `(7월 라벨 + 8월 건수)` 가 되어 그것이 곧 금지된 혼합이다.
     */
    val countText: String
        get() = when {
            isTransitioning -> "불러오는 중"
            content is ArchiveContent.Data -> "${content.challenges.size}건"
            else -> ""
        }
}

/**
 * 월 이동 바 아래 본문 한 가지.
 *
 * 월 전환이 이전 달을 통째로 유지하게 된 뒤로 **본문이 갈리는 순간은 첫 로딩과 응답 도착뿐**이다.
 * 그래서 실제로 화면에 나오는 것은 [Loading]([Failure] 포함)과 [Data] 이고, [Blank] 에는 도달
 * 경로가 없다 — 사유는 각 상태의 KDoc 에 있다.
 */
@Stable
sealed interface ArchiveContent {

    /**
     * 본문을 **비워만 둔다.**
     *
     * ⚠️ **지금은 도달하는 경로가 없다.** 월 전환 직후를 이 상태로 만들던 판을 접었기 때문이다.
     * 전환 중에 본문을 비우면 응답이 늦는 만큼 그 빈 화면이 그대로 보이는데, 그게 바로 고치려던
     * 깜빡임이었다. 지금은 응답이 올 때까지 **이전 달을 온전히 유지**하고 로딩 힌트는 건수 슬롯이
     * 진다 ([ArchiveUiState.isTransitioning]). 첫 로딩은 원래부터 [Loading] 로 직행한다.
     *
     * 타입을 지우지 않고 남기는 이유: *"비워 두는 것과 없다고 말하는 것은 다르다"* 는 구분 자체는
     * 여전히 옳다. 아직 0건인지 모르는데 *"이 달엔 없다"* 를 그리면 직후 카드가 나타나며 방금 한
     * 말을 뒤집는다 — 전자는 아직 말하지 않은 것이고 후자는 틀릴 수 있는 말이다. 그 구분이 필요한
     * 구간이 다시 생기면 여기서 다시 시작한다.
     */
    @Immutable
    data object Blank : ArchiveContent

    /**
     * 스피너. **첫 진입 전용**이다 — 감출 이전 달이 없는 유일한 순간이라, 여기서만 본문 전체가
     * 로딩을 말한다.
     *
     * 월 전환에서는 쓰지 않는다. 전환 즉시 스피너를 띄우면 빈 달을 연속으로 지날 때
     * `스피너 → 빈 달 → 스피너 → 빈 달` 이 반복되며 화면이 명멸하고, 늦춰 띄워도 그때는 이미
     * 화면에 이전 달이 온전히 남아 있어 덮을 이유가 없다.
     */
    @Immutable
    data object Loading : ArchiveContent

    /**
     * 조회 실패. **첫 로딩 실패 전용**이다.
     *
     * 실패가 본문 전체를 덮는 근거는 마이페이지가 화면 Error 를 기각한 사유("성공한 데이터까지 못
     * 쓰게 된다")가 **여기서는 성립하지 않는다** 는 것이었다 (design.md §2.5.4.1) — 화면에 잃을
     * 성공 데이터가 애초에 없으니 통째로 덮어도 잃는 게 없다.
     *
     * 그 전제가 **월 전환에서만 뒤집힌다.** 전환 중 화면에는 이전 달의 성공한 목록이 그대로 있어,
     * 그것을 실패 카드로 덮으면 정확히 기각된 손실이 일어난다. 그래서 전환 실패는 이 상태로 오지
     * 않고 **이전 달을 유지한 채 스낵바**로만 알린다. 남는 도달 경로는 첫 로딩뿐이고, 그때는
     * [ArchiveUiState.month] 가 null 이라 월 이동 바도 없어 "다시 시도" 가 유일한 복구 수단이다.
     *
     * 서버 message 를 화면에 쓰지 않아 `data object` 로 충분하다.
     */
    @Immutable
    data object Failure : ArchiveContent

    /**
     * 그 달 조회 성공. 빈 목록도 실패가 아니라 **성공한 0건**이다.
     *
     * 1차의 `ArchiveMonthState` 와 `toArchiveMonths()` 를 지우고 평평한 목록으로 되돌린 이유:
     * - 화면에 달이 하나뿐이라 월 그룹핑이 무의미하다.
     * - 무엇보다 1차는 월 라벨을 `histories.first()` 에서 뽑았는데 **빈 달에는 뽑아낼 데이터가 없다.**
     *   라벨의 출처가 데이터에서 **선택된 달**로 옮겨갔다 ([ArchiveMonthCursor.label]).
     *
     * 함께 있던 방어적 재정렬도 지웠다. 1차의 사유가 *"어긋나면 같은 월 헤더가 두 번 나오는 형태로
     * 깨진다"* 였는데 **헤더가 하나뿐이라 그 파손이 불가능**해졌다. 남는 것은 카드 순서뿐이고 그건
     * 서버 책임이다 — 계약이 `challengeDate DESC` + `id DESC` 로 고정돼 있다.
     */
    @Immutable
    data class Data(
        val challenges: List<ArchiveChallengeItemState>,
    ) : ArchiveContent {

        val isEmpty: Boolean get() = challenges.isEmpty()
    }
}

@Immutable
data class ArchiveChallengeItemState(private val model: ChallengeHistory) {

    val challengeId: Long get() = model.challengeId
    val opponentNickname: String get() = model.opponentNickname
    val myMission: String get() = model.myMission
    val opponentMission: String get() = model.opponentMission
    val opponentMissionLabel: String get() = "${model.opponentNickname}의 미션"
    val betText: String get() = "내기: ${model.bet}"
    val outcome: ChallengeOutcome get() = model.myResult

    /** 월 헤더가 연·월을 이미 말하지만, 카드만 떼어 봐도 읽히도록 월을 반복한다. */
    val dateText: String get() = "${model.challengeDate.monthNumber}월 ${model.challengeDate.day}일"
}
