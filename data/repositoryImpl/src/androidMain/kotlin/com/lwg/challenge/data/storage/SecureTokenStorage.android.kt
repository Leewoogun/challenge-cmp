package com.lwg.challenge.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
private const val PREFS_NAME = "challenge_secure_tokens"
private const val KEY_ACCESS = "access_token"
private const val KEY_REFRESH = "refresh_token"

/**
 * Android 구현: [EncryptedSharedPreferences] (AES-256-GCM + AndroidKeyStore).
 *
 * Context 는 Koin 에서 androidContext() 로 주입된다.
 */
actual class SecureTokenStorageImpl(
    private val context: Context,
) : SecureTokenStorage {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.Default) {
        prefs.edit().putString(KEY_ACCESS, token).apply()
    }

    actual override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.Default) {
        prefs.edit().putString(KEY_REFRESH, token).apply()
    }

    actual override suspend fun getAccessToken(): String = withContext(Dispatchers.Default) {
        prefs.getString(KEY_ACCESS, "") ?: ""
    }

    actual override suspend fun getRefreshToken(): String = withContext(Dispatchers.Default) {
        prefs.getString(KEY_REFRESH, "") ?: ""
    }

    actual override suspend fun clear() = withContext(Dispatchers.Default) {
        prefs.edit().clear().apply()
    }
}
