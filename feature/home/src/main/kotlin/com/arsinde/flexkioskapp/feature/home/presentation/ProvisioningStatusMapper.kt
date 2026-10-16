package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.core.devicepolicy.DeviceOwnerStatus

/**
 * Maps the platform status onto the value this screen renders.
 *
 * The mapping is total and lossy on purpose: [DeviceOwnerStatus.Error] keeps its cause inside
 * `core:devicepolicy` and is reduced to [ProvisioningStatus.Unavailable] here, so the failure detail
 * cannot reach the UI.
 */
internal fun DeviceOwnerStatus.toProvisioningStatus(): ProvisioningStatus = when (this) {
    DeviceOwnerStatus.DeviceOwner -> ProvisioningStatus.DeviceOwner
    DeviceOwnerStatus.NotProvisioned -> ProvisioningStatus.NotProvisioned
    is DeviceOwnerStatus.Error -> ProvisioningStatus.Unavailable
}