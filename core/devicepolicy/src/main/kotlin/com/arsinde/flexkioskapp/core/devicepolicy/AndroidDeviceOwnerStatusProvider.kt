package com.arsinde.flexkioskapp.core.devicepolicy

import android.app.admin.DevicePolicyManager
import android.content.Context

/**
 * Maps the platform answer onto [DeviceOwnerStatus].
 *
 * The framework call is delegated to [gateway] so the mapping holds no Android state and no
 * `Context`. Device provisioning state is not cached: every call re-queries the platform.
 *
 * The query is a synchronous framework call with no suspension point, so there is no cancellation
 * to preserve and no coroutine scope is involved.
 */
class AndroidDeviceOwnerStatusProvider internal constructor(
    private val gateway: DeviceOwnerGateway,
    private val applicationId: String,
) : DeviceOwnerStatusProvider {
    override fun getStatus(): DeviceOwnerStatus =
        try {
            if (gateway.isDeviceOwnerApp(applicationId)) {
                DeviceOwnerStatus.DeviceOwner
            } else {
                DeviceOwnerStatus.NotProvisioned
            }
        } catch (failure: Exception) {
            DeviceOwnerStatus.Error(failure)
        }

    companion object {
        /**
         * Builds a provider for the application package.
         *
         * The `Context` is consumed here and never stored on the provider, so it does not travel
         * through the dependency graph.
         */
        fun create(context: Context): AndroidDeviceOwnerStatusProvider {
            val applicationContext = context.applicationContext
            return AndroidDeviceOwnerStatusProvider(
                gateway = AndroidDeviceOwnerGateway(applicationContext),
                applicationId = applicationContext.packageName,
            )
        }
    }
}

/** [DeviceOwnerGateway] backed by `DevicePolicyManager`. */
internal class AndroidDeviceOwnerGateway(
    context: Context,
) : DeviceOwnerGateway {
    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    override fun isDeviceOwnerApp(applicationId: String): Boolean =
        devicePolicyManager.isDeviceOwnerApp(applicationId)
}
