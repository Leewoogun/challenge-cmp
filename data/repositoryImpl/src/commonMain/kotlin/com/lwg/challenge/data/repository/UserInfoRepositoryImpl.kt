package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.local.UserInfoLocalDataSource
import com.lwg.challenge.data.datasource.remote.UserInfoRemoteDataSource
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single

@Single(binds = [UserInfoRepository::class])
class UserInfoRepositoryImpl(
    private val userInfoRemoteDataSource: UserInfoRemoteDataSource,
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

    private fun fetchAndCache(onError: (String) -> Unit): Flow<UserInfo> =
        userInfoRemoteDataSource.getUserInfo(onError)
            .onEach { userInfoLocalDataSource.saveUserInfo(it) }
}
