package com.lwg.challenge.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import kotlinx.serialization.KSerializer
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object AndroidContext : KoinComponent {
    val context: Context by inject()
}

actual fun <T> createDataStore(
    serializer: KSerializer<T>,
    defaultValue: T,
    fileName: String,
): DataStore<T> {
    val path = AndroidContext.context.filesDir.resolve("datastore/$fileName.json").absolutePath
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = JsonSerializer(serializer, defaultValue),
            producePath = { path.toPath() },
        ),
    )
}
