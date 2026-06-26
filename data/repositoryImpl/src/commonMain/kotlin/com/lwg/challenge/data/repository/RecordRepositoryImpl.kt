package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.repository.RecordRepository
import com.lwg.challenge.remote.api.RecordApi
import com.lwg.challenge.remote.mapper.toUserRecord
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [RecordRepository::class])
class RecordRepositoryImpl(
    private val recordApi: RecordApi,
) : RecordRepository {

    override fun getMyRecord(onError: (String) -> Unit): Flow<UserRecord> = flow {
        recordApi.getMyRecord()
            .suspendOnSuccess {
                this@flow.emit(response.toUserRecord())
            }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
