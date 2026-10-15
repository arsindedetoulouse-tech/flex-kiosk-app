package com.arsinde.flexkioskapp.data.home

import com.arsinde.flexkioskapp.domain.home.HomeModel
import com.arsinde.flexkioskapp.domain.home.HomeRepository
import com.arsinde.flexkioskapp.domain.home.HomeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private class FakeDataSource(
    private val remote: () -> HomeModel,
    private val local: () -> HomeModel? = { null },
) : HomeDataSource {
    var saved: HomeModel? = null

    override suspend fun loadLocal(): HomeModel? = local()
    override suspend fun loadRemote(): HomeModel = remote()
    override suspend fun saveLocal(model: HomeModel) { saved = model }
}

class HomeRepositoryTest {
    @Test
    fun `repository returns remote data and caches it`() = runTest {
        val dataSource = FakeDataSource(remote = { HomeModel(id = "42") })
        val repository = HomeRepositoryImpl(dataSource)

        assertEquals(HomeModel(id = "42"), repository.load())
        assertEquals(HomeModel(id = "42"), dataSource.saved)
    }

    @Test
    fun `repository propagates an error when there is no cached data`() = runTest {
        val repository = HomeRepositoryImpl(
            FakeDataSource(
                remote = { throw IllegalStateException("offline") },
                local = { null },
            ),
        )

        assertThrows<IllegalStateException> { repository.load() }
    }

    @Test
    fun `repository returns cached data when remote loading fails`() = runTest {
        val repository = HomeRepositoryImpl(
            FakeDataSource(
                remote = { throw IllegalStateException("offline") },
                local = { HomeModel(id = "cached") },
            ),
        )

        assertEquals(HomeModel(id = "cached"), repository.load())
    }

    @Test
    fun `use case keeps the repository boundary`() = runTest {
        var calls = 0
        val repository = object : HomeRepository {
            override suspend fun load(): HomeModel? =
                HomeModel(id = "${++calls}")
        }
        val useCase = HomeUseCase(repository)

        assertEquals(HomeModel(id = "1"), useCase())
        assertEquals(HomeModel(id = "2"), useCase())
        assertEquals(2, calls)
    }
}