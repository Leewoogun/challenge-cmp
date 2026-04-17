package com.lwg.challenge.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.KSerializer
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun <T> createDataStore(
    serializer: KSerializer<T>,
    defaultValue: T,
    fileName: String,
): DataStore<T> {
    val path = documentDirectory() + "/datastore/$fileName.json"
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = JsonSerializer(serializer, defaultValue),
            producePath = { path.toPath() },
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(directory?.path)
}
