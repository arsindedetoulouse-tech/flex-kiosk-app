# Design: Kiosk Policy Management

## Context

This change follows `add-custom-dpc`. It introduces policy orchestration only; it does not start Lock Task Mode or implement remote management.

## Components

- `KioskPolicy`: immutable, typed desired configuration with a schema/version.
- `KioskPolicyValidator`: validates package names, component references, supported feature combinations, and required/optional policy fields.
- `DevicePolicyGateway`: interface for administrative operations and capability queries.
- `AndroidDevicePolicyGateway`: Android implementation backed by `DevicePolicyManager` and `PackageManager`.
- `KioskPolicyManager`: coordinates validation, application, reconciliation, and result reporting.
- `PolicyApplyResult`: records applied version, per-operation outcomes, unsupported features, and errors.

## Rules

- Require `isDeviceOwnerApp(packageName)` before privileged operations.
- Use `setLockTaskPackages()` for the explicit package allowlist.
- Use `addPersistentPreferredActivity()` for the configured HOME intent filter/component.
- Use `setLockTaskFeatures()` only on API 28+; validate feature dependencies and document API-specific behavior.
- Apply user restrictions only when explicitly represented in the policy.
- Do not assume all device policies can be queried back exactly. Distinguish requested, operation-confirmed, and observed state.
- Do not start Lock Task Mode in this change.

## Failure handling

Validate the complete policy before mutations. Since Android policy operations are not a single atomic transaction, record per-operation results and report partial application. Reconciliation must be idempotent and safe to retry.

## Testing

Unit-test validation and orchestration through a fake `DevicePolicyGateway`. Device tests verify actual behavior on supported OS/device combinations.
