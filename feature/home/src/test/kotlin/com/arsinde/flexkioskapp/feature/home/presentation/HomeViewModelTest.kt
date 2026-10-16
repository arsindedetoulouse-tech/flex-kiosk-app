package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.core.devicepolicy.DeviceOwnerStatus
import com.arsinde.flexkioskapp.core.devicepolicy.DeviceOwnerStatusProvider
import com.arsinde.flexkioskapp.domain.home.HomeModel
import com.arsinde.flexkioskapp.domain.home.HomeRepository
import com.arsinde.flexkioskapp.domain.home.HomeUseCase
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.test.testWithInternalState

internal class FakeRepository(
    private val result: () -> HomeModel?,
) : HomeRepository {
    override suspend fun load(): HomeModel? = result()
}

/** Status provider standing in for the device-management read in ViewModel tests. */
internal class FakeStatusProvider(
    private val status: DeviceOwnerStatus = DeviceOwnerStatus.NotProvisioned,
) : DeviceOwnerStatusProvider {
    override fun getStatus(): DeviceOwnerStatus = status
}

class HomeViewModelTest {

    @Test
    fun `loading moves through the loading state and ends with the model`() = runTest {
        val repository = FakeRepository { HomeModel(id = "42") }

        HomeViewModel(
            HomeUseCase(repository),
            FakeStatusProvider(),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState {
                copy(isLoading = true, provisioning = ProvisioningStatus.NotProvisioned)
            }
            expectInternalState {
                copy(
                    isLoading = false,
                    model = HomeModel(id = "42"),
                    provisioning = ProvisioningStatus.NotProvisioned,
                )
            }
        }
    }

    @Test
    fun `a failure is reported as state instead of being thrown`() = runTest {
        val repository = FakeRepository { throw IllegalStateException("offline") }

        HomeViewModel(
            HomeUseCase(repository),
            FakeStatusProvider(),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState {
                copy(isLoading = true, provisioning = ProvisioningStatus.NotProvisioned)
            }
            expectInternalState {
                copy(
                    isLoading = false,
                    error = HomeError.Unknown,
                    provisioning = ProvisioningStatus.NotProvisioned,
                )
            }
        }
    }

    @Test
    fun `a network failure is reported as a network error`() = runTest {
        val repository = FakeRepository { throw IOException("no route to host") }

        HomeViewModel(
            HomeUseCase(repository),
            FakeStatusProvider(),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState {
                copy(isLoading = true, provisioning = ProvisioningStatus.NotProvisioned)
            }
            expectInternalState {
                copy(
                    isLoading = false,
                    error = HomeError.Network,
                    provisioning = ProvisioningStatus.NotProvisioned,
                )
            }
        }
    }

    @Test
    fun `an empty result is neither loading nor an error`() = runTest {
        val repository = FakeRepository { null }

        HomeViewModel(
            HomeUseCase(repository),
            FakeStatusProvider(),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState {
                copy(isLoading = true, provisioning = ProvisioningStatus.NotProvisioned)
            }
            expectInternalState {
                copy(isLoading = false, provisioning = ProvisioningStatus.NotProvisioned)
            }
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
            FakeStatusProvider(),
        ).testWithInternalState(this) {
            containerHost.onRefresh()

            expectInternalState {
                copy(
                    isLoading = true,
                    provisioning = ProvisioningStatus.NotProvisioned,
                )
            }
            expectInternalState {
                copy(
                    isLoading = false,
                    error = HomeError.Unknown,
                    provisioning = ProvisioningStatus.NotProvisioned,
                )
            }

            failing = false
            containerHost.onRefresh()

            expectInternalState {
                copy(
                    isLoading = true,
                    error = null,
                    provisioning = ProvisioningStatus.NotProvisioned,
                )
            }
            expectInternalState {
                copy(
                    isLoading = false,
                    model = HomeModel(id = "42"),
                    error = null,
                    provisioning = ProvisioningStatus.NotProvisioned,
                )
            }
        }
    }
}