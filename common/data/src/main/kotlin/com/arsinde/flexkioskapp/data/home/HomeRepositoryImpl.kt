package com.arsinde.flexkioskapp.data.home

import com.arsinde.flexkioskapp.domain.home.HomeModel
import com.arsinde.flexkioskapp.domain.home.HomeRepository
import kotlinx.coroutines.CancellationException

class HomeRepositoryImpl(
    private val dataSource: HomeDataSource,
) : HomeRepository {
    override suspend fun load(): HomeModel? {
        return try {
            dataSource.loadRemote().also { dataSource.saveLocal(it) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            dataSource.loadLocal() ?: throw failure
        }
    }
}