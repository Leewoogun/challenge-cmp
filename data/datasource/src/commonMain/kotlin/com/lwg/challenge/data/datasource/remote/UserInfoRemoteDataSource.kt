package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface UserInfoRemoteDataSource {

    fun getUserInfo(onError: (String) -> Unit): Flow<UserInfo>
}
