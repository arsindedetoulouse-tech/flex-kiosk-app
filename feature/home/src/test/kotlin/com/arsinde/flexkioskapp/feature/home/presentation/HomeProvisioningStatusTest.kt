package com.arsinde.flexkioskapp.feature.home.presentation

import com.arsinde.flexkioskapp.core.devicepolicy.DeviceOwnerStatus
import com.arsinde.flexkioskapp.core.devicepolicy.DeviceOwnerStatusProvider
import com.arsinde.flexkioskapp.domain.home.HomeModel
import com.arsinde.flexkioskapp.domain.home.HomeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.test.testWithInternalState

/**
 * Handed out per call so a test can prove the status is re-read rather than cached.
 */
private class StatusByCall(
    private val statuses: List<DeviceOwnerStatus>,
) : DeviceOwnerStatusProvider {
    var calls: Int = 0
        private set

    override fun getStatus(): DeviceOwnerStatus = statuses[calls++]
}

private val loaded = HomeModel(id = "42")

class ProvisioningStatusMappingTest {

    @Test
    fun `a provisioned device maps to Device Owner`() {
        assertEquals(
            ProvisioningStatus.DeviceOwner,
            DeviceOwnerStatus.DeviceOwner.toProvisioningStatus(),
        )
    }

    @Test
    fun `an ordinary installation maps to not provisioned`() {
        assertEquals(
            ProvisioningStatus.NotProvisioned,
            DeviceOwnerStatus.NotProvisioned.toProvisioningStatus(),
        )
    }

    @Test
    fun `an undeterminable state maps to unavailable`() {
        assertEquals(
            ProvisioningStatus.Unavailable,
            DeviceOwnerStatus.Error(SecurityException("uid=10123")).toProvisioningStatus(),
        )
    }

    @Test
    fun `a mapped status never equals a successful provisioning for a failed lookup`() {
        val mapped = DeviceOwnerStatus
            .Error(RuntimeException("boom"))
            .toProvisioningStatus()

        assertTrue(mapped != ProvisioningStatus.DeviceOwner)
    }

    /**
     * The mapping is lossy on purpose: the cause must not be reachable from anything the screen
     * renders, and the presentation enum offers nowhere to put it.
     */
    @Test
    fun `no home state property can carry a failure cause`() {
        val causeCarrying = HomeState::class.java.declaredFields
            .map { it.type }
            .filter { Throwable::class.java.isAssignableFrom(it) }

        assertTrue(
            causeCarrying.isEmpty(),
            "presentation state must not expose a Throwable, found: $causeCarrying",
        )
    }
}

/**
 * The shape of the settled state, including the provisioning value, is asserted in
 * `HomeViewModelTest`. These cases cover what only the ViewModel can show: that the status is
 * re-read on every refresh instead of cached.
 */
class HomeProvisioningReadTest {

    @Test
    fun `the status is re-read on every refresh`() = runTest {
        // Index 0 is consumed by the ViewModel's initial read; each refresh consumes the next one.
        val statuses = StatusByCall(
            List(INITIAL_READ + REFRESHES) { index ->
                if (index < REFRESHES) {
                    DeviceOwnerStatus.NotProvisioned
                } else {
                    DeviceOwnerStatus.DeviceOwner
                }
            },
        )

        HomeViewModel(HomeUseCase(FakeRepository { loaded }), statuses)
            .testWithInternalState(this) {
                repeat(REFRESHES) {
                    containerHost.onRefresh()
                    // Every refresh emits at least the settled state. How many intermediate states
                    // survive the container's conflated StateFlow is not deterministic, so consume a
                    // single emission and assert on the read count rather than on emission order.
                    skipItems(1)
                }

                assertEquals(INITIAL_READ + REFRESHES, statuses.calls)
                cancelAndIgnoreRemainingItems()
            }
    }

    @Test
    fun `a provider that fails does not break the refresh`() = runTest {
        val statuses = StatusByCall(emptyList())

        HomeViewModel(HomeUseCase(FakeRepository { loaded }), statuses)
            .testWithInternalState(this) {
                containerHost.onRefresh()
                skipItems(1)

                // The initial read and this refresh both attempted a read and both were contained.
                assertEquals(INITIAL_READ + 1, statuses.calls)
                cancelAndIgnoreRemainingItems()
            }
    }

    private companion object {
        const val INITIAL_READ = 1
        const val REFRESHES = 3
    }
}