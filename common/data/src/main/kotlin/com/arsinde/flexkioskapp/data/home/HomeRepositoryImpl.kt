package com.arsinde.flexkioskapp.data.home

import com.arsinde.flexkioskapp.domain.home.HomeModel
import com.arsinde.flexkioskapp.domain.home.HomeRepository

class HomeRepositoryImpl(
    private val dataSource: HomeDataSource,
) : HomeRepository {
    override suspend fun load(): HomeModel? {
        return runCatching {
            dataSource.loadRemote().also { dataSource.saveLocal(it) }
        }.getOrElse {
            dataSource.loadLocal() ?: throw it
        }
    }
}