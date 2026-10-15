package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.domain.home.HomeModel
import com.arsinde.flexkioskapp.domain.home.HomeRepository
import com.arsinde.flexkioskapp.domain.home.HomeUseCase
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.test.testWithInternalState

private class FakeRepository(
    private val result: () -> HomeModel?,
) : HomeRepository {
    override suspend fun load(): HomeModel? = result()
}

class HomeViewModelTest {

    @Test
    fun `loading moves through the loading state and ends with the model`() = runTest {
        val repository = FakeRepository { HomeModel(id = "42") }

        HomeViewModel(
            HomeUseCase(repository),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState { copy(isLoading = true) }
            expectInternalState {
                copy(isLoading = false, model = HomeModel(id = "42"))
            }
        }
    }

    @Test
    fun `a failure is reported as state instead of being thrown`() = runTest {
        val repository = FakeRepository { throw IllegalStateException("offline") }

        HomeViewModel(
            HomeUseCase(repository),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState { copy(isLoading = true) }
            expectInternalState { copy(isLoading = false, error = HomeError.Unknown) }
        }
    }

    @Test
    fun `a network failure is reported as a network error`() = runTest {
        val repository = FakeRepository { throw IOException("no route to host") }

        HomeViewModel(
            HomeUseCase(repository),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState { copy(isLoading = true) }
            expectInternalState { copy(isLoading = false, error = HomeError.Network) }
        }
    }

    @Test
    fun `an empty result is neither loading nor an error`() = runTest {
        val repository = FakeRepository { null }

        HomeViewModel(
            HomeUseCase(repository),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState { copy(isLoading = true) }
            expectInternalState { copy(isLoading = false) }
        }
    }

    @Test
    fun `a refresh clears the previous error`() = runTest {
        var failing = true
        val repository = FakeRepository {
            if (failing) throw IllegalStateException("offline")
            HomeModel(id = "42")
        }

        HomeViewModel(
            HomeUseCase(repository),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState { copy(isLoading = true) }
            expectInternalState { copy(isLoading = false, error = HomeError.Unknown) }

            failing = false
            containerHost.onRefresh()

            expectInternalState { copy(isLoading = true, error = null) }
            expectInternalState {
                copy(
                    isLoading = false,
                    model = HomeModel(id = "42"),
                    error = null,
                )
            }
        }
    }
}