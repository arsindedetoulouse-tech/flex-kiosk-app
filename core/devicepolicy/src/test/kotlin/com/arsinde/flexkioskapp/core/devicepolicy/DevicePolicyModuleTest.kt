package com.arsinde.flexkioskapp.core.devicepolicy

import android.app.admin.DevicePolicyManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication

/**
 * Verifies the real Koin wiring, not just the mapping: the provider is resolved from the graph, is
 * a singleton, and re-reads the platform on every call.
 *
 * A single Koin application is used deliberately. Several `koinApplication` instances inside one
 * test class interfere through Koin's global context, which makes the results order-dependent.
 */
class DevicePolicyModuleTest {
    private val applicationId = "com.arsinde.flexkioskapp"

    @Test
    fun `resolved provider tracks the platform state on every call`() {
        var platformAnswer: Result<Boolean> = Result.success(false)
        val devicePolicyManager = mockk<DevicePolicyManager>()
        every { devicePolicyManager.isDeviceOwnerApp(applicationId) } answers {
            platformAnswer.getOrThrow()
        }
        val context = mockk<Context>()
        every { context.applicationContext } returns context
        every { context.packageName } returns applicationId
        every { context.getSystemService(Context.DEVICE_POLICY_SERVICE) } returns devicePolicyManager

        val koin = koinApplication {
            androidContext(context)
            modules(devicePolicyModule)
        }.koin

        val provider = koin.get<DeviceOwnerStatusProvider>()
        assertSame(provider, koin.get<DeviceOwnerStatusProvider>(), "expected a singleton provider")

        platformAnswer = Result.success(false)
        assertEquals(DeviceOwnerStatus.NotProvisioned, provider.getStatus())

        platformAnswer = Result.success(true)
        assertEquals(DeviceOwnerStatus.DeviceOwner, provider.getStatus())

        platformAnswer = Result.failure(SecurityException("device policy unavailable"))
        val failure = provider.getStatus()
        assertEquals(
            SecurityException("device policy unavailable").message,
            (failure as DeviceOwnerStatus.Error).cause.message,
        )
    }
}
