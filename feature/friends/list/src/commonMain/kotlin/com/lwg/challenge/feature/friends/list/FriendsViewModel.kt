package com.lwg.challenge.feature.friends.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.feature.friends.list.kakao.KakaoInviter
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.domain.usecase.GetFriendsDataUseCase
import com.lwg.challenge.feature.friends.list.contract.FriendItemState
import com.lwg.challenge.feature.friends.list.contract.FriendRequestItemState
import com.lwg.challenge.feature.friends.list.contract.FriendsModalEffect
import com.lwg.challenge.feature.friends.list.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.list.contract.FriendsUiState
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class FriendsViewModel(
    private val getFriendsDataUseCase: GetFriendsDataUseCase,
    private val friendsRepository: FriendsRepository,
    private val kakaoInviter: KakaoInviter,
    private val userInfoRepository: UserInfoRepository,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val uiState: StateFlow<FriendsUiState> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getFriendsDataUseCase(onError = ::showMessage).map { data ->
                if (data.receivedRequests.isEmpty() && data.friends.isEmpty()) {
                    FriendsUiState.Empty
                } else {
                    val now = nowKst()
                    FriendsUiState.Data(
                        receivedRequests = data.receivedRequests.map { FriendRequestItemState(it, now) },
                        friends = data.friends.map { FriendItemState(it, now) },
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = FriendsUiState.Loading,
        )

    private val _modalEffect = MutableStateFlow<FriendsModalEffect>(FriendsModalEffect.Hidden)
    val modalEffect: StateFlow<FriendsModalEffect> = _modalEffect.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FriendsUiEffect>()
    val uiEffect: SharedFlow<FriendsUiEffect> = _uiEffect.asSharedFlow()

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

    fun inviteFriend() {
        viewModelScope.launch {
            val userInfo = userInfoRepository
                .getUserInfo(onError = {}, cacheStrategy = CacheStrategy.CACHE_FIRST)
                .firstOrNull()
            if (userInfo == null) {
                showMessage("사용자 정보를 불러오는 중이에요. 잠시 후 다시 시도해주세요.")
                return@launch
            }
            kakaoInviter.sendInvite(userInfo.nickname)
                .onFailure { showMessage("친구 초대를 보낼 수 없어요") }
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
