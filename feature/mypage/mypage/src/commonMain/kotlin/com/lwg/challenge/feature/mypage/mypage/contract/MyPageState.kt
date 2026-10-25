package com.lwg.challenge.feature.mypage.mypage.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.UserInfo
import kotlin.math.floor

@Stable
sealed interface MyPageUiState {

    @Immutable
    data object Loading : MyPageUiState

    /**
     * @property record `null` 이면 전적 조회 실패다. `UserRecord.Empty` 로 폴백하지 않는다 (§1.7.3).
     */
    @Immutable
    data class Data(
        val record: UserRecord?,
        val userInfo: UserInfo? = null,
        val isWithdrawing: Boolean = false,
    ) : MyPageUiState {

        val nickname: String get() = userInfo?.nickname.orEmpty()

        /** 전적 4개 값이 모두 0이면 강조 색을 죽인다. 조회 실패(null)는 행 자체가 안 나와 무관하다. */
        val isRecordMuted: Boolean get() = record?.isEmpty == true

        /**
         * 승률 분모에 무를 포함한다 — 랭킹 `lossRate` 의 분모가 `totalChallenges`(무 포함)라,
         * 분모가 갈리면 같은 사용자에게 두 화면이 서로를 반증하는 비율을 보여준다 (§1.2.5.1).
         *
         * 반올림도 서버 `LoserRanking.kt` 의 `Math.round` 와 방향을 맞춘다 — 정수 나눗셈은
         * 버림이라 같은 분모인데 결과가 1 씩 어긋난다 (§1.2.5.2).
         *
         * 단 `kotlin.math.round` 는 쓰지 않는다. ties-to-even 이라 `round(62.5) == 62.0` 인데,
         * 서버 `Math.round` 는 `floor(x + 0.5)` 로 정의돼 `63` 이다. 랭킹 패율과 반올림 방향을
         * 맞추려면 `floor(x + 0.5)` 형태여야 한다. (design.md §1.2.5.2 의 "Kotlin round() 는
         * .5 를 위로 보낸다" 는 서술은 사실과 다르다.)
         */
        val recordSummary: String
            get() {
                // 실패는 total == 0 과 같은 층위가 아니다. 0 을 그리면 "이 0 은 진짜 0" 이라는
                // 사실 주장이 되므로, 모르는 것은 그리지 않고 캡션으로 모른다고 말한다 (§1.7.3).
                val current = record ?: return "전적을 불러오지 못했어요"

                val total = current.win + current.lose + current.draw
                if (total == 0) return "아직 기록이 없어요"

                val rate = floor(current.win * 100.0 / total + 0.5).toInt()
                return "총 ${total}챌린지 · 승률 $rate%"
            }
    }
}
