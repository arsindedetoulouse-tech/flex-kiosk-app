package com.arsinde.flexkioskapp.data.home

import com.arsinde.flexkioskapp.domain.home.HomeModel

/**
 * Sources of home data, split by where the data comes from.
 *
 * The split is what makes the feature offline-first: the repository can show a cached model while the
 * remote source is unavailable, and can persist a freshly loaded one.
 */
interface HomeDataSource {
    suspend fun loadLocal(): HomeModel?

    suspend fun loadRemote(): HomeModel

    suspend fun saveLocal(model: HomeModel)
}