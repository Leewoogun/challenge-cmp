package com.lwg.challenge.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.usecase.GetFriendsDataUseCase
import com.lwg.challenge.feature.friends.contract.FriendItemState
import com.lwg.challenge.feature.friends.contract.FriendRequestItemState
import com.lwg.challenge.feature.friends.contract.FriendsModalEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.koin.android.annotation.KoinViewModel

/**
 * 친구 탭 ViewModel.
 *
 * - `listReceivedRequests` + `listFriends` 두 Flow 를 [combine] 하여 단일 [uiState] 로 노출.
 * - 수락/거절은 낙관적 갱신 없이 성공 시 [reload] 로 목록을 다시 불러오고, 실패 시 메시지만 노출한다.
 * - 401(세션 만료)은 Ktor Authenticator 가 전담하므로 ViewModel 은 401 을 모른다.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@KoinViewModel
class FriendsViewModel(
    private val getFriendsDataUseCase: GetFriendsDataUseCase,
    private val friendsRepository: FriendsRepository,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val uiState: StateFlow<FriendsUiState> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getFriendsDataUseCase(onError = ::showMessage).map { data ->
                val now = Clock.System.now()
                FriendsUiState(
                    receivedRequests = data.receivedRequests.map { FriendRequestItemState(it, now) },
                    friends = data.friends.map { FriendItemState(it, now) },
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FriendsUiState(emptyList(), emptyList()),
        )

    private val _modalEffect = MutableStateFlow<FriendsModalEffect>(FriendsModalEffect.Hidden)
    val modalEffect: StateFlow<FriendsModalEffect> = _modalEffect.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FriendsUiEffect>()
    val uiEffect: SharedFlow<FriendsUiEffect> = _uiEffect

    fun acceptRequest(item: FriendRequestItemState) {
        viewModelScope.launch {
            friendsRepository.acceptRequest(
                requestId = item.requestId,
                onSuccess = ::reload,
                onError = ::showMessage,
            )
        }
    }

    fun rejectRequest(item: FriendRequestItemState) {
        viewModelScope.launch {
            friendsRepository.rejectRequest(
                requestId = item.requestId,
                onSuccess = ::reload,
                onError = ::showMessage,
            )
        }
    }

    fun dismiss() {
        _modalEffect.update { FriendsModalEffect.Hidden }
    }

    internal fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(FriendsUiEffect.ShowMessage(message))
        }
    }

    private fun reload() {
        refreshTrigger.tryEmit(Unit)
    }
}
