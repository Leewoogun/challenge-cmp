package com.lwg.challenge.data.storage

/**
 * 플랫폼 보안 저장소에 access/refresh 토큰을 저장/조회/삭제.
 *
 * - Android: EncryptedSharedPreferences (AES-256-GCM, androidx.security:security-crypto).
 * - iOS: Keychain (kSecClassGenericPassword).
 *
 * 평문 SharedPreferences / UserDefaults / DataStore 사용 금지 (spec 참조).
 *
 * 구현체는 플랫폼별 [SecureTokenStorageImpl] 의 actual 선언.
 */
interface SecureTokenStorage {

    suspend fun saveAccessToken(token: String)

    suspend fun saveRefreshToken(token: String)

    suspend fun getAccessToken(): String

    suspend fun getRefreshToken(): String

    suspend fun clear()
}

/**
 * [SecureTokenStorage] 의 플랫폼별 구현체 (expect/actual).
 *
 * Koin 은 플랫폼별 모듈에서 `SecureTokenStorage` 로 바인딩된다.
 */
expect class SecureTokenStorageImpl : SecureTokenStorage {
    override suspend fun saveAccessToken(token: String)
    override suspend fun saveRefreshToken(token: String)
    override suspend fun getAccessToken(): String
    override suspend fun getRefreshToken(): String
    override suspend fun clear()
}
