package com.arsinde.flexkioskapp.data.asset

/**
 * Read access to packaged assets.
 *
 * Data sources depend on this instead of `android.content.Context` so the layer stays free of
 * Android framework types and can be unit tested with an in-memory fake.
 */
interface AssetSource {
    /**
     * Reads the asset [name] as text.
     *
     * Implementations move the blocking read to an I/O dispatcher; callers must not assume the
     * calling thread performs it.
     *
     * @throws NoSuchElementException if no asset named [name] is packaged.
     */
    suspend fun readAsset(name: String): String
}
