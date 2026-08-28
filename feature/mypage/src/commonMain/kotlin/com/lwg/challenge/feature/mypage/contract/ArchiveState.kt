package com.lwg.challenge.feature.mypage.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome

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
 */
@Immutable
data class ArchiveUiState(
    val month: ArchiveMonthCursor?,
    val content: ArchiveContent,
) {

    /**
     * 월 이동 바의 건수 줄. [ArchiveContent.Data] 가 아니면 빈 문자열이다 — 아직 몇 건인지 모르는데
     * "0건" 으로 채우면 로딩 중에 그 달이 비었다고 말해 버리고, 카드가 도착하는 순간 방금 한 말이
     * 뒤집힌다.
     *
     * 값이 비어도 **바는 그 줄을 항상 그린다**(빈 문자열의 `Text`). 줄을 조건부로 감추면 바 높이가
     * 줄어 화살표가 위로 올라오는데, 빈 달을 건너뛰지 않기로 확정돼 화살표 연타가 정상 사용인
     * 화면에서 버튼이 움직이면 안 된다.
     */
    val countText: String
        get() = (content as? ArchiveContent.Data)?.let { "${it.challenges.size}건" } ?: ""
}

/**
 * 월 이동 바 아래 본문 한 가지.
 *
 * 상태가 4개인 이유는 [Blank] 하나 때문이다 — 나머지 셋(로딩·실패·데이터)은 1차와 같다.
 */
@Stable
sealed interface ArchiveContent {

    /**
     * 본문을 **비워만 둔다.** 월을 전환한 직후 약 300ms 가 이 상태다.
     *
     * [Loading] 과 다른 상태여야 하는 이유: 전환 즉시 스피너를 띄우면 빈 달을 연속으로 지날 때
     * `스피너 → 빈 달 → 스피너 → 빈 달` 이 반복되며 화면이 명멸한다. 대부분의 응답은 300ms 보다
     * 빨리 오므로, 그 사이를 비워 두면 스피너 없이 카드만 조용히 갈린다.
     *
     * 그 300ms 동안 *"이 달엔 없다"* (빈 달 화면)를 대신 그리면 **안 된다.** 아직 0건인지 모르는데
     * 없다고 말하면 직후 카드가 나타나며 방금 한 말을 뒤집는다. **비워 두는 것과 없다고 말하는 것은
     * 다르다** — 전자는 아직 말하지 않은 것이고 후자는 틀릴 수 있는 말이다.
     */
    @Immutable
    data object Blank : ArchiveContent

    /**
     * 스피너. 첫 진입은 [Blank] 를 거치지 않고 곧바로 이 상태로 들어간다 — 위아래가 통째로 빈 화면을
     * 300ms 보여 주는 것은 '조용한 전환' 이 아니라 그냥 앱이 멈춘 것으로 읽힌다. 월 전환에서는
     * 바뀌지 않는 바가 화면에 이미 남아 있어 그 문제가 없다.
     */
    @Immutable
    data object Loading : ArchiveContent

    /**
     * 그 달 조회 실패. 보관함은 바 아래가 목록 하나뿐이라 실패 영역도 본문 전체다 — 마이페이지가
     * 화면 Error 를 기각한 사유("성공한 데이터까지 못 쓰게 된다")가 여기서는 성립하지 않는다
     * (design.md §2.5.4.1).
     *
     * 월 이동이 생기면서 실패가 **특정 달에 귀속**된다. 바는 실패 상태에서도 살아 있고, 옆 달로
     * 넘어가는 것 자체가 복구 수단이다. 예외는 첫 로딩 실패뿐 — 그때는 [ArchiveUiState.month] 가
     * null 이라 바를 그릴 수 없어 "다시 시도" 만 남는다.
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
