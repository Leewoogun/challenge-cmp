package com.lwg.challenge.remote.datasource

import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.remote.api.VerificationApi
import com.lwg.challenge.remote.api.createVerificationApi
import com.lwg.challenge.remote.model.verification.ChallengeVerificationsData
import com.lwg.challenge.remote.model.verification.ChallengeVerificationsResponse
import com.lwg.challenge.remote.model.verification.PartyVerificationDto
import com.lwg.challenge.remote.model.verification.SubmitVerificationData
import com.lwg.challenge.remote.model.verification.SubmitVerificationResponse
import com.lwg.challenge.remote.network.util.ApiResult
import com.lwg.challenge.remote.network.util.ApiResultConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.appendIfNameAbsent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VerificationRemoteDataSourceImplTest {

    private val jpegBytes = byteArrayOf(-1, -40, -1, -32, 0, 16)

    private val submitData = SubmitVerificationData(
        challengeId = 7L,
        status = "VERIFIED",
        verifiedAt = LocalDateTime(2026, 8, 14, 15, 32, 10),
    )

    private val verificationsData = ChallengeVerificationsData(
        challengeId = 7L,
        challenger = PartyVerificationDto(
            userId = 1L,
            status = "VERIFIED",
            photoUrl = "/api/v1/challenges/7/photos/challenger",
            verifiedAt = LocalDateTime(2026, 8, 14, 15, 32, 10),
        ),
        opponent = PartyVerificationDto(userId = 14L, status = "PENDING"),
    )

    private fun dataSource(api: FakeVerificationApi = FakeVerificationApi()) =
        VerificationRemoteDataSourceImpl(api)

    @Test
    fun `submitVerification 성공 - Success 로 매핑되고 photo part 가 multipart 로 나간다`() = runTest {
        val api = FakeVerificationApi(
            submitResult = ApiResult.Success(SubmitVerificationResponse(submitData)),
        )

        val outcome = dataSource(api).submitVerification(7L, jpegBytes)

        assertEquals(7L, api.capturedSubmitId)
        assertIs<MultiPartFormDataContent>(api.capturedSubmitBody)
        val contentType = api.capturedSubmitContentType.orEmpty()
        assertTrue(contentType.startsWith("multipart/form-data"), "contentType=$contentType")
        assertTrue(contentType.contains("boundary="), "boundary 가 없으면 서버가 파싱하지 못한다")
        val result = assertIs<VerificationSubmitOutcome.Success>(outcome).result
        assertEquals(7L, result.challengeId)
        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertEquals(LocalDateTime(2026, 8, 14, 15, 32, 10), result.verifiedAt)
    }

    @Test
    fun `submitVerification CustomError 700 - 서버 판정 거부라 Rejected`() = runTest {
        val api = FakeVerificationApi(
            submitResult = ApiResult.Failure.CustomError(code = 700, message = "JPEG 사진만 올릴 수 있어요"),
        )

        val outcome = dataSource(api).submitVerification(7L, jpegBytes)

        assertEquals(VerificationSubmitOutcome.Rejected("JPEG 사진만 올릴 수 있어요"), outcome)
    }

    @Test
    fun `submitVerification NetworkError - 서버 반영 여부를 모르므로 Unreachable`() = runTest {
        val api = FakeVerificationApi(
            submitResult = ApiResult.Failure.NetworkError(message = "connection reset"),
        )

        val outcome = dataSource(api).submitVerification(7L, jpegBytes)

        assertEquals(
            VerificationSubmitOutcome.Unreachable("네트워크 오류가 발생하였습니다. 네트워크를 확인하여 주세요."),
            outcome,
        )
    }

    @Test
    fun `submitVerification HttpError 500 - Rejected 가 아니라 Unreachable`() = runTest {
        val api = FakeVerificationApi(
            submitResult = ApiResult.Failure.HttpError(code = 500, message = "Internal Server Error", body = ""),
        )

        val outcome = dataSource(api).submitVerification(7L, jpegBytes)

        // 커밋 이후에 실패했을 수 있어 반영 여부를 모른다 — Rejected 로 분류하면 상태 확인 경로가 막힌다
        assertTrue(outcome !is VerificationSubmitOutcome.Rejected)
        assertIs<VerificationSubmitOutcome.Unreachable>(outcome)
    }

    @Test
    fun `submitVerification UnknownApiError - Unreachable`() = runTest {
        val api = FakeVerificationApi(
            submitResult = ApiResult.Failure.UnknownApiError(IllegalStateException("boom")),
        )

        val outcome = dataSource(api).submitVerification(7L, jpegBytes)

        assertIs<VerificationSubmitOutcome.Unreachable>(outcome)
    }

    @Test
    fun `submitVerification 실요청 - DefaultRequest 의 JSON 기본값이 multipart 를 덮지 않는다`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val submitWire =
            """{"error":false,"code":200,"message":"","data":{"challengeId":7,"status":"VERIFIED","verifiedAt":"2026-08-14 15:32:10"}}"""
        val client = HttpClient(
            MockEngine { request ->
                requests.add(request)
                respond(
                    content = submitWire,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        ) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                        prettyPrint = true
                    },
                )
            }
            // 운영(KtorfitModule.provideHttpClient)과 같은 DefaultRequest 를 건 채로 검증한다
            install(DefaultRequest) {
                headers.appendIfNameAbsent(HttpHeaders.ContentType, "application/json; charset=utf-8")
            }
        }
        val api = Ktorfit.Builder()
            .httpClient(client)
            .baseUrl("http://localhost:8080/")
            .converterFactories(ApiResultConverterFactory())
            .build()
            .createVerificationApi()

        val outcome = VerificationRemoteDataSourceImpl(api).submitVerification(7L, jpegBytes)

        assertIs<VerificationSubmitOutcome.Success>(outcome)
        val request = requests.single()
        val body = assertIs<MultiPartFormDataContent>(request.body)
        val effectiveContentType = body.contentType.toString()
        assertTrue(effectiveContentType.startsWith("multipart/form-data"), "contentType=$effectiveContentType")
        assertTrue(effectiveContentType.contains("boundary="), "boundary 가 없으면 서버가 파싱하지 못한다")
        // 엔진은 body 의 contentType 하나만 wire 에 싣는다 — 요청 헤더에 JSON 기본값이 남아 있으면
        // Content-Type 이 두 값이 되거나 어느 쪽이 쓰일지가 순서에 의존하게 된다
        assertNull(
            request.headers.getAll(HttpHeaders.ContentType)?.firstOrNull { it.startsWith("application/json") },
            "DefaultRequest 의 JSON 기본값이 multipart 요청 헤더에 끼어들면 안 된다",
        )
        assertTrue(
            request.headers.getAll(HttpHeaders.ContentType).orEmpty().size <= 1,
            "Content-Type 값은 1개를 넘을 수 없다",
        )
    }

    @Test
    fun `getVerifications 성공 - 양측 현황이 도메인으로 매핑돼 onSuccess 로 전달`() = runTest {
        val api = FakeVerificationApi(
            verificationsResult = ApiResult.Success(ChallengeVerificationsResponse(verificationsData)),
        )

        val results = mutableListOf<ChallengeVerifications>()
        val errors = mutableListOf<String>()
        dataSource(api).getVerifications(7L, onSuccess = { results.add(it) }, onError = { errors.add(it) })

        assertEquals(7L, api.capturedVerificationsId)
        assertEquals(emptyList(), errors)
        val verifications = results.single()
        assertEquals(VerificationStatus.VERIFIED, verifications.challenger.status)
        assertEquals(VerificationStatus.PENDING, verifications.opponent.status)
        assertNull(verifications.opponent.photoUrl)
    }

    @Test
    fun `getVerifications 실패 - onError 호출하고 onSuccess 미호출`() = runTest {
        val api = FakeVerificationApi(
            verificationsResult = ApiResult.Failure.CustomError(code = 700, message = "내 챌린지가 아니에요"),
        )

        var successCalled = false
        val errors = mutableListOf<String>()
        dataSource(api).getVerifications(7L, onSuccess = { successCalled = true }, onError = { errors.add(it) })

        assertTrue(!successCalled)
        assertEquals(listOf("내 챌린지가 아니에요"), errors)
    }
}

private class FakeVerificationApi(
    private val submitResult: ApiResult<SubmitVerificationResponse> =
        ApiResult.Success(SubmitVerificationResponse()),
    private val verificationsResult: ApiResult<ChallengeVerificationsResponse> =
        ApiResult.Success(ChallengeVerificationsResponse()),
) : VerificationApi {

    var capturedSubmitId: Long? = null
        private set
    var capturedSubmitContentType: String? = null
        private set
    var capturedSubmitBody: MultiPartFormDataContent? = null
        private set
    var capturedVerificationsId: Long? = null
        private set

    override suspend fun submitVerification(
        challengeId: Long,
        contentType: String,
        body: MultiPartFormDataContent,
    ): ApiResult<SubmitVerificationResponse> {
        capturedSubmitId = challengeId
        capturedSubmitContentType = contentType
        capturedSubmitBody = body
        return submitResult
    }

    override suspend fun getVerifications(challengeId: Long): ApiResult<ChallengeVerificationsResponse> {
        capturedVerificationsId = challengeId
        return verificationsResult
    }
}
