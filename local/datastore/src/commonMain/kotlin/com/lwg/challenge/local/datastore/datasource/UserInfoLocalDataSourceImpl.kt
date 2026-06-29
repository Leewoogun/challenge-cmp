package com.lwg.challenge.local.datastore.datasource

import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.local.datastore.createDataStore
import com.lwg.challenge.local.datastore.mapper.toDomain
import com.lwg.challenge.local.datastore.mapper.toPrefs
import com.lwg.challenge.local.datastore.model.UserInfoPrefs
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single(binds = [UserInfoLocalDataSource::class])
class UserInfoLocalDataSourceImpl : UserInfoLocalDataSource {
    private val dataStore = createDataStore(
        serializer = UserInfoPrefs.serializer(),
        defaultValue = UserInfoPrefs(),
        fileName = "user_info",
    )

    override suspend fun getUserInfo(): UserInfo? =
        dataStore.data.first().let { if (it.hasValue) it.toDomain() else null }

    override suspend fun saveUserInfo(userInfo: UserInfo) {
        dataStore.updateData { userInfo.toPrefs() }
    }

    override suspend fun clear() {
        dataStore.updateData { UserInfoPrefs() }
    }
}
