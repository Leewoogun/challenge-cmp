package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.RecordRemoteDataSource
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [RecordRepository::class])
class RecordRepositoryImpl(
    private val recordRemoteDataSource: RecordRemoteDataSource,
) : RecordRepository {

    override fun getMyRecord(onError: (String) -> Unit): Flow<UserRecord> =
        recordRemoteDataSource.getMyRecord(onError)
}
