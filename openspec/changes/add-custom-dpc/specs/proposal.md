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
- Support local development and verification using ADB on a compatible test device.
- Keep Android framework dependencies isolated from presentation and business logic.
- Add tests for provisioning-state detection and error handling.
- Document provisioning prerequisites and limitations.

## Proposed approach

### Administrative component

Add a `DeviceAdminReceiver` implementation and register it in `AndroidManifest.xml` with the required permission, metadata, and receiver declaration.

The receiver MUST remain minimal. It MUST NOT contain networking, database operations, kiosk policy orchestration, or long-running work.

### Device Owner status

Introduce a small device-management abstraction that checks whether the application package is the Device Owner using `DevicePolicyManager.isDeviceOwnerApp()`.

The implementation MUST distinguish at least:

- `NotProvisioned`: the application is not the Device Owner.
- `DeviceOwner`: the application is successfully provisioned as Device Owner.
- `Error`: the status cannot be determined because the check failed.

The implementation MUST NOT infer Device Owner status from receiver registration or legacy device-administrator activation.

### Architecture

Keep the DPC implementation independent from the existing Home Screen.

Use an interface around Android device-management status so that the state-detection logic can be unit tested without a physical device.

Use the project's existing dependency-injection conventions only where they provide a clear benefit. Do not introduce unnecessary persistence, network, or UI abstractions in this change.

### Provisioning

Document a development provisioning procedure using ADB and a compatible test device.

Production enrollment workflows, QR provisioning, kiosk policies, and backend registration are deferred to later changes.

### Compatibility

Maintain the project's minimum supported Android API level of 26.

The implementation MUST use APIs available at the minimum supported level or guard newer APIs explicitly.

## Scope

### Included

- `DeviceAdminReceiver` implementation.
- Manifest registration and device-admin XML metadata.
- Device Owner status abstraction.
- Provisioning-state model.
- Unit tests for status interpretation.
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

- The application builds successfully with the project's existing configuration.
- The administrative receiver is correctly declared in the manifest.
- The existing Home Screen continues to work without provisioning.
- An ordinary installation is reported as `NotProvisioned`.
- A correctly provisioned installation is reported as `DeviceOwner`.
- Status-detection failures are represented explicitly rather than silently interpreted as successful provisioning.
- The provisioning instructions describe device prerequisites and the limitations of ADB-based setup.
- Unit tests cover the status mapping and failure cases.
- No kiosk policy is applied as part of this change.

## Rollout

1. Inspect the existing Android manifest and package structure.
2. Implement the receiver and metadata.
3. Implement the Device Owner status abstraction.
4. Add unit tests.
5. Verify ordinary installation behavior.
6. Provision a compatible test device and verify Device Owner detection.
7. Review and archive the change before starting kiosk policy management.
