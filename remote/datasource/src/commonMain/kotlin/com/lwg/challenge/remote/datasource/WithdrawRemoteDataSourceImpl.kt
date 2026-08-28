package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.WithdrawRemoteDataSource
import com.lwg.challenge.remote.api.UserApi
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import org.koin.core.annotation.Single

@Single(binds = [WithdrawRemoteDataSource::class])
class WithdrawRemoteDataSourceImpl(
    private val userApi: UserApi,
) : WithdrawRemoteDataSource {

    override suspend fun withdraw(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        userApi.withdraw()
            .suspendOnSuccess { onSuccess() }
            // 701 은 envelope 에러라 CustomError 로 오고, 서버 message 가 그대로 실린다.
            // 계약이 정한 문구를 앱이 다시 쓰지 않는다 — 원인별 문구를 서버가 쥔다.
            .suspendOnFailureWithErrorHandling(onError)
    }
}
