package com.arsinde.flexkioskapp.data.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.arsinde.flexkioskapp.data.asset.AssetSource
import com.arsinde.flexkioskapp.domain.home.HomeModel
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Offline-first source of home data.
 *
 * `home_stub.json` is a local stand-in for a backend response until the real endpoint is
 * available. It ships with the feature module, and Android merges the assets of every library into
 * the application, so reading it by name from here resolves to that file.
 *
 * The parser intentionally accepts any valid JSON shape and uses a meaningful `id` when present;
 * otherwise it keeps the complete JSON as the model identifier so the response is never discarded.
 */
class HomeDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val assetSource: AssetSource,
) : HomeDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadLocal(): HomeModel? =
        dataStore.data.first()[MODEL_ID]?.let(::HomeModel)

    override suspend fun loadRemote(): HomeModel {
        val response = assetSource.readAsset(STUB_ASSET)
        return HomeModel(id = extractId(json.parseToJsonElement(response)))
    }

    override suspend fun saveLocal(model: HomeModel) {
        dataStore.edit { preferences -> preferences[MODEL_ID] = model.id }
    }

    private fun extractId(element: JsonElement): String = when (element) {
        is JsonObject -> {
            val directId = element["id"]?.asText()
                ?: element["data"]?.let(::extractIdOrNull)
                ?: element["result"]?.let(::extractIdOrNull)
            directId ?: element.values.asSequence().mapNotNull(::extractIdOrNull).firstOrNull()
                ?: element.toString()
        }
        is JsonArray -> element.asSequence().mapNotNull(::extractIdOrNull).firstOrNull()
            ?: element.toString()
        else -> element.asText() ?: element.toString()
    }

    private fun extractIdOrNull(element: JsonElement): String? = runCatching {
        extractId(element)
    }.getOrNull()

    private fun JsonElement.asText(): String? = (this as? JsonPrimitive)
        ?.takeIf { it.isString || it.content != "null" }
        ?.content

    private companion object {
        val MODEL_ID = stringPreferencesKey("home_id")
        const val STUB_ASSET = "home_stub.json"
    }
}