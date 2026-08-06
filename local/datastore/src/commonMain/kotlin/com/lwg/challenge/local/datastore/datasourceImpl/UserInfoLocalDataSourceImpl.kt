package com.lwg.challenge.local.datastore.datasourceImpl

import com.lwg.challenge.data.datasource.local.UserInfoLocalDataSource
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.local.datastore.datastore.UserInfoDataStore
import org.koin.core.annotation.Single

@Single(binds = [UserInfoLocalDataSource::class])
internal class UserInfoLocalDataSourceImpl(
    private val userInfoDataStore: UserInfoDataStore,
) : UserInfoLocalDataSource {

    override suspend fun getUserInfo(): UserInfo? = userInfoDataStore.getUserInfo()

    override suspend fun saveUserInfo(userInfo: UserInfo) {
        userInfoDataStore.saveUserInfo(userInfo)
    }

    override suspend fun clear() {
        userInfoDataStore.clear()
    }
}
