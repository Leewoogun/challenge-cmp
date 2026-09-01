package com.lwg.challenge.feature.notification.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 이 화면에는 모달이 없어 `ModalEffect` 를 두지 않는다.
 *
 * 첫 조회 실패는 화면 전체를 [NotificationUiState.Failure] 로 바꾸므로 이 채널을 쓰지 않는다.
 * 여기로 오는 것은 목록을 남긴 채 알려야 하는 부분 실패다.
 */
@Stable
sealed interface NotificationUiEffect {

    @Immutable
    data class ShowMessage(val message: String) : NotificationUiEffect
}
