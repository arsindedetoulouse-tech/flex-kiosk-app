package com.arsinde.flexkioskapp.core.devicepolicy

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * DI registrations for device management.
 *
 * The Android `Context` is resolved inside the graph and consumed by the platform adapter, so no
 * `Context` travels through the graph and no `parametersOf` call is needed.
 */
val devicePolicyModule = module {
    single<DeviceOwnerStatusProvider> { AndroidDeviceOwnerStatusProvider.create(androidContext()) }
}
