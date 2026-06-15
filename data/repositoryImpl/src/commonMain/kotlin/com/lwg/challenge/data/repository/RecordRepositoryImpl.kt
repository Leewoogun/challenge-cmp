package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.repository.RecordRepository
import com.lwg.challenge.remote.api.RecordApi
import com.lwg.challenge.remote.mapper.toUserRecord
import com.lwg.challenge.remote.network.util.ApiResult
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [RecordRepository::class])
class RecordRepositoryImpl(
    private val recordApi: RecordApi,
    private val authEventBus: AuthEventBus,
) : RecordRepository {

    override fun getMyRecord(onError: (String) -> Unit): Flow<UserRecord> = flow {
        val result = recordApi.getMyRecord()
        if (result is ApiResult.Failure.CustomError && result.code == CODE_UNAUTHORIZED) {
            authEventBus.emitSessionExpired()
            return@flow
        }
        result
            .suspendOnSuccess {
                this@flow.emit(response.toUserRecord())
            }
            .suspendOnFailureWithErrorHandling(onError)
    }

    private companion object {
        const val CODE_UNAUTHORIZED = 401
    }
}
