package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.local.datastore.datasource.UserInfoLocalDataSource
import com.lwg.challenge.remote.api.UserApi
import com.lwg.challenge.remote.mapper.toUserInfo
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [UserInfoRepository::class])
class UserInfoRepositoryImpl(
    private val userApi: UserApi,
    private val userInfoLocalDataSource: UserInfoLocalDataSource,
) : UserInfoRepository {

    override fun getUserInfo(
        onError: (String) -> Unit,
        cacheStrategy: CacheStrategy,
    ): Flow<UserInfo> = when (cacheStrategy) {
        CacheStrategy.CACHE_FIRST -> flow {
            val cached = userInfoLocalDataSource.getUserInfo()
            if (cached != null) {
                emit(cached)
            } else {
                fetchAndCache(onError).collect { emit(it) }
            }
        }
        CacheStrategy.NETWORK_ONLY -> fetchAndCache(onError)
    }

    override suspend fun clearUserInfoCache() {
        userInfoLocalDataSource.clear()
    }

    private fun fetchAndCache(onError: (String) -> Unit): Flow<UserInfo> = flow {
        userApi.getMe()
            .suspendOnSuccess {
                val userInfo = response.toUserInfo()
                userInfoLocalDataSource.saveUserInfo(userInfo)
                emit(userInfo)
            }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
