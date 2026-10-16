package com.arsinde.flexkioskapp.data.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.arsinde.flexkioskapp.data.asset.AssetSource
import com.arsinde.flexkioskapp.domain.home.HomeModel
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

private class FakeAssetSource(private val assets: Map<String, String>) : AssetSource {
    val requested = mutableListOf<String>()

    override suspend fun readAsset(name: String): String {
        requested += name
        return assets[name] ?: throw NoSuchElementException("missing asset: $name")
    }
}

private fun temporaryDataStore(scope: CoroutineScope, file: File): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })

class HomeDataSourceTest {
    @Test
    fun `remote load reads the stub asset through the asset source`() = runTest {
        val assets = FakeAssetSource(mapOf("home_stub.json" to """{"id":"remote-1"}"""))
        val dataSource = HomeDataSourceImpl(temporaryDataStore(this, tempFile()), assets)

        assertEquals(HomeModel(id = "remote-1"), dataSource.loadRemote())
        assertEquals(listOf("home_stub.json"), assets.requested)
    }

    @Test
    fun `remote load keeps the whole payload when no id can be extracted`() = runTest {
        val assets = FakeAssetSource(mapOf("home_stub.json" to "{}"))
        val dataSource = HomeDataSourceImpl(temporaryDataStore(this, tempFile()), assets)

        assertEquals(HomeModel(id = "{}"), dataSource.loadRemote())
    }

    @Test
    fun `remote load prefers an id nested under data or result`() = runTest {
        val assets = FakeAssetSource(
            mapOf("home_stub.json" to """{"result":{"id":"nested-1"}}"""),
        )
        val dataSource = HomeDataSourceImpl(temporaryDataStore(this, tempFile()), assets)

        assertEquals(HomeModel(id = "nested-1"), dataSource.loadRemote())
    }

    @Test
    fun `saved model is readable as the local model`() = runTest {
        val dataStore = temporaryDataStore(this, tempFile())
        val dataSource = HomeDataSourceImpl(dataStore, FakeAssetSource(emptyMap()))

        assertNull(dataSource.loadLocal())

        dataSource.saveLocal(HomeModel(id = "cached-1"))

        assertEquals(HomeModel(id = "cached-1"), dataSource.loadLocal())
    }

    @Test
    fun `remote load surfaces a missing asset instead of inventing a model`() = runTest {
        val dataSource = HomeDataSourceImpl(temporaryDataStore(this, tempFile()), FakeAssetSource(emptyMap()))

        assertThrows<NoSuchElementException> { dataSource.loadRemote() }
    }

    @TempDir
    lateinit var tempDir: File

    private fun tempFile(): File = File(tempDir, "home.preferences_pb")
}
