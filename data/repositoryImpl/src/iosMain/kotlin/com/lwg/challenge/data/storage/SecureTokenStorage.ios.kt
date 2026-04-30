package com.lwg.challenge.data.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.numberWithBool
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val SERVICE = "com.lwg.challenge.auth"
private const val KEY_ACCESS = "access_token"
private const val KEY_REFRESH = "refresh_token"

/**
 * iOS Keychain 구현.
 *
 * NSDictionary / CFDictionary 는 toll-free bridged 이므로 NSDictionary (Kotlin Map via `mapOf`)
 * 를 그대로 Security API 에 넘긴다. Kotlin/Native 가 Map<Any?, Any?> 를 자동으로 NSDictionary 로 변환.
 *
 * - kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly: 백업/동기화 대상 제외.
 * - 외부 wrapper 라이브러리 없이 구현.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class SecureTokenStorageImpl : SecureTokenStorage {

    actual override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.Default) {
        set(KEY_ACCESS, token)
    }

    actual override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.Default) {
        set(KEY_REFRESH, token)
    }

    actual override suspend fun getAccessToken(): String = withContext(Dispatchers.Default) {
        get(KEY_ACCESS)
    }

    actual override suspend fun getRefreshToken(): String = withContext(Dispatchers.Default) {
        get(KEY_REFRESH)
    }

    actual override suspend fun clear() = withContext(Dispatchers.Default) {
        remove(KEY_ACCESS)
        remove(KEY_REFRESH)
    }

    @Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
    private fun set(account: String, value: String) {
        val nsData = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val baseQuery: Map<Any?, Any?> = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to account,
        )
        val updateAttrs: Map<Any?, Any?> = mapOf(
            kSecValueData to nsData,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        )
        val updateStatus = SecItemUpdate(
            baseQuery as platform.CoreFoundation.CFDictionaryRef?,
            updateAttrs as platform.CoreFoundation.CFDictionaryRef?,
        )

        if (updateStatus == errSecItemNotFound) {
            val addQuery: Map<Any?, Any?> = baseQuery + updateAttrs
            SecItemAdd(addQuery as platform.CoreFoundation.CFDictionaryRef?, null)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun get(account: String): String = memScoped {
        val query: Map<Any?, Any?> = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to account,
            kSecReturnData to NSNumber.numberWithBool(true),
            kSecMatchLimit to kSecMatchLimitOne,
        )
        val resultVar = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(
            query as platform.CoreFoundation.CFDictionaryRef?,
            resultVar.ptr,
        )
        if (status != errSecSuccess) return@memScoped ""
        val resultRef = resultVar.value ?: return@memScoped ""
        val nsData = resultRef as NSData
        NSString.create(nsData, NSUTF8StringEncoding)?.toString().orEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    private fun remove(account: String) {
        val query: Map<Any?, Any?> = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to account,
        )
        SecItemDelete(query as platform.CoreFoundation.CFDictionaryRef?)
    }
}
