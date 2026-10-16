package com.arsinde.flexkioskapp.core.devicepolicy

/**
 * Read access to the current Device Owner status of the application package.
 *
 * Deliberately free of Android types so callers and tests stay independent of the framework, and
 * free of `Context` so an Android instance is never propagated through the graph.
 *
 * Implementations must be side-effect free and must not cache the result: the device can be
 * re-provisioned while the application is running.
 */
interface DeviceOwnerStatusProvider {
    fun getStatus(): DeviceOwnerStatus
}
