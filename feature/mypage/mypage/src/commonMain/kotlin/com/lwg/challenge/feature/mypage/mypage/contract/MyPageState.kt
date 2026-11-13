package com.lwg.challenge.feature.mypage.mypage.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.UserInfo
import kotlin.math.floor

@Stable
sealed interface MyPageUiState {

    @Immutable
    data object Loading : MyPageUiState

    /** @property record `null` 이면 전적 조회 실패다. `UserRecord.Empty` 로 폴백하지 않는다. */
    @Immutable
    data class Data(
        val record: UserRecord?,
        val userInfo: UserInfo? = null,
        val isWithdrawing: Boolean = false,
        val themeMode: ThemeMode = ThemeMode.DARK,
    ) : MyPageUiState {

        val isDarkThemeSelected: Boolean get() = themeMode == ThemeMode.DARK
        val isLightThemeSelected: Boolean get() = themeMode == ThemeMode.LIGHT

        val isRecordMuted: Boolean get() = record?.isEmpty == true

        /**
         * 랭킹 패율과 숫자가 갈리지 않게 분모에 무를 넣고 서버 `Math.round` 처럼 `floor(x + 0.5)` 로 반올림한다.
         * `kotlin.math.round` 는 ties-to-even(62.5 → 62)이라 쓰지 않는다.
         */
        val recordSummary: String
            get() {
                // 모르는 값을 0 으로 그리면 사실 주장이 되므로 실패는 캡션으로만 말한다.
                val current = record ?: return "전적을 불러오지 못했어요"

                val total = current.win + current.lose + current.draw
                if (total == 0) return "아직 기록이 없어요"

                val rate = floor(current.win * 100.0 / total + 0.5).toInt()
                return "총 ${total}챌린지 · 승률 $rate%"
            }
    }
}
