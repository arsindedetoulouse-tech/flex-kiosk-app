# Add Custom Device Policy Controller

## Why

The application currently contains a Home Screen but has no device-management component.

Future Kiosk Mode functionality requires a custom Device Policy Controller (DPC) that can be provisioned as Device Owner on compatible, fully managed Android devices.

Before implementing kiosk policies, Lock Task Mode, automatic startup, or remote management, the project needs a minimal and independently testable DPC foundation.

This change introduces the administrative component and verifies its provisioning state without changing the existing Home Screen behavior.

## Goals

- Register a custom `DeviceAdminReceiver`.
- Declare the required device-administrator metadata in the Android manifest.
- Introduce a `CustomDevicePolicyController` abstraction for checking Device Owner status.
- Expose a deterministic provisioning state to the application.
- Surface the provisioning state on the Home Screen as an operator-facing status card.
- Support local development and verification using ADB on a compatible test device.
- Keep Android framework dependencies isolated from presentation and business logic.
- Add tests for provisioning-state detection and error handling.
- Document provisioning prerequisites and limitations.

## Proposed approach

### Administrative component

Add a `DeviceAdminReceiver` implementation and declare it in the `core:devicepolicy` module manifest with the required permission, metadata, and receiver declaration. Gradle manifest merging brings it into the application APK.

The receiver MUST remain minimal. It MUST NOT contain networking, database operations, kiosk policy orchestration, or long-running work.

### Device Owner status

Introduce a small device-management abstraction that checks whether the application package is the Device Owner using `DevicePolicyManager.isDeviceOwnerApp()`.

The implementation MUST distinguish at least:

- `NotProvisioned`: the application is not the Device Owner.
- `DeviceOwner`: the application is successfully provisioned as Device Owner.
- `Error`: the status cannot be determined because the check failed.

The implementation MUST NOT infer Device Owner status from receiver registration or legacy device-administrator activation.

### Architecture

Place the DPC code in a new `core:devicepolicy` module under the existing `com.arsinde.flexkioskapp` namespace. Device management is Android platform infrastructure: it does not belong in `app`, in a feature module, or in `common:data`. Placing it in `core` also lets the later kiosk changes depend on the DPC foundation without depending on the application module.

Keep the DPC implementation independent from the existing Home Screen.

Use an interface around Android device-management status so that the state-detection logic can be unit tested without a physical device. The interface and the status model must not reference Android types.

Use the project's existing dependency-injection conventions only where they provide a clear benefit. Do not introduce unnecessary persistence, network, or UI abstractions in this change.

### Provisioning

Document a development provisioning procedure using ADB and a compatible test device.

Production enrollment workflows, QR provisioning, kiosk policies, and backend registration are deferred to later changes.

### Compatibility

Maintain the project's minimum supported Android API level of 24, as configured by the `android.library` / `android.application` convention plugins in `build-logic`.

Every API used by this change is available at level 24: `DeviceAdminReceiver` (API 8), `DevicePolicyManager.isDeviceOwnerApp` (API 17), and `dpm set-device-owner` (API 21). No version guard is therefore required. If a later kiosk change needs a higher API level, that change raises `minSdk` through the convention plugin rather than adding `Build.VERSION` checks here.

## Scope

### Included

- New `core:devicepolicy` module registered in `settings.gradle.kts`.
- `DeviceAdminReceiver` implementation.
- Manifest registration in the module and device-admin XML metadata.
- Device Owner status abstraction and provisioning-state model.
- A provisioning status card on the Home Screen, including a detail-free explanation when the state
  cannot be determined.
- Koin registration and a Koin resolution test.
- Unit tests for status interpretation and failure cases.
- Presentation mapping of the provisioning state onto a Home-owned model.
- Basic device-level verification instructions.
- ADB provisioning instructions for development.

### Excluded

- Lock Task Mode and screen pinning.
- Persistent HOME activity configuration.
- Kiosk startup and recovery.
- User restrictions and system UI restrictions.
- Remote management, Ktor, and backend APIs.
- SQLite persistence.
- Maintenance mode and device decommissioning.
- Changes to existing Home Screen business behavior.

## Acceptance criteria

- The application builds successfully with the project's existing configuration and `minSdk` stays at 24.
- The administrative receiver is correctly declared in the merged manifest.
- `core:devicepolicy` exists, is registered in `settings.gradle.kts`, and depends on neither `app` nor any `feature:*` module.
- The existing Home Screen continues to work without provisioning.
- The Home Screen shows the provisioning state, and an indeterminate state is explained without any
  failure detail.
- An ordinary installation is reported as `NotProvisioned`.
- A correctly provisioned installation is reported as `DeviceOwner`.
- Status-detection failures are represented explicitly rather than silently interpreted as successful provisioning.
- `AndroidDeviceOwnerStatusProvider` is resolvable from Koin and covered by a resolution test.
- The provisioning instructions describe device prerequisites and the limitations of ADB-based setup.
- Unit tests cover the status mapping, failure cases, and re-evaluation on repeated queries.
- No kiosk policy is applied as part of this change.

## Rollout

1. Inspect the existing Android manifest and package structure.
2. Implement the receiver and metadata.
3. Implement the Device Owner status abstraction.
4. Add unit tests.
5. Verify ordinary installation behavior.
6. Provision a compatible test device and verify Device Owner detection.
7. Review and archive the change before starting kiosk policy management.
