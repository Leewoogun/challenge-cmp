package com.lwg.challenge.data.repository

import com.lwg.challenge.data.storage.SecureTokenStorage
import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.model.UserProfile
import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.RefreshRequest
import com.lwg.challenge.remote.network.util.ApiResult
import org.koin.core.annotation.Single

/**
 * 서버 API + 플랫폼 보안 저장소를 조합해 [LoginRepository] 를 구현.
 *
 * 에러 코드 매핑 (ADR-0002):
 * - 응답 성공 (ApiResult.Success) + body.code == 200: 정상
 * - 응답 성공 + body.code == 401: refresh 만료로 간주 (onError 는 호출하지 않고 null 반환)
 * - 응답 성공 + 기타 code: onError(code, message)
 * - 네트워크/HTTP 실패: onError(네트워크 코드, message)
 */
@Single(binds = [LoginRepository::class])
class LoginRepositoryImpl(
    private val loginApi: LoginApi,
    private val tokenStorage: SecureTokenStorage,
) : LoginRepository {

    override suspend fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (code: Int, message: String) -> Unit,
    ): LoginResult? {
        return when (val result = loginApi.kakaoLogin(KakaoLoginRequest(kakaoAccessToken))) {
            is ApiResult.Success -> {
                val body = result.response
                if (body.code == CODE_SUCCESS) {
                    val data = body.data
                    val tokens = AuthTokens(
                        accessToken = data.accessToken,
                        refreshToken = data.refreshToken,
                    )
                    tokenStorage.saveAccessToken(tokens.accessToken)
                    tokenStorage.saveRefreshToken(tokens.refreshToken)
                    LoginResult(
                        tokens = tokens,
                        userProfile = UserProfile(
                            userId = data.userId,
                            isNewUser = data.isNewUser,
                        ),
                    )
                } else {
                    onError(body.code, body.message)
                    null
                }
            }

            is ApiResult.Failure -> {
                onError(result.errorCode(), result.errorMessage())
                null
            }
        }
    }

    override suspend fun refreshAccessToken(
        onError: (code: Int, message: String) -> Unit,
    ): AuthTokens? {
        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken.isBlank()) return null

        return when (val result = loginApi.refresh(RefreshRequest(refreshToken))) {
            is ApiResult.Success -> {
                val body = result.response
                when (body.code) {
                    CODE_SUCCESS -> {
                        val newAccess = body.data.accessToken
                        tokenStorage.saveAccessToken(newAccess)
                        AuthTokens(accessToken = newAccess, refreshToken = refreshToken)
                    }
                    CODE_UNAUTHORIZED -> {
                        // refresh 만료 → 저장소 정리 후 null. 로그인 화면으로 유도.
                        tokenStorage.clear()
                        null
                    }
                    else -> {
                        onError(body.code, body.message)
                        null
                    }
                }
            }

            is ApiResult.Failure -> {
                onError(result.errorCode(), result.errorMessage())
                null
            }
        }
    }

    override suspend fun getStoredTokens(): AuthTokens {
        val access = tokenStorage.getAccessToken()
        val refresh = tokenStorage.getRefreshToken()
        return if (access.isBlank() && refresh.isBlank()) {
            AuthTokens.Empty
        } else {
            AuthTokens(accessToken = access, refreshToken = refresh)
        }
    }

    override suspend fun clearTokens() {
        tokenStorage.clear()
    }

    private fun ApiResult.Failure.errorCode(): Int = when (this) {
        is ApiResult.Failure.HttpError -> status_code
        is ApiResult.Failure.CustomError -> status_code
        ApiResult.Failure.NetworkError -> CODE_NETWORK
        is ApiResult.Failure.UnknownApiError -> CODE_UNKNOWN
    }

    private fun ApiResult.Failure.errorMessage(): String = when (this) {
        is ApiResult.Failure.HttpError -> status_message
        is ApiResult.Failure.CustomError -> status_message
        ApiResult.Failure.NetworkError -> "네트워크 오류가 발생하였습니다. 네트워크를 확인하여 주세요."
        is ApiResult.Failure.UnknownApiError -> throwable.message ?: "알 수 없는 오류가 발생하였습니다."
    }

    companion object {
        private const val CODE_SUCCESS = 200
        private const val CODE_UNAUTHORIZED = 401
        private const val CODE_NETWORK = -1
        private const val CODE_UNKNOWN = -2
    }
}
