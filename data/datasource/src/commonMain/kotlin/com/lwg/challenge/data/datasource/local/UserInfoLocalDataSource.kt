package com.lwg.challenge.data.datasource.local

import com.lwg.challenge.domain.model.user.UserInfo

interface UserInfoLocalDataSource {

    suspend fun getUserInfo(): UserInfo?

    suspend fun saveUserInfo(userInfo: UserInfo)

    suspend fun clear()
}
