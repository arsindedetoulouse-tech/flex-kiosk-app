package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.core.devicepolicy.DeviceOwnerStatusProvider
import com.arsinde.flexkioskapp.domain.home.HomeUseCase
import com.arsinde.flexkioskapp.presentation.BaseViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.orbitmvi.orbit.syntax.Syntax

class HomeViewModel(
    private val useCase: HomeUseCase,
    private val deviceOwnerStatusProvider: DeviceOwnerStatusProvider,
) : BaseViewModel<
    HomeState,
    HomeSideEffect,
>(
    initialState = HomeState(),
) {
    init {
        intent { refresh() }
    }

    fun onRefresh() {
        intent { refresh() }
    }

/**
     * Reloads the data and re-reads the provisioning state as one atomic transition.
     *
     * The status is read synchronously and folded into the same state update that starts the load,
     * so a refresh produces exactly the two state changes it always did instead of an extra one that
     * a conflated state flow would swallow unpredictably.
     */
    private suspend fun Syntax<HomeState, HomeSideEffect>.refresh() {
        reduce { state.copy(isLoading = true, error = null, provisioning = readProvisioning()) }
        try {
            val model = useCase()
            reduce { state.copy(isLoading = false, model = model, error = null) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            reduce {
                state.copy(
                    isLoading = false,
                    error = if (failure is IOException) {
                        HomeError.Network
                    } else {
                        HomeError.Unknown
                    },
                )
            }
        }
    }

    /**
     * Reads the current provisioning state without publishing an intermediate state.
     *
     * The read is repeated on every refresh rather than cached, so a device provisioned while the
     * application is running is reported without a restart.
     *
     * Any failure collapses into [ProvisioningStatus.Unavailable]: the platform cause stays inside
     * `core:devicepolicy` and must not surface as text on this screen.
     */
    private fun readProvisioning(): ProvisioningStatus =
        try {
            deviceOwnerStatusProvider.getStatus().toProvisioningStatus()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            ProvisioningStatus.Unavailable
        }
}