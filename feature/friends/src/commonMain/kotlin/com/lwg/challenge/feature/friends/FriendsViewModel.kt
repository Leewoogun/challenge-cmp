package com.lwg.challenge.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.FriendsApiException
import com.lwg.challenge.domain.model.friend.FriendsNetworkException
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.feature.friends.contract.FriendItemState
import com.lwg.challenge.feature.friends.contract.FriendRequestItemState
import com.lwg.challenge.feature.friends.contract.FriendsModalEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.koin.android.annotation.KoinViewModel

/**
 * 친구 탭 ViewModel.
 *
 * - init 시 `listReceivedRequests` + `listFriends` 두 Flow 를 [combine] 하여 [FriendsUiState.Data] 갱신.
 * - `acceptRequest(id)` / `rejectRequest(id)` — 낙관적 갱신 후 repository 호출, 실패 시 롤백 + 스낵바.
 * - 401(세션 만료)은 Ktor Auth(refreshTokens)가 전담하고 Repository 가 흡수하므로 ViewModel 은 401 을 모른다.
 */
@KoinViewModel
class FriendsViewModel(
    private val friendsRepository: FriendsRepository,
) : ViewModel() {

    private val _receivedRequests: MutableStateFlow<List<FriendRequest>> = MutableStateFlow(emptyList())
    private val _friends: MutableStateFlow<List<Friend>> = MutableStateFlow(emptyList())

    private val _uiState: MutableStateFlow<FriendsUiState> = MutableStateFlow(FriendsUiState.Loading)
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _modalEffect = MutableStateFlow<FriendsModalEffect>(FriendsModalEffect.Hidden)
    val modalEffect: StateFlow<FriendsModalEffect> = _modalEffect.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FriendsUiEffect>()
    val uiEffect: SharedFlow<FriendsUiEffect> = _uiEffect

    init {
        observeReceivedRequests()
        observeFriends()
        observeCombined()
    }

    private fun observeReceivedRequests() {
        viewModelScope.launch {
            friendsRepository
                .listReceivedRequests(onError = ::handleLoadError)
                .collect { list -> _receivedRequests.update { list } }
        }
    }

    private fun observeFriends() {
        viewModelScope.launch {
            friendsRepository
                .listFriends(onError = ::handleLoadError)
                .collect { list -> _friends.update { list } }
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun observeCombined() {
        viewModelScope.launch {
            combine(_receivedRequests, _friends) { requests, friends ->
                // emit 시점에 1회만 측정 → ItemState 동일성 유지 + 테스트 가능
                val now = Clock.System.now()
                FriendsUiState.Data(
                    receivedRequests = requests.map { FriendRequestItemState(it, now) },
                    friends = friends.map { FriendItemState(it, now) },
                )
            }.collect { data -> _uiState.update { data } }
        }
    }

    /**
     * 받은 요청 수락 — 낙관적 갱신.
     *
     * 1) 받은 요청에서 제거 + 친구 목록에 (최소 정보로) 추가
     * 2) repository.acceptRequest 호출
     * 3) 실패 시 **1행 단위 역연산** 으로 롤백 + ShowMessage
     *
     * 전체 리스트 스냅샷을 통째 복원하면 동시에 진행 중인 다른 mutation 의
     * 낙관적 갱신까지 덮어쓰는 race 가 발생하므로 반드시 1행 단위로 되돌린다.
     */
    fun acceptRequest(item: FriendRequestItemState) {
        val current = _receivedRequests.value
        val target = current.firstOrNull { it.requestId == item.requestId } ?: return
        val index = current.indexOf(target)
        val optimisticFriend = target.toOptimisticFriend()

        _receivedRequests.update { list -> list.filterNot { it.requestId == target.requestId } }
        _friends.update { list -> list + optimisticFriend }

        viewModelScope.launch {
            val result = friendsRepository.acceptRequest(item.requestId)
            result.onFailure { throwable ->
                // 1행 단위 역연산 — 다른 mutation 의 낙관 갱신을 보존
                _receivedRequests.update { list ->
                    if (list.any { it.requestId == target.requestId }) {
                        list
                    } else {
                        val mutable = list.toMutableList()
                        mutable.add(index.coerceIn(0, mutable.size), target)
                        mutable
                    }
                }
                _friends.update { list -> list.filterNot { it.userId == optimisticFriend.userId } }
                emitError(throwable)
            }
        }
    }

    /**
     * 받은 요청 거절 — 낙관적 갱신. 실패 시 1행 단위 역연산.
     */
    fun rejectRequest(item: FriendRequestItemState) {
        val current = _receivedRequests.value
        val target = current.firstOrNull { it.requestId == item.requestId } ?: return
        val index = current.indexOf(target)

        _receivedRequests.update { list -> list.filterNot { it.requestId == target.requestId } }

        viewModelScope.launch {
            val result = friendsRepository.rejectRequest(item.requestId)
            result.onFailure { throwable ->
                _receivedRequests.update { list ->
                    if (list.any { it.requestId == target.requestId }) {
                        list
                    } else {
                        val mutable = list.toMutableList()
                        mutable.add(index.coerceIn(0, mutable.size), target)
                        mutable
                    }
                }
                emitError(throwable)
            }
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

    private fun handleLoadError(throwable: Throwable) {
        emitError(throwable)
    }

    private fun emitError(throwable: Throwable) {
        val message = when (throwable) {
            is FriendsApiException -> throwable.message ?: "알 수 없는 오류"
            is FriendsNetworkException -> "네트워크 오류가 발생하였습니다."
            else -> throwable.message ?: "알 수 없는 오류"
        }
        showMessage(message)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun FriendRequest.toOptimisticFriend(): Friend = Friend(
        userId = fromUser.userId,
        nickname = fromUser.nickname,
        profileImageUrl = fromUser.profileImageUrl,
        since = requestedAt,
    )
}
