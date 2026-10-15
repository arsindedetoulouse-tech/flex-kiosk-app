# Tasks: Add Custom Device Policy Controller

## 1. Inspect the existing project

- [ ] Inspect `AndroidManifest.xml`, application namespace, Gradle `applicationId`, `minSdk`, and `targetSdk`.
- [ ] Identify package conventions, existing error/result types, Koin module organization, and test frameworks.
- [ ] Confirm the change does not require edits to protected OpenSpec configuration files.

## 2. Register the administrative component

- [ ] Add `KioskDeviceAdminReceiver` as a minimal subclass of `DeviceAdminReceiver`.
- [ ] Add `res/xml/device_admin.xml` with valid device-admin metadata and an empty `<uses-policies />`.
- [ ] Register the receiver in the existing manifest using `android.permission.BIND_DEVICE_ADMIN`.
- [ ] Add the `android.app.device_admin` metadata reference and `DEVICE_ADMIN_ENABLED` intent filter.
- [ ] Preserve unrelated manifest declarations and existing Home Screen behavior.
- [ ] Verify no HOME intent filter, boot receiver, Lock Task call, or kiosk restriction is added.

## 3. Implement Device Owner status detection

- [ ] Define `DeviceOwnerStatus` with `NotProvisioned`, `DeviceOwner`, and `Error`, following project error conventions.
- [ ] Define a small, testable `DeviceOwnerStatusProvider` interface.
- [ ] Implement `AndroidDeviceOwnerStatusProvider` using `DevicePolicyManager.isDeviceOwnerApp(applicationId)`.
- [ ] Map `true` to `DeviceOwner`, `false` to `NotProvisioned`, and unexpected failures to `Error`.
- [ ] Ensure status checks have no policy-changing side effects.
- [ ] Avoid permanently caching the Device Owner result.
- [ ] Register the implementation with existing Koin setup only if required by the current architecture.

## 4. Add automated tests

- [ ] Add unit tests for the `DeviceOwner` result.
- [ ] Add unit tests for the `NotProvisioned` result.
- [ ] Add unit tests for error mapping.
- [ ] Add a test that verifies status is re-evaluated on subsequent queries.
- [ ] Use a fake gateway/provider so domain behavior can be tested without a physical device.
- [ ] Verify tests do not depend on actual Device Owner provisioning.

## 5. Document and verify provisioning

- [ ] Document prerequisites for assigning Device Owner on a compatible test device.
- [ ] Document ADB install and `dpm set-device-owner` commands using the actual project application ID and receiver component.
- [ ] Document verification with `adb shell dumpsys device_policy`.
- [ ] Build and install the application on an ordinary, unprovisioned device and verify `NotProvisioned`.
- [ ] Provision a compatible resettable test device and verify `DeviceOwner`.
- [ ] Verify provisioning failure is not reported as success.
- [ ] Verify the existing Home Screen remains usable in both provisioning states.

## 6. Final validation

- [ ] Run relevant unit tests.
- [ ] Run the Android build and repository lint checks.
- [ ] Review the diff to ensure no kiosk policies, Lock Task Mode, persistent HOME configuration, remote management, or SQLite work has entered scope.
- [ ] Validate the OpenSpec change against the repository's installed conventions.
- [ ] Record device/API limitations and test results.
- [ ] Mark the change ready for review only after all required checks pass.
