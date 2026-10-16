# Tasks: Add Custom Device Policy Controller

## 1. Inspect the existing project

- [x] Inspect `AndroidManifest.xml`, application namespace, Gradle `applicationId`, `minSdk`, and `targetSdk`.
      - `applicationId`/`namespace` = `com.arsinde.flexkioskapp`; `minSdk = 24` (build-logic convention plugin); `targetSdk` comes from AGP defaults.
- [x] Identify package conventions, existing error/result types, Koin module organization, and test frameworks.
      - No `Result` wrapper; sealed types and presentation error models are the convention (`HomeState`/`HomeError`).
      - Koin modules live next to the code they configure and are wired in `App.kt` (`homeModule`, `coreDataStoreModule`, `coreNetworkModule`).
      - Tests are JUnit 5 + MockK + Orbit test in module-local `src/test`.
- [x] Confirm the change does not require edits to protected OpenSpec configuration files.

## 2. Create the `core:devicepolicy` module

- [x] Register `:core:devicepolicy` in `settings.gradle.kts`.
- [x] Apply the existing `android.library`, `android.koin` and `android.library.test` convention plugins.
- [x] Keep the module free of dependencies on `app`, `feature:*`, `common:data` and `common:presentation`.
- [x] Verify `./gradlew :core:devicepolicy:assembleDebug` succeeds for an empty module.

## 3. Register the administrative component

- [x] Add `KioskDeviceAdminReceiver` as a minimal subclass of `DeviceAdminReceiver` under `core/devicepolicy/.../admin/`.
- [x] Add `res/xml/device_admin.xml` with valid device-admin metadata and an empty `<uses-policies />`.
- [x] Declare the receiver in the module manifest using `android.permission.BIND_DEVICE_ADMIN`, `android:exported="true"`, the `android.app.device_admin` metadata reference and the `DEVICE_ADMIN_ENABLED` intent filter.
- [x] Leave the app-level `AndroidManifest.xml` unchanged and confirm the receiver reaches the merged manifest.
- [x] Verify no HOME intent filter, boot receiver, Lock Task call, or kiosk restriction is added.

## 4. Implement Device Owner status detection

- [x] Define `DeviceOwnerStatus` with `NotProvisioned`, `DeviceOwner`, and `Error(cause)`, following project error conventions.
- [x] Define `DeviceOwnerStatusProvider` without Android types in its signature.
- [x] Define an internal `DeviceOwnerGateway` so the mapping is testable without a device.
- [x] Implement `AndroidDeviceOwnerStatusProvider` on top of the gateway using `DevicePolicyManager.isDeviceOwnerApp(applicationId)`.
- [x] Map `true` to `DeviceOwner`, `false` to `NotProvisioned`, and unexpected failures to `Error`.
- [x] Keep the application `Context` inside the Android adapter; do not expose it through the interface or to callers.
- [x] Ensure status checks have no policy-changing side effects.
- [x] Do not cache the Device Owner result.
- [x] Add `devicePolicyModule` following the existing Koin module convention and register it in `App.kt`.

## 5. Add automated tests

- [x] Add a unit test for the `DeviceOwner` result.
- [x] Add a unit test for the `NotProvisioned` result.
- [x] Add a unit test for error mapping, asserting the cause is preserved.
- [x] Add a test that verifies the status is re-evaluated on a subsequent query.
- [x] Add a Koin resolution test for `DeviceOwnerStatusProvider`.
- [x] Verify the tests do not depend on actual Device Owner provisioning or on Robolectric.

## 6. Document and verify provisioning

- [x] Document prerequisites for assigning Device Owner on a compatible test device.
- [x] Document ADB install and `dpm set-device-owner` commands using the real application ID and receiver component.
- [x] Document verification with `adb shell dumpsys device_policy`.
- [x] Build and install the application on an ordinary, unprovisioned device; platform reports no Device Owner.
- [x] Provision a compatible resettable test device; platform reports our receiver as Device Owner.
- [x] Verify the DPC applies no policy: `mLockTaskModeState=NONE` after provisioning.
- [x] Verify the existing Home Screen remains usable in both provisioning states.
- [x] Surface the provisioning state on the Home Screen as a status card.
- [x] Explain an undeterminable state without exposing the failure detail.
- [x] Re-read the status on refresh so a device provisioned at runtime is reflected.
- [x] Verify the card renders the provisioned state on a device.
- [x] Verify the card renders Russian text on a device.
- [~] Deferred: the card in the not-provisioned state was not seen on a device. The emulator could not be
      returned to that state without a wipe. Tracked in `TECHNICAL_DEBT.md` section 2; covered by
      `ProvisioningStatusMappingTest`.
- [~] Deferred: the undeterminable state cannot be produced on a healthy device, because it needs
      `DevicePolicyManager` to fail. Covered by `ProvisioningStatusMappingTest` and
      `HomeProvisioningReadTest`. Tracked in `TECHNICAL_DEBT.md` section 2.

### Status card evidence

Recorded 2026-10-16, on the Pixel_9a emulator (API 37) after provisioning.

- Provisioned state renders: the card shows "Managed: this device is provisioned as Device Owner".
- The card renders Russian text when the per-app locale is set to `ru-RU`, which also confirms that
  `values-ru` resources resolve at runtime.
- Returning the emulator to the not-provisioned state requires a data wipe, because Android refuses to
  uninstall a device owner (`DELETE_FAILED_DEVICE_POLICY_MANAGER`). That was not performed.
- The undeterminable state cannot be produced on a healthy device: it needs `DevicePolicyManager` to
  fail, which no realistic device condition produces. It is covered by unit tests only.

## 7. Final validation

- [x] Run `./gradlew test` and `./gradlew lint`.
- [x] Run `./gradlew :app:assembleDebug`.
- [x] Review the diff to ensure no kiosk policies, Lock Task Mode, persistent HOME configuration, remote management, or SQLite work has entered scope.
- [ ] Record device/API limitations and test results.
- [ ] Mark the change ready for review only after all required checks pass.
