package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.UserInfoRemoteDataSource
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.remote.api.UserApi
import com.lwg.challenge.remote.mapper.toUserInfo
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [UserInfoRemoteDataSource::class])
class UserInfoRemoteDataSourceImpl(
    private val userApi: UserApi,
) : UserInfoRemoteDataSource {

    override fun getUserInfo(onError: (String) -> Unit): Flow<UserInfo> = flow {
        userApi.getMe()
            .suspendOnSuccess { this@flow.emit(response.toUserInfo()) }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
