package com.arsinde.flexkioskapp.core.devicepolicy

/**
 * The single seam that touches `DevicePolicyManager`.
 *
 * Exists so the status mapping can be unit tested on the JVM without a device or Robolectric.
 */
internal interface DeviceOwnerGateway {
    /**
     * @throws RuntimeException if the platform cannot answer, in which case the caller reports
     *   [DeviceOwnerStatus.Error].
     */
    fun isDeviceOwnerApp(applicationId: String): Boolean
}
