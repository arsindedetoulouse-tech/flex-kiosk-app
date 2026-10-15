# Design: Add Custom Device Policy Controller

## 1. Context

The application currently contains a Home Screen and does not yet have a device-management component.

This change introduces the minimum foundation for a project-owned Android Device Policy Controller (DPC). It must allow the application to be provisioned as Device Owner on a compatible, fully managed test device and expose the current provisioning state to the application.

This change does **not** implement Kiosk Mode. It does not configure Lock Task Mode, change the preferred HOME activity, apply kiosk restrictions, start the application after boot, or add remote management.

## 2. Goals

- Register a custom `DeviceAdminReceiver`.
- Add device-admin XML metadata and manifest registration.
- Isolate Device Owner status detection behind a small, testable abstraction.
- Represent provisioning outcomes explicitly.
- Keep the existing Home Screen behavior unchanged.
- Document local ADB provisioning and verification.
- Add unit tests for status mapping and error handling.

## 3. Non-goals

- Applying `DevicePolicyManager` policies beyond checking Device Owner status.
- Calling `setLockTaskPackages()`, `setLockTaskFeatures()`, or `startLockTask()`.
- Configuring a persistent preferred HOME activity.
- Adding boot receivers, automatic startup, or process recovery.
- Adding Ktor clients, backend endpoints, remote commands, or SQLite persistence.
- Adding maintenance UI or decommissioning workflow.
- Implementing production QR provisioning.

## 4. Proposed structure

Adapt package names to the existing application namespace; do not introduce a new namespace solely for this feature.

```text
<existing namespace>/
├── deviceadmin/
│   └── KioskDeviceAdminReceiver.kt
└── devicepolicy/
    ├── DeviceOwnerStatus.kt
    ├── DeviceOwnerStatusProvider.kt
    └── AndroidDeviceOwnerStatusProvider.kt
```

The exact directory names may follow repository conventions. Keep the receiver as a thin Android framework adapter and keep status interpretation testable independently of Android framework classes.

## 5. Components

### 5.1 `KioskDeviceAdminReceiver`

A minimal subclass of `android.app.admin.DeviceAdminReceiver`.

Responsibilities:
- Provide the receiver class referenced by the manifest.
- Handle only lifecycle callbacks needed by this feature.
- Optionally emit safe diagnostic logs for enable/disable callbacks.

Constraints:
- Do not apply kiosk policies from receiver callbacks.
- Do not perform network or database operations.
- Do not start long-running work.
- Do not log credentials or sensitive device information.

The receiver declaration must use `android.permission.BIND_DEVICE_ADMIN`, include the `android.app.device_admin` metadata entry, and reference a valid XML resource under `res/xml/`.

The XML metadata should use an empty `<uses-policies />` element because this change requests no legacy device-admin policies. Device Owner status is determined separately through `DevicePolicyManager`.

### 5.2 `DeviceOwnerStatus`

Represent the outcome of a status query explicitly. A minimal model is:

```kotlin
sealed interface DeviceOwnerStatus {
    data object NotProvisioned : DeviceOwnerStatus
    data object DeviceOwner : DeviceOwnerStatus
    data class Error(val cause: Throwable) : DeviceOwnerStatus
}
```

Use the project's existing error conventions if it already has a standard result/error type. Avoid exposing raw exceptions to UI code if the project normally maps exceptions to domain errors.

`NotProvisioned` means the application package is not the Device Owner. It does not necessarily mean legacy Device Admin is disabled.

`Error` means the status check could not be completed. It must never be treated as successful provisioning.

### 5.3 `DeviceOwnerStatusProvider`

Define a small interface for retrieving the current status. Prefer a synchronous API if it only wraps the local `DevicePolicyManager.isDeviceOwnerApp()` call; do not introduce coroutines for a simple immediate framework query.

```kotlin
interface DeviceOwnerStatusProvider {
    fun getStatus(): DeviceOwnerStatus
}
```

If the repository has a standard domain/use-case convention, expose the interface in the appropriate domain/application package and place the Android implementation in the platform/data layer.

### 5.4 `AndroidDeviceOwnerStatusProvider`

The Android implementation should:

1. Obtain `DevicePolicyManager` from the application context.
2. Call `isDeviceOwnerApp(context.packageName)`.
3. Return `DeviceOwner` when the API returns `true`.
4. Return `NotProvisioned` when the API returns `false`.
5. Map an unexpected exception to `Error` using project conventions.

The implementation must not cache a successful result indefinitely. A status query should be safe to repeat when the application needs to refresh provisioning state.

### 5.5 Dependency injection

Register the Android implementation through existing Koin module conventions if dependency injection is required by the current application.

Do not add a new DI framework, global service locator, persistence, or network abstractions solely for this feature.

The current Home Screen does not need to display provisioning state unless the existing architecture already has a diagnostic surface that can consume it without changing user-facing behavior.

## 6. Manifest and resources

Update the existing `AndroidManifest.xml`; do not replace unrelated application declarations.

The receiver should be declared with:
- The fully qualified receiver class.
- `android.permission.BIND_DEVICE_ADMIN`.
- An `android:exported` value compatible with the target SDK and system-managed receiver declaration.
- The `android.app.device_admin` metadata reference.
- The `android.app.action.DEVICE_ADMIN_ENABLED` intent action.

Add `res/xml/device_admin.xml` containing a valid `<device-admin>` root and an empty `<uses-policies />`.

Do not add HOME/LAUNCHER intent filters or `BOOT_COMPLETED` handling in this change.

## 7. Provisioning and development verification

Document a development-only procedure for a compatible test device:

1. Build and install the debug APK.
2. Ensure the device meets Android Device Owner provisioning prerequisites. Typical development workflows require a fresh/reset test device without incompatible existing provisioning or accounts.
3. Run the ADB `dpm set-device-owner` command using the actual application ID and fully qualified receiver component.
4. Inspect `adb shell dumpsys device_policy`.
5. Query the app's status provider and verify it returns `DeviceOwner`.
6. Verify an ordinary installation without provisioning returns `NotProvisioned`.

Do not hardcode an assumed package name in project documentation. Use the actual Gradle `applicationId` and receiver class. ADB provisioning is for development/testing; production enrollment is a separate future change.

## 8. Error handling and observability

- Status-check errors must remain distinguishable from `NotProvisioned`.
- Do not report successful Device Owner status unless Android confirms it.
- Logs must be minimal and must not contain secrets.
- The feature must not crash the existing Home Screen because provisioning is absent.
- Status queries must be side-effect free.

## 9. Testing strategy

### Unit tests

Test status interpretation through a fake provider or injectable gateway:
- Device Owner API result is `true` -> `DeviceOwner`.
- Device Owner API result is `false` -> `NotProvisioned`.
- Status lookup throws -> `Error`.
- Repeated queries re-evaluate the current status rather than returning a permanently cached result.

If Android framework calls cannot be reliably mocked in the current test stack, wrap the call in a small gateway and unit-test the mapping separately.

### Android/device verification

On a compatible test device:
- Verify that the manifest receiver and XML metadata are accepted.
- Verify provisioning through ADB.
- Verify `dumpsys device_policy` identifies the expected Device Owner.
- Verify the app reports the correct state before and after provisioning.
- Verify the existing Home Screen still works when the app is not provisioned.
- Verify that this change does not apply Lock Task or other kiosk policies.

## 10. Acceptance and follow-up

This change is complete when the receiver is registered, status detection is testable, ADB provisioning is documented and verified, and the Home Screen remains unchanged.

The next change, `add-kiosk-policy-management`, may build on the DPC foundation to apply actual device policies. It must not be implemented as part of this change.
