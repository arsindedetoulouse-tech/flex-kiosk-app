package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.domain.home.HomeModel

data class HomeState(
    val isLoading: Boolean = false,
    val model: HomeModel? = null,
    val error: HomeError? = null,
    val provisioning: ProvisioningStatus? = null,
)

/**
 * Failure of the home screen the user can act on.
 *
 * The state carries the kind of failure instead of the text of the exception, so the presentation
 * decides what to show and a failure message of the implementation never becomes part of the UI.
 */
enum class HomeError {
    Unknown,
    Network,
}

/**
 * Device-management state as this screen needs to show it.
 *
 * A home-owned enum rather than the platform status model: the platform type is a technical concern
 * of `core:devicepolicy`, and its failure variant carries the original cause. [Unavailable] is
 * deliberately payload-free, so a failure cannot reach the UI through the state.
 */
enum class ProvisioningStatus {
    DeviceOwner,
    NotProvisioned,
    Unavailable,
}

sealed interface HomeSideEffect