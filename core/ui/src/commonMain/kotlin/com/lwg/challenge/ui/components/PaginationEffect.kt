package com.lwg.challenge.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

/**
 * 스크롤이 멈춘 뒤 끝에 닿아 있으면 [onLoadNextPage] 를 부른다. 더 받을지는 호출받는 쪽이 판단한다.
 * 첫 값을 버려, 재조성 직후 `canScrollForward` 초기값만으로 다음 쪽을 부르지 않는다.
 */
@Composable
fun PaginationEffect(
    listState: LazyListState,
    isRefreshing: Boolean = false,
    onLoadNextPage: () -> Unit,
) {
    val currentIsRefreshing = rememberUpdatedState(isRefreshing)
    val currentOnLoadNextPage = rememberUpdatedState(onLoadNextPage)

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .drop(1)
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                if (listState.canScrollBackward && !listState.canScrollForward && !currentIsRefreshing.value) {
                    currentOnLoadNextPage.value()
                }
            }
    }
}
