package com.lwg.challenge.feature.friends.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.domain.usecase.GetFriendsDataUseCase
import com.lwg.challenge.feature.friends.list.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.list.contract.FriendsUiState
import com.lwg.challenge.feature.friends.list.kakao.KakaoInviter
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

private const val USER_INFO_NOT_READY_MESSAGE = "사용자 정보를 불러오는 중이에요. 잠시 후 다시 시도해주세요."
private const val INVITE_FAILED_MESSAGE = "친구 초대를 보낼 수 없어요"

@KoinViewModel
class FriendsViewModel(
    getFriendsDataUseCase: GetFriendsDataUseCase,
    private val friendsRepository: FriendsRepository,
    private val kakaoInviter: KakaoInviter,
    private val userInfoRepository: UserInfoRepository,
) : ViewModel() {

    val uiState: TriggerStateFlow<FriendsUiState> = getFriendsDataUseCase(onError = ::showMessage)
        .map { data ->
            if (data.receivedRequests.isEmpty() && data.friends.isEmpty()) {
                FriendsUiState.Empty
            } else {
                FriendsUiState.Data(
                    receivedRequests = data.receivedRequests,
                    friends = data.friends,
                )
            }
        }
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = FriendsUiState.Loading,
        )

    private val _uiEffect = MutableSharedFlow<FriendsUiEffect>()
    val uiEffect: SharedFlow<FriendsUiEffect> = _uiEffect.asSharedFlow()

    fun acceptRequest(item: FriendRequest) {
        viewModelScope.launch {
            friendsRepository.acceptRequest(
                requestId = item.requestId,
                onSuccess = uiState::restart,
                onError = ::showMessage,
            )
        }
    }

    fun rejectRequest(item: FriendRequest) {
        viewModelScope.launch {
            friendsRepository.rejectRequest(
                requestId = item.requestId,
                onSuccess = uiState::restart,
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
                showMessage(USER_INFO_NOT_READY_MESSAGE)
                return@launch
            }
            kakaoInviter.sendInvite(userInfo.nickname)
                .onFailure { showMessage(INVITE_FAILED_MESSAGE) }
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(FriendsUiEffect.ShowMessage(message))
        }
    }
}
