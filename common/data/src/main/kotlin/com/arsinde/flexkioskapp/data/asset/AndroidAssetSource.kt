package com.arsinde.flexkioskapp.data.asset

import android.content.Context
import java.io.FileNotFoundException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [AssetSource] backed by the packaged assets of the application.
 *
 * The [Context] is held internally: callers of [AssetSource] never receive it, so the data layer is
 * not coupled to the Android framework and stays testable.
 */
class AndroidAssetSource(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AssetSource {
    private val assets = context.applicationContext.assets

    override suspend fun readAsset(name: String): String = withContext(ioDispatcher) {
        try {
            assets.open(name).bufferedReader().use { it.readText() }
        } catch (missing: FileNotFoundException) {
            // NoSuchElementException(String, Throwable) requires API 34, so the cause is attached
            // separately to stay compatible with minSdk 24.
            throw NoSuchElementException("missing asset: $name").apply { initCause(missing) }
        }
    }
}
