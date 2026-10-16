package com.arsinde.flexkioskapp.core.devicepolicy

/**
 * Outcome of a Device Owner status query.
 *
 * The states are explicit so that an absent or failed provisioning state can never be mistaken for
 * a successful one: [DeviceOwner] is only ever produced when the platform confirms it.
 */
sealed interface DeviceOwnerStatus {
    /** The application package is not the Device Owner. It may still be a plain installed app. */
    data object NotProvisioned : DeviceOwnerStatus

    /** The platform confirmed that the application package is the Device Owner. */
    data object DeviceOwner : DeviceOwnerStatus

    /**
     * The status could not be determined.
     *
     * This is never a successful provisioning state. Consumers map it to their own error model
     * rather than surfacing [cause] to the UI.
     */
    data class Error(val cause: Throwable) : DeviceOwnerStatus
}
