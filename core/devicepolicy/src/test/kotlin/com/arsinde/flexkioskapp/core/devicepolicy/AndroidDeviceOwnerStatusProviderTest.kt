package com.arsinde.flexkioskapp.core.devicepolicy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The gateway is the only seam that touches the Android framework. A fake keeps the status mapping
 * verifiable on the JVM, per the change design.
 */
private class FakeDeviceOwnerGateway(
    @Volatile var result: Result<Boolean>,
) : DeviceOwnerGateway {
    val queriedApplications = mutableListOf<String>()

    override fun isDeviceOwnerApp(applicationId: String): Boolean {
        queriedApplications += applicationId
        return result.getOrThrow()
    }
}

class AndroidDeviceOwnerStatusProviderTest {
    private val applicationId = "com.arsinde.flexkioskapp"

    @Test
    fun `reports DeviceOwner when the framework confirms it`() {
        val gateway = FakeDeviceOwnerGateway(Result.success(true))

        val status = AndroidDeviceOwnerStatusProvider(gateway, applicationId).getStatus()

        assertEquals(DeviceOwnerStatus.DeviceOwner, status)
    }

    @Test
    fun `reports NotProvisioned when the framework denies it`() {
        val gateway = FakeDeviceOwnerGateway(Result.success(false))

        val status = AndroidDeviceOwnerStatusProvider(gateway, applicationId).getStatus()

        assertEquals(DeviceOwnerStatus.NotProvisioned, status)
    }

    @Test
    fun `reports Error and preserves the cause when the lookup fails`() {
        val failure = IllegalStateException("device policy unavailable")
        val gateway = FakeDeviceOwnerGateway(Result.failure(failure))

        val status = AndroidDeviceOwnerStatusProvider(gateway, applicationId).getStatus()

        val error = assertInstanceOfError(status)
        assertSame(failure, error.cause)
    }

    @Test
    fun `queries the configured application id rather than a hardcoded package`() {
        val gateway = FakeDeviceOwnerGateway(Result.success(false))

        AndroidDeviceOwnerStatusProvider(gateway, "com.example.other").getStatus()

        assertEquals(listOf("com.example.other"), gateway.queriedApplications)
    }

    @Test
    fun `re-evaluates the status on every call instead of caching a successful result`() {
        val gateway = FakeDeviceOwnerGateway(Result.success(false))
        val provider = AndroidDeviceOwnerStatusProvider(gateway, applicationId)

        assertEquals(DeviceOwnerStatus.NotProvisioned, provider.getStatus())

        gateway.result = Result.success(true)
        assertEquals(DeviceOwnerStatus.DeviceOwner, provider.getStatus())

        gateway.result = Result.success(false)
        assertEquals(DeviceOwnerStatus.NotProvisioned, provider.getStatus())

        assertEquals(3, gateway.queriedApplications.size)
    }

    @Test
    fun `two calls of the same state are equal and Error carries its cause`() {
        val gateway = FakeDeviceOwnerGateway(Result.failure(RuntimeException("boom")))

        val first = AndroidDeviceOwnerStatusProvider(gateway, applicationId).getStatus()
        val second = AndroidDeviceOwnerStatusProvider(gateway, applicationId).getStatus()

        assertEquals(first, second)
        assertTrue(first is DeviceOwnerStatus.Error)
    }

    @Test
    fun `a failed lookup is never equal to DeviceOwner`() {
        val gateway = FakeDeviceOwnerGateway(Result.failure(RuntimeException("boom")))

        val status = AndroidDeviceOwnerStatusProvider(gateway, applicationId).getStatus()

        assertNotEquals(DeviceOwnerStatus.DeviceOwner, status)
    }

    private fun assertInstanceOfError(status: DeviceOwnerStatus): DeviceOwnerStatus.Error {
        assertTrue(status is DeviceOwnerStatus.Error, "expected Error but was $status")
        return status as DeviceOwnerStatus.Error
    }
}
