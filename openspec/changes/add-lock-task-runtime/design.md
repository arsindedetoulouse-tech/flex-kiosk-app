# Design: Lock Task Runtime

## Context

Depends on `add-custom-dpc` and `add-kiosk-policy-management`. This change does not alter the policy allowlist or HOME resolution.

## Components

- `LockTaskState`: `Unavailable`, `NotActive`, `Locked`, `Pinned`, `Entering`, `Error`.
- `LockTaskGateway`: reads ActivityManager state and requests entry through the current foreground Activity.
- `KioskRuntimeCoordinator`: checks provisioning/allowlist preconditions, coordinates entry, and publishes state.
- `KioskRuntimeState`: observable state for ViewModel/diagnostics.
- Thin Activity integration that forwards lifecycle events to the coordinator.

## Behavior

Use `ActivityManager.lockTaskModeState` to observe state. Treat `LOCK_TASK_MODE_LOCKED` as full lock and `LOCK_TASK_MODE_PINNED` as screen pinning. Request `startLockTask()` only when the package is permitted and an eligible Activity is foreground/resumed. Re-query state after the request.

Do not put runtime logic in composables. Do not assume the Activity remains alive. No background loop is required.

## Failure handling

Represent missing privileges, not-allowlisted package, request exception, and observed-state mismatch explicitly. Any retry must be bounded and triggered by lifecycle/state changes, not a tight loop.
