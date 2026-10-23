package com.lwg.challenge.remote.network.auth

import com.lwg.challenge.utils.AuthEventBus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val BASE_URL = "https://mock.challenge.test/"
private const val RECORD_PATH = "record"
private const val REFRESH_PATH = "auth/refresh"

private const val ACCESS_TOKEN = "access-token-1"
private const val REFRESH_TOKEN = "refresh-token-1"
private const val NEW_ACCESS_TOKEN = "access-token-2"
private const val NEW_REFRESH_TOKEN = "refresh-token-2"

private const val REFRESH_SUCCESS_BODY =
    """{"error":false,"code":200,"message":"OK","data":{"accessToken":"$NEW_ACCESS_TOKEN","refreshToken":"$NEW_REFRESH_TOKEN"}}"""

private const val REFRESH_EXPIRED_BODY =
    """{"error":true,"code":401,"message":"refresh token expired","data":{"accessToken":"","refreshToken":""}}"""

/**
 * [installChallengeAuth] 의 **현재 실동작**을 실측으로 고정하는 특성화 테스트.
 *
 * 여기 단언들은 "이래야 한다" 가 아니라 "지금 이렇다" 를 박제한 것이다.
 * 일부는 의도된 동작이고 일부는 결함이며, 어느 쪽인지는 각 테스트 주석에 표기했다.
 */
class ChallengeAuthTest {

    // ───────────────────────── A. 선제 부착 sendWithoutRequest ─────────────────────────

    @Test
    fun `record 요청에는 Bearer 토큰이 선제 부착된다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val client = testClient(FakeTokenProvider(), recorded = recorded) { respondJson("{}") }

        client.get("${BASE_URL}api/v1/record")

        assertEquals("Bearer $ACCESS_TOKEN", recorded.single().authorization)
    }

    // 카카오 로그인은 토큰 발급 이전에 호출되는 공개 엔드포인트다 — 부착 제외가 맞다.
    @Test
    fun `auth kakao 경로에는 토큰이 선제 부착되지 않는다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val client = testClient(FakeTokenProvider(), recorded = recorded) { respondJson("{}") }

        client.post("${BASE_URL}api/v1/auth/kakao")

        assertNull(recorded.single().authorization)
    }

    // 만료된 access 를 갱신 요청에 실으면 그 요청이 다시 401 → 갱신을 부르는 재귀가 된다. 부착 제외가 맞다.
    @Test
    fun `auth refresh 경로에는 토큰이 선제 부착되지 않는다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val client = testClient(FakeTokenProvider(), recorded = recorded) { respondJson(REFRESH_SUCCESS_BODY) }

        client.post("${BASE_URL}api/v1/auth/refresh")

        assertNull(recorded.single().authorization)
    }

    // logout 은 auth 밑에 있지만 공개 엔드포인트가 아니라 Bearer 가 **필수** 인 엔드포인트다.
    // 선제 부착이 빠지면 ktor-client-auth 가 401 자동 갱신 대상으로도 잡지 않는다.
    @Test
    fun `auth logout 경로에는 토큰이 선제 부착된다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val client = testClient(FakeTokenProvider(), recorded = recorded) { respondJson("{}") }

        client.delete("${BASE_URL}api/v1/auth/logout")

        assertEquals("Bearer $ACCESS_TOKEN", recorded.single().authorization)
    }

    // 테스트 로그인도 토큰 발급 이전에 호출되는 공개 엔드포인트다.
    @Test
    fun `auth test-login 경로에는 토큰이 선제 부착되지 않는다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val client = testClient(FakeTokenProvider(), recorded = recorded) { respondJson("{}") }

        client.post("${BASE_URL}api/v1/auth/test-login")

        assertNull(recorded.single().authorization)
    }

    // 이미지 로더가 같은 클라이언트를 쓰므로 외부 CDN 요청에 우리 토큰이 실리면 유출이다.
    @Test
    fun `외부 호스트 요청에는 토큰이 선제 부착되지 않는다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val client = testClient(FakeTokenProvider(), recorded = recorded) { respondJson("{}") }

        client.get("https://k.kakaocdn.net/dn/abc/img_640x640.jpg")

        assertNull(recorded.single().authorization)
    }

    @Test
    fun `access 토큰이 비어 있으면 어떤 요청에도 헤더가 붙지 않는다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val tokenProvider = FakeTokenProvider(accessToken = "", refreshToken = REFRESH_TOKEN)
        val client = testClient(tokenProvider, recorded = recorded) { respondJson("{}") }

        client.get("${BASE_URL}api/v1/record")
        client.delete("${BASE_URL}api/v1/auth/logout")

        assertEquals(2, recorded.size)
        assertTrue(recorded.all { it.authorization == null })
    }

    // ───────────────────────── B. 401 자동 갱신 ─────────────────────────

    @Test
    fun `401 에 WWW-Authenticate 헤더가 있으면 갱신 후 원 요청을 재시도한다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val tokenProvider = FakeTokenProvider()
        val client = testClient(tokenProvider, recorded = recorded) { request ->
            when {
                request.path().endsWith(REFRESH_PATH) -> respondJson(REFRESH_SUCCESS_BODY)
                recorded.count { it.path.endsWith(RECORD_PATH) } == 1 -> respondUnauthorized(withChallenge = true)
                else -> respondJson("{}")
            }
        }

        client.get("${BASE_URL}api/v1/record")

        assertEquals(listOf(RECORD_PATH, REFRESH_PATH, RECORD_PATH), recorded.map { it.shortPath })
        assertEquals("Bearer $NEW_ACCESS_TOKEN", recorded.last().authorization)
        assertEquals(1, tokenProvider.updateCount)
        assertEquals(NEW_ACCESS_TOKEN to NEW_REFRESH_TOKEN, tokenProvider.lastUpdatedTokens)
    }

    /**
     * 🔴 **가설이며 실행 결과로 확정한다.**
     *
     * 서버가 401 에 `WWW-Authenticate` 를 안 실어도 Ktor 의 `refreshTokens` 가 도는지는
     * 아무도 확인한 적이 없다. 여기서는 "돈다" 로 단언해 두었다.
     * - 통과하면: 헤더 없이도 ADR-0009 의 401 자동 갱신이 동작한다.
     * - 실패하면: Ktor 가 그 헤더를 요구한다는 뜻이고, ADR-0009 의 자동 갱신 전체가
     *   서버의 `WWW-Authenticate` 응답에 의존한다는 발견이 된다.
     */
    @Test
    fun `401 에 WWW-Authenticate 헤더가 없어도 갱신이 시도된다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val tokenProvider = FakeTokenProvider()
        val client = testClient(tokenProvider, recorded = recorded) { request ->
            when {
                request.path().endsWith(REFRESH_PATH) -> respondJson(REFRESH_SUCCESS_BODY)
                recorded.count { it.path.endsWith(RECORD_PATH) } == 1 -> respondUnauthorized(withChallenge = false)
                else -> respondJson("{}")
            }
        }

        client.get("${BASE_URL}api/v1/record")

        assertTrue(recorded.any { it.shortPath == REFRESH_PATH })
        assertEquals(1, tokenProvider.updateCount)
    }

    @Test
    fun `갱신 응답 code 가 401 이면 토큰을 지우고 세션 만료를 알린다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val tokenProvider = FakeTokenProvider()
        val authEventBus = AuthEventBus()
        val sessionExpired = collectSessionExpired(authEventBus)
        val client = testClient(tokenProvider, authEventBus, recorded) { request ->
            if (request.path().endsWith(REFRESH_PATH)) {
                respondJson(REFRESH_EXPIRED_BODY)
            } else {
                respondUnauthorized(withChallenge = true)
            }
        }

        client.get("${BASE_URL}api/v1/record")

        assertEquals(1, tokenProvider.clearCount)
        assertEquals(1, sessionExpired.size)
    }

    @Test
    fun `refresh 토큰이 비어 있으면 서버 호출 없이 세션 만료를 알린다`() = runTest {
        val recorded = mutableListOf<RecordedRequest>()
        val tokenProvider = FakeTokenProvider(refreshToken = "")
        val authEventBus = AuthEventBus()
        val sessionExpired = collectSessionExpired(authEventBus)
        val client = testClient(tokenProvider, authEventBus, recorded) { respondUnauthorized(withChallenge = true) }

        client.get("${BASE_URL}api/v1/record")

        assertTrue(recorded.none { it.shortPath == REFRESH_PATH })
        assertEquals(1, sessionExpired.size)
        assertEquals(0, tokenProvider.clearCount)
    }

    // ───────────────────────── fixtures ─────────────────────────

    private fun testClient(
        tokenProvider: TokenProvider,
        authEventBus: AuthEventBus = AuthEventBus(),
        recorded: MutableList<RecordedRequest>,
        handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine { request ->
            recorded += RecordedRequest(
                path = request.path(),
                authorization = request.headers[HttpHeaders.Authorization],
            )
            handler(this, request)
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            installChallengeAuth(tokenProvider, authEventBus, BASE_URL)
        }
    }

    // sessionExpired 는 replay 0 인 SharedFlow 라 emit 시점에 구독자가 없으면 이벤트가 사라진다.
    private fun TestScope.collectSessionExpired(authEventBus: AuthEventBus): List<Unit> {
        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            authEventBus.sessionExpired.collect { events += it }
        }
        return events
    }
}

private class RecordedRequest(val path: String, val authorization: String?) {
    val shortPath: String get() = path.removePrefix("/api/v1/")
}

private class FakeTokenProvider(
    private var accessToken: String = ACCESS_TOKEN,
    private var refreshToken: String = REFRESH_TOKEN,
) : TokenProvider {
    var updateCount = 0
        private set
    var clearCount = 0
        private set
    var lastUpdatedTokens: Pair<String, String>? = null
        private set

    override suspend fun getAccessToken(): String = accessToken

    override suspend fun getRefreshToken(): String = refreshToken

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        updateCount++
        lastUpdatedTokens = accessToken to refreshToken
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clearTokens() {
        clearCount++
        accessToken = ""
        refreshToken = ""
    }
}

private fun HttpRequestData.path(): String = url.encodedPath

private fun MockRequestHandleScope.respondJson(body: String): HttpResponseData = respond(
    content = body,
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
)

private fun MockRequestHandleScope.respondUnauthorized(withChallenge: Boolean): HttpResponseData = respond(
    content = "",
    status = HttpStatusCode.Unauthorized,
    headers = if (withChallenge) headersOf(HttpHeaders.WWWAuthenticate, "Bearer") else headersOf(),
)
