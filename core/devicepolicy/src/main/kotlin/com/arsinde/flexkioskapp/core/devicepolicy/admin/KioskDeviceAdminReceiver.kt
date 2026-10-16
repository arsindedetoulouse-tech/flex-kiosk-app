package com.arsinde.flexkioskapp.core.devicepolicy.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.util.Log

/**
 * Administrative component of the Device Policy Controller.
 *
 * The receiver exists only so Android can recognise and bind the application as a device
 * administrator. It is deliberately inert:
 *
 * - it applies no policy, including no Lock Task Mode configuration;
 * - it performs no network or database work;
 * - it starts no long-running work, so nothing outlives the callback;
 * - it logs only the lifecycle event, never credentials or device state.
 *
 * Kiosk behaviour belongs to the policy layer applied by the application, not to these callbacks.
 */
class KioskDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: android.content.Intent) {
        Log.i(TAG, "Device administrator enabled")
    }

    override fun onDisabled(context: Context, intent: android.content.Intent) {
        Log.i(TAG, "Device administrator disabled")
    }

    private companion object {
        const val TAG = "KioskDeviceAdmin"
    }
}
