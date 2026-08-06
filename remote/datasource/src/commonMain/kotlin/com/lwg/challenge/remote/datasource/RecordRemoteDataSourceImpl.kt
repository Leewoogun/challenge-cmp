package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.RecordRemoteDataSource
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.remote.api.RecordApi
import com.lwg.challenge.remote.mapper.toUserRecord
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [RecordRemoteDataSource::class])
class RecordRemoteDataSourceImpl(
    private val recordApi: RecordApi,
) : RecordRemoteDataSource {

    override fun getMyRecord(onError: (String) -> Unit): Flow<UserRecord> = flow {
        recordApi.getMyRecord()
            .suspendOnSuccess { this@flow.emit(response.toUserRecord()) }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
