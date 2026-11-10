package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.VerificationRemoteDataSource
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.remote.api.VerificationApi
import com.lwg.challenge.remote.mapper.toChallengeVerifications
import com.lwg.challenge.remote.mapper.toSubmitResult
import com.lwg.challenge.remote.network.util.ApiResult
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import com.lwg.challenge.utils.Constants
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [VerificationRemoteDataSource::class])
class VerificationRemoteDataSourceImpl(
    private val verificationApi: VerificationApi,
) : VerificationRemoteDataSource {

    /**
     * 실패 분류 규칙:
     * - `CustomError` → [VerificationSubmitOutcome.Rejected]. 서버가 code 로 판정해 거부한
     *   것이고 재시도해도 같다.
     * - `NetworkError`/`UnknownApiError`/`HttpError` → [VerificationSubmitOutcome.Unreachable].
     *   `HttpError`(예: 500)를 Rejected 로 넣지 않는다 — 서버가 커밋 이후에 실패했을 수 있어
     *   반영 여부를 알 수 없다. 모르는 것은 모른다고 분류해야 호출자가 실제 상태를 조회한다.
     */
    override suspend fun submitVerification(
        challengeId: Long,
        jpegBytes: ByteArray,
    ): VerificationSubmitOutcome {
        val content = MultiPartFormDataContent(
            formData {
                append(
                    key = "photo",
                    value = jpegBytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                        append(HttpHeaders.ContentDisposition, "filename=\"verification.jpg\"")
                    },
                )
            },
        )
        return when (val result = verificationApi.submitVerification(challengeId, content.contentType.toString(), content)) {
            is ApiResult.Success -> VerificationSubmitOutcome.Success(result.response.toSubmitResult())
            is ApiResult.Failure.CustomError -> VerificationSubmitOutcome.Rejected(result.message)
            is ApiResult.Failure.NetworkError ->
                VerificationSubmitOutcome.Unreachable("네트워크 오류가 발생하였습니다. 네트워크를 확인하여 주세요.")
            is ApiResult.Failure.HttpError -> VerificationSubmitOutcome.Unreachable(result.message)
            is ApiResult.Failure.UnknownApiError -> VerificationSubmitOutcome.Unreachable("요청을 처리하지 못했어요")
        }
    }

    override fun getVerifications(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeVerifications> = flow {
        verificationApi.getVerifications(challengeId)
            .suspendOnSuccess { this@flow.emit(response.toChallengeVerifications(Constants.BASE_URL)) }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
