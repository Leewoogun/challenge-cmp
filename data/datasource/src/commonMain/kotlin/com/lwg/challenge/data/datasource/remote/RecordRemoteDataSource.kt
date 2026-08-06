package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.UserRecord
import kotlinx.coroutines.flow.Flow

interface RecordRemoteDataSource {

    fun getMyRecord(onError: (String) -> Unit): Flow<UserRecord>
}
