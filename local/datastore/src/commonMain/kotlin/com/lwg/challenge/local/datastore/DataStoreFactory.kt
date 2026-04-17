package com.lwg.challenge.local.datastore

import androidx.datastore.core.DataStore
import kotlinx.serialization.KSerializer

expect fun <T> createDataStore(
    serializer: KSerializer<T>,
    defaultValue: T,
    fileName: String,
): DataStore<T>
