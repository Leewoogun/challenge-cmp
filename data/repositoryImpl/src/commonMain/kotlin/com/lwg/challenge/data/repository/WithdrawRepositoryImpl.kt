package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.WithdrawRemoteDataSource
import com.lwg.challenge.domain.repository.WithdrawRepository
import org.koin.core.annotation.Single

@Single(binds = [WithdrawRepository::class])
class WithdrawRepositoryImpl(
    private val withdrawRemoteDataSource: WithdrawRemoteDataSource,
) : WithdrawRepository {

    // 로컬 정리를 여기서 하지 않는다 — 순서 책임은 WithdrawUseCase 에 있다 (LoginRepositoryImpl.logout 과 동일).
    override suspend fun withdraw(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        withdrawRemoteDataSource.withdraw(onSuccess, onError)
    }
}
