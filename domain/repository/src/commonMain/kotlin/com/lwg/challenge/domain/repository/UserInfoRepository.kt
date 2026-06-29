package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface UserInfoRepository {
    fun getUserInfo(
        onError: (String) -> Unit,
        cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST,
    ): Flow<UserInfo>

    suspend fun clearUserInfoCache()
}
